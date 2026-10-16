package com.smishguard.smishguard.controller;

import com.smishguard.smishguard.repository.CheckHistoryRepository;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** GET /api/stats – aggregated statistics for the dashboard. */
@RestController
@RequestMapping("/api")
public class StatsController {

    private final CheckHistoryRepository repo;

    public StatsController(CheckHistoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        long total   = repo.count();
        long genuine = repo.countByVerdict("GENUINE");
        long suspicious = repo.countByVerdict("SUSPICIOUS");
        long fake    = repo.countByVerdict("FAKE");

        // Top scam types
        List<Object[]> scamRows = repo.countByScamType();
        List<Map<String, Object>> topScamTypes = scamRows.stream()
            .limit(5)
            .map(row -> Map.<String, Object>of("type", row[0], "count", row[1]))
            .toList();

        // Last 7 days by verdict
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<Object[]> recentRows = repo.countByVerdictSince(sevenDaysAgo);
        Map<String, Long> last7Days = new LinkedHashMap<>();
        for (Object[] row : recentRows) {
            last7Days.put((String) row[0], (Long) row[1]);
        }

        return Map.of(
            "total", total,
            "genuine", genuine,
            "suspicious", suspicious,
            "fake", fake,
            "topScamTypes", topScamTypes,
            "last7Days", last7Days
        );
    }
}
