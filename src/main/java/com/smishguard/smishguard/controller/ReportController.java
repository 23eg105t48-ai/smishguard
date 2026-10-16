package com.smishguard.smishguard.controller;

import com.smishguard.smishguard.entity.UserReport;
import com.smishguard.smishguard.repository.UserReportRepository;
import com.smishguard.smishguard.service.MaskingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

/** POST /api/report – submit user feedback; GET /api/reports – admin view. */
@RestController
@RequestMapping("/api")
public class ReportController {

    private final UserReportRepository repo;
    private final MaskingService masker;

    @Value("${app.admin-key:dev-admin-key}")
    private String adminKey;

    public ReportController(UserReportRepository repo, MaskingService masker) {
        this.repo = repo;
        this.masker = masker;
    }

    /** Request body for user feedback. */
    public record ReportRequest(
        @NotBlank @Size(max = 1000) String message,
        @NotBlank @Pattern(regexp = "fake|genuine") String userSaysIs,
        String systemVerdict
    ) {}

    @PostMapping("/report")
    public ResponseEntity<Map<String, String>> report(@Valid @RequestBody ReportRequest req) {
        // Mask before storing
        String masked = masker.mask(req.message());
        repo.save(new UserReport(masked, req.userSaysIs(), req.systemVerdict()));
        return ResponseEntity.ok(Map.of("status", "reported"));
    }

    /** Admin endpoint – requires ADMIN_KEY header. */
    @GetMapping("/reports")
    public List<UserReport> reports(@RequestHeader("ADMIN_KEY") String key) {
        if (!adminKey.equals(key)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin key");
        }
        return repo.findByReviewedFalseOrderByCreatedAtDesc();
    }
}
