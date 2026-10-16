package com.smishguard.smishguard.service;

import com.smishguard.smishguard.entity.CheckHistory;
import com.smishguard.smishguard.model.CheckRequest;
import com.smishguard.smishguard.model.CheckResponse;
import com.smishguard.smishguard.model.CheckResponse.Highlight;
import com.smishguard.smishguard.repository.CheckHistoryRepository;
import com.smishguard.smishguard.service.RuleCheckService.RuleResult;
import com.smishguard.smishguard.service.UrlScanService.ScanResult;
import com.smishguard.smishguard.service.LlmService.LlmResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.*;

/**
 * Step 5 – Combines all signals into a final verdict.
 * Score bands: 0-29 GENUINE, 30-59 SUSPICIOUS, 60+ FAKE.
 * If rules and LLM disagree → SUSPICIOUS.
 */
@Service
public class VerdictService {

    private final MaskingService masker;
    private final RuleCheckService ruleChecker;
    private final UrlScanService urlScanner;
    private final LlmService llmService;
    private final CheckHistoryRepository historyRepo;

    public VerdictService(MaskingService masker, RuleCheckService ruleChecker,
                          UrlScanService urlScanner, LlmService llmService,
                          CheckHistoryRepository historyRepo) {
        this.masker = masker;
        this.ruleChecker = ruleChecker;
        this.urlScanner = urlScanner;
        this.llmService = llmService;
        this.historyRepo = historyRepo;
    }

    public CheckResponse check(CheckRequest req) {
        boolean scanLinks = req.scanLinks() == null || req.scanLinks();
        boolean useAi     = req.useAi()     == null || req.useAi();

        // Step 1 – Mask sensitive data
        String masked = masker.mask(req.message());

        // Step 2 – Rule checks
        RuleResult rules = ruleChecker.evaluate(masked, req.sender());
        int score = rules.score();
        List<String> reasons = new ArrayList<>(rules.reasons());
        List<Highlight> highlights = new ArrayList<>(rules.highlights());

        // Step 3 – URL scan (only if URLs exist and scan is enabled)
        ScanResult urlScan = new ScanResult(false, "not scanned");
        if (scanLinks && !rules.urls().isEmpty()) {
            urlScan = urlScanner.scan(rules.urls());
            if (urlScan.malicious()) {
                score = 100; // hard FAKE
                reasons.add("At least one link flagged as malicious by Safe Browsing");
            }
        } else if (rules.urls().isEmpty()) {
            urlScan = new ScanResult(false, "no links");
        }

        // Step 4 – LLM (only in uncertain range 30-59)
        LlmResult llm = null;
        boolean llmUsed = false;
        if (useAi && score >= 30 && score < 60 && !urlScan.malicious()) {
            llm = llmService.analyze(masked, detectBank(req.sender()));
            if (llm != null) {
                llmUsed = true;
                reasons.add("AI analysis: " + llm.reason());
            }
        }

        // Step 5 – Final verdict
        String rulesVerdict = scoreToVerdict(score);
        String finalVerdict;
        int finalScore = score;

        if (urlScan.malicious()) {
            finalVerdict = "FAKE";
            finalScore = 100;
        } else if (llm != null) {
            if (rulesVerdict.equals(llm.verdict())) {
                finalVerdict = rulesVerdict;
            } else {
                // Disagreement → SUSPICIOUS
                finalVerdict = "SUSPICIOUS";
                finalScore = Math.max(30, Math.min(59, score));
                reasons.add("Rules and AI analysis disagree – treating as SUSPICIOUS");
            }
        } else {
            finalVerdict = rulesVerdict;
        }

        // Detect scam type from reasons/highlights
        String scamType = finalVerdict.equals("GENUINE") ? null : detectScamType(masked, reasons);

        // Build advice
        String advice = buildAdvice(finalVerdict);

        // Save to history (only masked message)
        historyRepo.save(new CheckHistory(masked, finalVerdict, finalScore, scamType));

        return new CheckResponse(
            finalVerdict, finalScore, scamType, reasons, highlights,
            urlScan.status(), llmUsed, advice, Instant.now()
        );
    }

    // ── Helpers ────────────────────────────────────────────────────────

    public static String scoreToVerdict(int score) {
        if (score < 30) return "GENUINE";
        if (score < 60) return "SUSPICIOUS";
        return "FAKE";
    }

    private String detectBank(String sender) {
        if (sender == null || sender.isBlank()) return null;
        String up = sender.toUpperCase();
        if (up.contains("HDFC")) return "HDFC";
        if (up.contains("SBI"))  return "SBI";
        if (up.contains("ICICI"))return "ICICI";
        if (up.contains("AXIS")) return "Axis";
        if (up.contains("KOTAK"))return "Kotak";
        if (up.contains("PNB"))  return "PNB";
        return null;
    }

    private String detectScamType(String message, List<String> reasons) {
        String m = message.toLowerCase();
        String r = String.join(" ", reasons).toLowerCase();
        if (m.contains("kyc") || m.contains("pan") || m.contains("aadhaar") || r.contains("kyc")) return "kyc_update";
        if (m.contains("block") || m.contains("suspend") || r.contains("blocked")) return "account_blocked";
        if (m.contains("reward") || m.contains("cashback") || r.contains("reward")) return "fake_rewards";
        if (m.contains("lottery") || m.contains("won") || m.contains("lucky"))  return "lottery";
        if (m.contains("<otp>") || r.contains("otp")) return "otp_request";
        if (m.contains("apk") || m.contains("download") || r.contains("apk"))   return "apk_download";
        if (m.contains("loan") || m.contains("pre-approved")) return "fake_loan";
        if (m.contains("upi") && m.contains("refund")) return "upi_refund";
        if (r.contains("call") || r.contains("support number")) return "fake_support_number";
        if (m.contains("debited") || m.contains("debit")) return "fake_debit_alert";
        return "other";
    }

    private String buildAdvice(String verdict) {
        String base = "Do not click links. Verify in your official bank app.";
        if (verdict.equals("FAKE")) {
            return base + " Report cyber fraud at cybercrime.gov.in or call 1930.";
        }
        return base;
    }
}
