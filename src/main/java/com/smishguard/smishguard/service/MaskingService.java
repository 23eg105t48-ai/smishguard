package com.smishguard.smishguard.service;

import org.springframework.stereotype.Service;
import java.util.regex.*;

/**
 * Step 1 of the pipeline: masks sensitive data before any external call or storage.
 * Masking order matters:
 *   1. Phone numbers (10 digits) masked first → <PHONE>  (before accounts catch them)
 *   2. OTP-like digits near "otp/code" → <OTP>
 *   3. Account / card numbers (9-18 digits) → XXXXXX + last 4 digits
 */
@Service
public class MaskingService {

    // Standalone 10-digit phone number (not part of a longer number)
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(\\d{10})(?!\\d)");

    // 4-8 digit number that appears near the word "otp" or "code"
    // Group 1: digit before the keyword, Group 2: digit after the keyword
    private static final Pattern OTP_PATTERN = Pattern.compile(
        "(?i)(\\b\\d{4,8}\\b)(?=.{0,50}\\b(?:otp|code)\\b)|(?<=\\b(?:otp|code)\\b.{0,50})(\\b\\d{4,8}\\b)",
        Pattern.DOTALL
    );

    // 9-18 consecutive digits (account / card number) – matches what remains after phone masking
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("\\b(\\d{5,14})(\\d{4})\\b");

    public String mask(String text) {
        if (text == null) return null;
        // 1. Phones first (before accounts catch them)
        String result = PHONE_PATTERN.matcher(text).replaceAll("<PHONE>");
        // 2. OTPs
        result = maskOtp(result);
        // 3. Remaining long digit sequences = account/card numbers
        result = maskAccountNumbers(result);
        return result;
    }

    private String maskOtp(String text) {
        StringBuffer sb = new StringBuffer();
        Matcher m = OTP_PATTERN.matcher(text);
        while (m.find()) {
            String digits = m.group(1) != null ? m.group(1) : m.group(2);
            if (digits == null) { m.appendReplacement(sb, m.group()); continue; }
            String replacement = m.group().replace(digits, "<OTP>");
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String maskAccountNumbers(String text) {
        StringBuffer sb = new StringBuffer();
        Matcher m = ACCOUNT_PATTERN.matcher(text);
        while (m.find()) {
            m.appendReplacement(sb, "XXXXXX" + m.group(2));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
