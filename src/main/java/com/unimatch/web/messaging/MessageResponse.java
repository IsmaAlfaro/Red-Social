package com.unimatch.web.messaging;

import com.unimatch.domain.ChatMessage;
import java.time.Instant;

public class MessageResponse {

    private final Long senderId;
    private final String content;
    private final Instant sentAt;

    public MessageResponse(ChatMessage message) {
        this.senderId = message.getSenderId();
        this.content = message.getContent();
        this.sentAt = message.getSentAt();
    }

    public Long getSenderId() { return senderId; }
    public String getContent() { return content; }
    public Instant getSentAt() { return sentAt; }
}
