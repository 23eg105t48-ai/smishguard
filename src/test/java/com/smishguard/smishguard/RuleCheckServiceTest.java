package com.smishguard.smishguard;

import com.smishguard.smishguard.repository.BankSenderIdRepository;
import com.smishguard.smishguard.repository.GenuineTemplateRepository;
import com.smishguard.smishguard.repository.TrustedDomainRepository;
import com.smishguard.smishguard.service.RuleCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

/** Integration tests for RuleCheckService using the seeded H2 database. */
@SpringBootTest
@ActiveProfiles("local")
class RuleCheckServiceTest {

    @Autowired RuleCheckService ruleChecker;

    @Test
    void knownSenderScoresZero() {
        var result = ruleChecker.evaluate("Rs.500 debited from A/c XXXXXX1234.", "VK-HDFCBK");
        // Sender is known – no sender penalty
        assertThat(result.reasons()).noneMatch(r -> r.contains("not a registered bank code"));
    }

    @Test
    void unknownSenderAddsPenalty() {
        var result = ruleChecker.evaluate("Rs.500 debited from A/c XXXXXX1234.", "VK-FAKEBK");
        assertThat(result.score()).isGreaterThanOrEqualTo(15);
        assertThat(result.reasons()).anyMatch(r -> r.contains("not a registered bank code"));
    }

    @Test
    void mobileSenderAddsPenalty() {
        var result = ruleChecker.evaluate("Your account has been blocked.", "9876543210");
        assertThat(result.score()).isGreaterThan(0);
        assertThat(result.reasons()).anyMatch(r -> r.contains("plain mobile number"));
    }

    @Test
    void suspiciousLinkDetected() {
        var result = ruleChecker.evaluate(
            "Update KYC at http://hdfcbank-kyc.xyz now!", null);
        assertThat(result.score()).isGreaterThan(20);
        assertThat(result.reasons()).anyMatch(r -> r.toLowerCase().contains("domain"));
    }

    @Test
    void trustedLinkNotPenalized() {
        var result = ruleChecker.evaluate(
            "Pay bill at https://hdfcbank.com/pay", null);
        // Should not add link penalty for trusted domain
        assertThat(result.reasons()).noneMatch(r -> r.contains("unknown/untrusted domain"));
    }

    @Test
    void urgencyWordsDetected() {
        var result = ruleChecker.evaluate(
            "Your account is SUSPENDED. Update immediately.", null);
        assertThat(result.score()).isGreaterThanOrEqualTo(10);
        assertThat(result.reasons()).anyMatch(r -> r.contains("Urgent language"));
    }

    @Test
    void genuineDebitScoresLow() {
        var result = ruleChecker.evaluate(
            "Rs.500.00 debited from A/c XXXXXX1234. Avl Bal Rs.9,500.00", "VK-HDFCBK");
        // Should match genuine template and get negative or zero
        assertThat(result.score()).isLessThan(30);
    }

    @Test
    void scamMessageScoresHigh() {
        var result = ruleChecker.evaluate(
            "Account blocked! Update KYC at http://sbi-kyc.xyz IMMEDIATELY or account closes", "9876543210");
        assertThat(result.score()).isGreaterThan(30);
    }
}
