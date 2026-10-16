package com.smishguard.smishguard.controller;

import com.smishguard.smishguard.repository.BankSenderIdRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/** GET /api/sender/{code} – is this sender code a known bank? */
@RestController
@RequestMapping("/api")
public class SenderController {

    private final BankSenderIdRepository repo;

    public SenderController(BankSenderIdRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/sender/{code}")
    public Map<String, Object> lookup(@PathVariable String code) {
        return repo.findBySenderCode(code.toUpperCase())
            .map(s -> Map.<String, Object>of(
                "known", true,
                "bank", s.getBankName(),
                "senderCode", s.getSenderCode()
            ))
            .orElse(Map.of("known", false, "senderCode", code));
    }
}
