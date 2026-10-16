package com.smishguard.smishguard.entity;

import jakarta.persistence.*;

/** Known bank sender codes like HDFCBK, SBIINB, etc. */
@Entity
@Table(name = "bank_sender_ids")
public class BankSenderId {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "sender_code", nullable = false, unique = true)
    private String senderCode;

    public BankSenderId() {}
    public BankSenderId(String bankName, String senderCode) {
        this.bankName = bankName;
        this.senderCode = senderCode;
    }

    public Long getId() { return id; }
    public String getBankName() { return bankName; }
    public String getSenderCode() { return senderCode; }
}
