package com.smishguard.smishguard.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** User feedback reports (user says message is fake or genuine). */
@Entity
@Table(name = "user_reports")
public class UserReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "masked_message", length = 2000)
    private String maskedMessage;

    @Column(name = "user_says") // "fake" or "genuine"
    private String userSays;

    @Column(name = "system_verdict")
    private String systemVerdict;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "reviewed")
    private boolean reviewed = false;

    public UserReport() {}
    public UserReport(String maskedMessage, String userSays, String systemVerdict) {
        this.maskedMessage = maskedMessage;
        this.userSays = userSays;
        this.systemVerdict = systemVerdict;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getMaskedMessage() { return maskedMessage; }
    public String getUserSays() { return userSays; }
    public String getSystemVerdict() { return systemVerdict; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isReviewed() { return reviewed; }
}
