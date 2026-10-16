package com.smishguard.smishguard.entity;

import jakarta.persistence.*;

/** Labelled example messages used as few-shot examples for the LLM. */
@Entity
@Table(name = "reference_messages")
public class ReferenceMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_text", length = 2000)
    private String messageText;

    @Column(name = "label") // "genuine" or "fake"
    private String label;

    @Column(name = "type")  // scam type or message type
    private String type;

    @Column(name = "bank")
    private String bank;

    @Column(name = "sender")
    private String sender;

    public ReferenceMessage() {}
    public ReferenceMessage(String messageText, String label, String type, String bank, String sender) {
        this.messageText = messageText;
        this.label = label;
        this.type = type;
        this.bank = bank;
        this.sender = sender;
    }

    public Long getId() { return id; }
    public String getMessageText() { return messageText; }
    public String getLabel() { return label; }
    public String getType() { return type; }
    public String getBankName() { return bank; }
    public String getSender() { return sender; }
}
