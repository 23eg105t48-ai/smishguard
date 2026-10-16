package com.smishguard.smishguard.controller;

import com.smishguard.smishguard.model.CheckRequest;
import com.smishguard.smishguard.model.CheckResponse;
import com.smishguard.smishguard.service.VerdictService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** POST /api/check – run the full detection pipeline. */
@RestController
@RequestMapping("/api")
public class CheckController {

    private final VerdictService verdictService;

    public CheckController(VerdictService verdictService) {
        this.verdictService = verdictService;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckResponse> check(@Valid @RequestBody CheckRequest req) {
        CheckResponse resp = verdictService.check(req);
        return ResponseEntity.ok(resp);
    }
}
