package com.prueba.tecnica.infrastructure.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document mapped to the "processed_messages" collection.
 */
@Document(collection = "processed_messages")
public class ProcessedMessageDocument {

    @Id
    private String id;

    @Indexed
    private String origin;

    @Indexed
    private String destination;

    private String messageType;
    private String content;
    private Long processingTime;
    private Instant createdDate;
    private String error;

    public ProcessedMessageDocument() {
    }

    public ProcessedMessageDocument(String id, String origin, String destination,
            String messageType, String content, Long processingTime,
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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
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
