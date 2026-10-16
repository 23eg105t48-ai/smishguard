package com.smishguard.smishguard.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** Stores the result of every /api/check call. Only the masked message is saved. */
@Entity
@Table(name = "check_history")
public class CheckHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "masked_message", length = 2000)
    private String maskedMessage;

    @Column(name = "verdict")
    private String verdict; // GENUINE, SUSPICIOUS, FAKE

    @Column(name = "risk_score")
    private int riskScore;

    @Column(name = "scam_type")
    private String scamType;

    @Column(name = "created_at")
    private Instant createdAt;

    public CheckHistory() {}
    public CheckHistory(String maskedMessage, String verdict, int riskScore, String scamType) {
        this.maskedMessage = maskedMessage;
        this.verdict = verdict;
        this.riskScore = riskScore;
        this.scamType = scamType;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getMaskedMessage() { return maskedMessage; }
    public String getVerdict() { return verdict; }
    public int getRiskScore() { return riskScore; }
    public String getScamType() { return scamType; }
    public Instant getCreatedAt() { return createdAt; }
}
