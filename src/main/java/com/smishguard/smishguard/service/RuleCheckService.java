package com.smishguard.smishguard.service;

import com.smishguard.smishguard.entity.GenuineTemplate;
import com.smishguard.smishguard.model.CheckResponse.Highlight;
import com.smishguard.smishguard.repository.BankSenderIdRepository;
import com.smishguard.smishguard.repository.GenuineTemplateRepository;
import com.smishguard.smishguard.repository.TrustedDomainRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.regex.*;

/**
 * Step 2 of the pipeline – cheap rule-based checks.
 * Each rule adds points (risk) and records reasons + highlights.
 * Final score is clamped to 0..60.
 */
@Service
public class RuleCheckService {

    private final BankSenderIdRepository senderRepo;
    private final TrustedDomainRepository domainRepo;
    private final GenuineTemplateRepository templateRepo;

    public RuleCheckService(BankSenderIdRepository senderRepo,
                            TrustedDomainRepository domainRepo,
                            GenuineTemplateRepository templateRepo) {
        this.senderRepo = senderRepo;
        this.domainRepo = domainRepo;
        this.templateRepo = templateRepo;
    }

    /** Result of the rule-check phase. */
    public record RuleResult(int score, List<String> reasons, List<Highlight> highlights, List<String> urls) {}

