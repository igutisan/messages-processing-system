package com.prueba.tecnica.domain.model;

import com.prueba.tecnica.domain.enums.MessageType;

public class Petition {

    private final String origin;
    private final String destination;
    private final MessageType messageType;
    private final String content;
    private final String receivedAt;

    public Petition(String origin, String destination, MessageType messageType, String content, String receivedAt) {
        this.origin = origin;
        this.destination = destination;
        this.messageType = messageType;
        this.content = content;
        this.receivedAt = receivedAt;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public String getReceivedAt() {
        return receivedAt;
    }
}
