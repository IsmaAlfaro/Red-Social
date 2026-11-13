package com.unimatch.domain;

import java.time.Instant;

public class ChatMessage {
    private Long id;
    private StudentProfile sender;
    private StudentProfile recipient;
    private String content;
    private Instant sentAt;

    protected ChatMessage() {
    }

    public ChatMessage(StudentProfile sender, StudentProfile recipient, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.sentAt = Instant.now();
    }

    public ChatMessage(Long id, StudentProfile sender, StudentProfile recipient, String content, Instant sentAt) {
        this(sender, recipient, content);
        this.id = id;
        this.sentAt = sentAt;
    }

    public Long getId() { return id; }
    public Long getRecipientId() { return recipient.getId(); }

    public Long getSenderId() { return sender.getId(); }
    public String getContent() { return content; }
    public Instant getSentAt() { return sentAt; }
}
