package com.prueba.tecnica.infrastructure.persistence;

import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Persistence adapter — implements the domain repository contract using Spring
 * Data MongoDB.
 */
@Component
@RequiredArgsConstructor
public class ProcessedMessageRepositoryAdapter implements ProcessedMessageRepository {

    private final MongoProcessedMessageRepository mongoRepository;

    @Override
    public ProcessedMessage save(ProcessedMessage message) {
        ProcessedMessageDocument document = ProcessedMessageMapper.toDocument(message);
        ProcessedMessageDocument saved = mongoRepository.save(document);
        return ProcessedMessageMapper.toDomain(saved);
    }
}
