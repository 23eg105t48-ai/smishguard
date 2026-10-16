package com.smishguard.smishguard.entity;

import jakarta.persistence.*;

/** Trusted domains for Indian banks (e.g. hdfcbank.com, sbi.co.in). */
@Entity
@Table(name = "trusted_domains")
public class TrustedDomain {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "domain", nullable = false, unique = true)
    private String domain;

    public TrustedDomain() {}
    public TrustedDomain(String bankName, String domain) {
        this.bankName = bankName;
        this.domain = domain;
    }

    public Long getId() { return id; }
    public String getBankName() { return bankName; }
    public String getDomain() { return domain; }
}
