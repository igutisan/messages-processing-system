package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.PetitionMessageDto;
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

        if (error == null) {
            error = checkMessageLimit(dto.destination());
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
        log.info("Message saved. id={}, processingTime={}ms, error={}", saved.getId(), processingTime,
                saved.getError());
        return saved;
    }

    private String checkMessageLimit(String destination) {
        Instant windowStart = Instant.now().minus(24, ChronoUnit.HOURS);
        long count = processedMessageRepository.countSuccessfulByDestinationSince(destination, windowStart);

        if (count >= MAX_MESSAGES_PER_DAY) {
            log.warn("Message limit exceeded for destination '{}'. Count in last 24h: {}/{}", destination, count,
                    MAX_MESSAGES_PER_DAY);
            return String.format(
                    "Message limit exceeded: destination '%s' has already received %d/%d messages in the last 24 hours.",
                    destination, count, MAX_MESSAGES_PER_DAY);
        }

        log.debug("Message limit check passed for destination '{}'. Count in last 24h: {}/{}", destination, count,
                MAX_MESSAGES_PER_DAY);
        return null;
    }
}
