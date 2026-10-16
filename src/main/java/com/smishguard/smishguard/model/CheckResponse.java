package com.smishguard.smishguard.model;

import java.time.Instant;
import java.util.List;

/**
 * Full response from POST /api/check.
 * All fields are always present; nulls allowed only for scamType (genuine msgs).
 */
public record CheckResponse(
    String verdict,           // GENUINE | SUSPICIOUS | FAKE
    int riskScore,            // 0-100
    String scamType,          // null for genuine
    List<String> reasons,     // plain-language explanations
    List<Highlight> highlights, // suspicious phrases with category
    String linkStatus,        // e.g. "clean", "malicious", "not scanned"
    boolean llmUsed,
    String advice,
    Instant checkedAt
) {
    /** A highlighted phrase and its risk category. */
    public record Highlight(String phrase, String category) {}
}
