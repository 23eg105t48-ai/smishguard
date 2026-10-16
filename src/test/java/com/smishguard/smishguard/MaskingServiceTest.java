package com.smishguard.smishguard;

import com.smishguard.smishguard.service.MaskingService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for MaskingService – no Spring context needed. */
class MaskingServiceTest {

    private final MaskingService masker = new MaskingService();

    @Test
    void masksOtpAfterKeyword() {
        // "OTP is 483921" → otp keyword then number
        String result = masker.mask("Your OTP is 483921. Do not share.");
        assertThat(result).contains("<OTP>").doesNotContain("483921");
    }

    @Test
    void masksOtpBeforeKeyword() {
        // "483921 is your OTP" → number then otp keyword
        String result = masker.mask("483921 is your OTP for the transaction.");
        assertThat(result).contains("<OTP>").doesNotContain("483921");
    }

    @Test
    void masksAccountNumber() {
        String result = masker.mask("A/c 123456789012 has been debited.");
        assertThat(result).contains("XXXXXX9012").doesNotContain("123456789012");
    }

    @Test
    void masksPhoneNumber() {
        // Phone (10 digits) is masked BEFORE account pattern runs
        String result = masker.mask("Not you? Call 9876543210 immediately.");
        assertThat(result).contains("<PHONE>").doesNotContain("9876543210");
    }

    @Test
    void doesNotMaskShortNumbers() {
        // 4-digit balance is not an account number (needs 9+)
        String result = masker.mask("Your balance is Rs.1234.");
        assertThat(result).contains("1234");
    }

    @Test
    void masksMultiplePatterns() {
        // Phone → <PHONE> first, then OTP, then accounts
        String input = "OTP 123456 sent. A/c 987654321098 debited. Call 9999988888.";
        String result = masker.mask(input);
        assertThat(result).contains("<OTP>");
        assertThat(result).contains("XXXXXX1098");
        assertThat(result).contains("<PHONE>");
    }
}
