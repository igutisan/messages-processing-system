package com.prueba.tecnica.domain.model;

import java.time.Instant;
import java.util.UUID;

public class ProcessedMessage {

    private String id;
    private String origin;
    private String destination;
    private MessageType messageType;
    private String content;
    private Long processingTime;
    private Instant createdDate;
    private String error;

    public ProcessedMessage() {
    }

    public ProcessedMessage(String id, String origin, String destination,
            MessageType messageType, String content, Long processingTime,
            Instant createdDate, String error) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.messageType = messageType;
        this.content = content;
        this.processingTime = processingTime;
        this.createdDate = createdDate;
        this.error = error;
    }

    public static ProcessedMessage create(String origin, String destination,
            MessageType messageType, String content, Long processingTime, String error) {
        return new ProcessedMessage(
                UUID.randomUUID().toString(),
                origin,
                destination,
                messageType,
                content,
                processingTime,
                Instant.now(),
                error);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
