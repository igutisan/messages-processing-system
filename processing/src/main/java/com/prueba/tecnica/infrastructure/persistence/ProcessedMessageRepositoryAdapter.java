package com.prueba.tecnica.infrastructure.persistence;

import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

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

    @Override
    public List<ProcessedMessage> findByDestinationPaged(String destination, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return mongoRepository.findByDestination(destination, pageable).stream()
                .map(ProcessedMessageMapper::toDomain)
                .toList();
    }

    @Override
    public long countByDestination(String destination) {
        return mongoRepository.countByDestination(destination);
    }

    @Override
    public long countSuccessfulByDestinationSince(String destination, Instant since) {
        return mongoRepository.countByDestinationAndCreatedDateAfterAndErrorIsNull(destination, since);
    }
}
