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
    public List<ProcessedMessage> findByDestinationPagedFiltered(String destination, int page, int size,
            Boolean success) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        List<ProcessedMessageDocument> documents;

        if (success == null) {
            documents = mongoRepository.findByDestination(destination, pageable);
        } else if (success) {
            documents = mongoRepository.findByDestinationAndErrorIsNull(destination, pageable);
        } else {
            documents = mongoRepository.findByDestinationAndErrorIsNotNull(destination, pageable);
        }

        return documents.stream()
                .map(ProcessedMessageMapper::toDomain)
                .toList();
    }

    @Override
    public long countByDestinationFiltered(String destination, Boolean success) {
        if (success == null) {
            return mongoRepository.countByDestination(destination);
        } else if (success) {
            return mongoRepository.countByDestinationAndErrorIsNull(destination);
        } else {
            return mongoRepository.countByDestinationAndErrorIsNotNull(destination);
        }
    }

    @Override
    public long countSuccessfulByDestinationSince(String destination, Instant since) {
        return mongoRepository.countByDestinationAndCreatedDateAfterAndErrorIsNull(destination, since);
    }
}
