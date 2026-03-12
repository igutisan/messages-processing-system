package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.PetitionMessageRequestDto;
import com.prueba.tecnica.domain.model.MessageType;
import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessMessageUseCase {

    private static final int MAX_MESSAGES_PER_DAY = 3;

    private final ProcessedMessageRepository processedMessageRepository;

    public ProcessedMessage process(PetitionMessageRequestDto dto, String receivedAt) {
        long startMillis = Instant.parse(receivedAt).toEpochMilli();
        MessageType messageType = MessageType.valueOf(dto.messageType().toUpperCase());

        Instant windowStart = Instant.now().minus(24, ChronoUnit.HOURS);
        boolean acquired = processedMessageRepository.incrementMessageCountIfAllowed(dto.destination(), windowStart,
                MAX_MESSAGES_PER_DAY);

        String error = acquired ? null
                : String.format(
                        "Message limit exceeded: destination '%s' has already received %d/%d messages in the last 24 hours.",
                        dto.destination(), MAX_MESSAGES_PER_DAY, MAX_MESSAGES_PER_DAY);

        long processingTime = System.currentTimeMillis() - startMillis;

        ProcessedMessage processedMessage = new ProcessedMessage(
                dto.origin(),
                dto.destination(),
                messageType,
                dto.content(),
                processingTime,
                error);

        ProcessedMessage saved = processedMessageRepository.save(processedMessage);
        log.info("Message saved. id={}, processingTime={}ms, error={}", saved.getId(), processingTime,
                saved.getError());
        return saved;
    }
}
