package com.smishguard.smishguard.controller;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

/** GET /api/health – simple health check endpoint. */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "timestamp", Instant.now().toString());
    }
}
