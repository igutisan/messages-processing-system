package com.prueba.tecnica.application.dto;

import com.prueba.tecnica.domain.model.MessageType;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ProcessedMessageDto(
        String id,
        String content,
        String destination,
        MessageType messageType,
        Instant createdDate,
        String error,
        Long processingTime) {
}
