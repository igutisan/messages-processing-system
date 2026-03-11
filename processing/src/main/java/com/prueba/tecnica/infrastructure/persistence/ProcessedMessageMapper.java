package com.prueba.tecnica.infrastructure.persistence;

import com.prueba.tecnica.domain.model.MessageType;
import com.prueba.tecnica.domain.model.ProcessedMessage;

/**
 * Maps between domain model (ProcessedMessage) and MongoDB document
 * (ProcessedMessageDocument).
 */
public class ProcessedMessageMapper {

    private ProcessedMessageMapper() {
        // Utility class
    }

    public static ProcessedMessageDocument toDocument(ProcessedMessage message) {
        return new ProcessedMessageDocument(
                message.getId(),
                message.getOrigin(),
                message.getDestination(),
                message.getMessageType().name(),
                message.getContent(),
                message.getProcessingTime(),
                message.getCreatedDate(),
                message.getError());
    }

    public static ProcessedMessage toDomain(ProcessedMessageDocument doc) {
        return new ProcessedMessage(
                doc.getId(),
                doc.getOrigin(),
                doc.getDestination(),
                MessageType.valueOf(doc.getMessageType()),
                doc.getContent(),
                doc.getProcessingTime(),
                doc.getCreatedDate(),
                doc.getError());
    }
}
