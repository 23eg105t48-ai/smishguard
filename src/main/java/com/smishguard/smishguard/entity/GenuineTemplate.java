package com.smishguard.smishguard.entity;

import jakarta.persistence.*;

/** Regex templates for genuine bank messages used to reduce false positives. */
@Entity
@Table(name = "genuine_templates")
public class GenuineTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "type")
    private String type;  // e.g. debit, otp, upi, balance

    @Column(name = "template_text", length = 2000)
    private String templateText; // regex pattern

    public GenuineTemplate() {}
    public GenuineTemplate(String bankName, String type, String templateText) {
        this.bankName = bankName;
        this.type = type;
        this.templateText = templateText;
    }

    public Long getId() { return id; }
    public String getBankName() { return bankName; }
    public String getType() { return type; }
    public String getTemplateText() { return templateText; }
}
