package com.smishguard.smishguard.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/check.
 * scanLinks and useAi default to true if not provided.
 */
public record CheckRequest(
    @NotBlank(message = "message must not be blank")
    @Size(max = 1000, message = "message must be at most 1000 characters")
    String message,

    String sender,          // optional – e.g. "VK-HDFCBK"

    Boolean scanLinks,      // defaults to true in service

    Boolean useAi           // defaults to true in service
) {}