    // ── Patterns ───────────────────────────────────────────────────────

    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+"
    );

    private static final List<String> SHORTENED_DOMAINS = List.of(
        "bit.ly", "tinyurl.com", "cutt.ly", "goo.gl", "t.co", "short.ly",
        "ow.ly", "rb.gy", "is.gd", "tiny.cc"
    );

    private static final List<String> SUSPICIOUS_TLDS = List.of(
        ".xyz", ".top", ".click", ".info", ".online", ".live", ".site",
        ".buzz", ".loan", ".work", ".win", ".party"
    );

    // Known look-alike patterns: bank name in domain but domain isn't trusted
    private static final List<String> BANK_KEYWORDS = List.of(
        "hdfc", "sbi", "icici", "axis", "kotak", "pnb", "bob", "canara",
        "union", "idfc", "yes", "indusind", "paytm", "amazon"
    );

    private static final Pattern URGENCY_PATTERN = Pattern.compile(
        "(?i)\\b(block(?:ed|ing)?|suspend(?:ed|ing)?|immediately|today only|" +
        "expire[sd]?|urgent(?:ly)?|last chance|final notice|account (?:will be )?closed|" +
        "kyc (?:pending|expired)|jaldi|turant|abhi|kal tak|band ho jayega)\\b"
    );

    private static final Pattern CREDENTIAL_PATTERN = Pattern.compile(
        "(?i)\\b(otp|pin|cvv|password|card (?:number|details)|" +
        "net ?banking (?:id|password)|kyc|pan card|aadhaar|update (?:kyc|details)|" +
        "verify (?:your )?(?:account|kyc|pan)|click.*(?:link|url))\\b"
    );

    private static final Pattern APK_PATTERN = Pattern.compile(
        "(?i)\\b(download|install|apk|app)\\b"
    );

    private static final Pattern SUPPORT_NUMBER_PATTERN = Pattern.compile(
        "(?i)(not you\\?|didn't do this\\?|call\\s+(?:us|now)?\\s*(?:on\\s+)?\\d{10}|" +
        "helpline\\s*:\\s*\\d{10}|contact\\s+us\\s+at\\s+\\d{10})"
    );

    private static final Pattern REWARD_PATTERN = Pattern.compile(
        "(?i)\\b(lottery|prize|reward points?|cashback|won|winner|lucky|" +
        "instant loan|pre-?approved loan|no documents?|free (?:gift|money)|" +
        "refund of rs|upi refund)\\b"
    );

    // ── Public method ──────────────────────────────────────────────────

    public RuleResult evaluate(String maskedMessage, String sender) {
        int score = 0;
        List<String> reasons = new ArrayList<>();
        List<Highlight> highlights = new ArrayList<>();
        List<String> urls = extractUrls(maskedMessage);

        // Rule 1 – Sender ID check
        score += checkSender(sender, reasons, highlights);

        // Rule 2 – Link analysis
        score += checkLinks(urls, maskedMessage, reasons, highlights);

        // Rule 3 – Urgency words
        score += checkPattern(maskedMessage, URGENCY_PATTERN, 10, "urgency",
                "Urgent language detected", reasons, highlights);

        // Rule 4 – Credential / KYC requests
        score += checkPattern(maskedMessage, CREDENTIAL_PATTERN, 15, "credential",
                "Requests sensitive credentials (OTP/PIN/KYC/PAN)", reasons, highlights);

        // Rule 5 – APK download prompt
        score += checkPattern(maskedMessage, APK_PATTERN, 15, "link",
                "Promotes app/APK download via SMS", reasons, highlights);

        // Rule 6 – Fake support number pressure
        score += checkPattern(maskedMessage, SUPPORT_NUMBER_PATTERN, 10, "sender",
                "Pressures user to call a number", reasons, highlights);

        // Rule 7 – Too-good-to-be-true rewards
        score += checkPattern(maskedMessage, REWARD_PATTERN, 10, "reward",
                "Promises prize, lottery or instant loan", reasons, highlights);

        // Rule 8 – Genuine template match (reduces score)
        score += checkGenuineTemplate(maskedMessage, reasons);

        // Clamp to 0..60
        score = Math.max(0, Math.min(60, score));
        return new RuleResult(score, reasons, highlights, urls);
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private int checkSender(String sender, List<String> reasons, List<Highlight> highlights) {
        if (sender == null || sender.isBlank()) return 0;

        sender = sender.trim();
        // Mobile-number sender (all digits)
        if (sender.matches("\\d+")) {
            reasons.add("Sender is a plain mobile number, not a bank header");
            highlights.add(new Highlight(sender, "sender"));
            return 15;
        }

        // Extract code after "-" (e.g. VK-HDFCBK → HDFCBK)
        String code = sender.contains("-") ? sender.substring(sender.lastIndexOf('-') + 1) : sender;
        if (!senderRepo.existsBySenderCode(code.toUpperCase())) {
            reasons.add("Sender header '" + sender + "' is not a registered bank code");
            highlights.add(new Highlight(sender, "sender"));
            return 15;
        }
        return 0;
    }

    private int checkLinks(List<String> urls, String message, List<String> reasons, List<Highlight> highlights) {
        if (urls.isEmpty()) return 0;
        int pts = 0;
        for (String url : urls) {
            String host = extractHost(url).toLowerCase();

            // Shortened link
            if (SHORTENED_DOMAINS.stream().anyMatch(host::endsWith)) {
                reasons.add("Shortened link detected: " + host);
                highlights.add(new Highlight(url, "link"));
                pts += 10;
            }

            // Check if domain is trusted
            boolean trusted = isTrustedDomain(host);
            if (!trusted) {
                // Look-alike: contains a bank keyword but isn't the real domain
                boolean lookalike = BANK_KEYWORDS.stream().anyMatch(host::contains);
                if (lookalike) {
                    reasons.add("Look-alike bank domain: " + host);
                    highlights.add(new Highlight(url, "link"));
                    pts += 15;
                } else {
                    reasons.add("Link goes to an unknown/untrusted domain: " + host);
                    highlights.add(new Highlight(url, "link"));
                    pts += 10;
                }

                // Suspicious TLD check
                if (SUSPICIOUS_TLDS.stream().anyMatch(host::endsWith)) {
                    reasons.add("Suspicious top-level domain on link: " + host);
                    pts += 5;
                }
            }
        }
        return pts;
    }

    /** Checks if domain or its parent matches a trusted domain in DB. */
    private boolean isTrustedDomain(String host) {
        // Remove www prefix
        String clean = host.startsWith("www.") ? host.substring(4) : host;
        if (domainRepo.findByDomain(clean).isPresent()) return true;
        // Check parent domain (e.g. login.hdfcbank.com → hdfcbank.com)
        int dot = clean.indexOf('.');
        if (dot > 0) {
            String parent = clean.substring(dot + 1);
            return domainRepo.findByDomain(parent).isPresent();
        }
        return false;
    }

    private int checkPattern(String text, Pattern p, int maxPts, String category,
                             String reason, List<String> reasons, List<Highlight> highlights) {
        Matcher m = p.matcher(text);
        boolean found = false;
        while (m.find()) {
            highlights.add(new Highlight(m.group(), category));
            found = true;
        }
        if (found) {
            reasons.add(reason);
            return maxPts;
        }
        return 0;
    }

    /** Subtracts 15 if message matches a genuine template regex. */
    private int checkGenuineTemplate(String message, List<String> reasons) {
        List<GenuineTemplate> templates = templateRepo.findAll();
        for (GenuineTemplate t : templates) {
            try {
                if (Pattern.compile(t.getTemplateText(), Pattern.CASE_INSENSITIVE).matcher(message).find()) {
                    reasons.add("Message matches a known genuine bank template (" + t.getBankName() + " " + t.getType() + ")");
                    return -15;
                }
            } catch (PatternSyntaxException ignored) {
                // Bad regex in DB – skip silently
            }
        }
        return 0;
    }

    private List<String> extractUrls(String text) {
        List<String> urls = new ArrayList<>();
        Matcher m = URL_PATTERN.matcher(text);
        while (m.find()) urls.add(m.group());
        return urls;
    }

    private String extractHost(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return url;
        }
    }
}
