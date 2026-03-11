package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.PetitionMessageDto;
import com.prueba.tecnica.domain.model.MessageType;
import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessMessageUseCase {

    private final ProcessedMessageRepository processedMessageRepository;

    public ProcessedMessage process(PetitionMessageDto dto, String receivedAt) {

        long startMillis = Instant.parse(receivedAt).toEpochMilli();
        String error = null;
        MessageType messageType;

        try {
            messageType = MessageType.valueOf(dto.messageType().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown messageType received: '{}'", dto.messageType());
            messageType = MessageType.TEXT;
            error = "Unsupported messageType: " + dto.messageType();
        }

        long processingTime = System.currentTimeMillis() - startMillis;

        ProcessedMessage processedMessage = ProcessedMessage.create(
                dto.origin(),
                dto.destination(),
                messageType,
                dto.content(),
                processingTime,
                error);

        ProcessedMessage saved = processedMessageRepository.save(processedMessage);
        log.info("Message processed and saved. id={}, processingTime={}ms", saved.getId(), processingTime);
        return saved;
    }
}
