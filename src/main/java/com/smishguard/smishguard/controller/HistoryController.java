package com.smishguard.smishguard.controller;

import com.smishguard.smishguard.entity.CheckHistory;
import com.smishguard.smishguard.repository.CheckHistoryRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** GET /api/history – returns last 20 check results. */
@RestController
@RequestMapping("/api")
public class HistoryController {

    private final CheckHistoryRepository repo;

    public HistoryController(CheckHistoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/history")
    public List<CheckHistory> history() {
        return repo.findTop20ByOrderByCreatedAtDesc();
    }
}
