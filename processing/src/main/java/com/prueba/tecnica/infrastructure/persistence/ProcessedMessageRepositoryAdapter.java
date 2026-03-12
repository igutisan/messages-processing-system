package com.prueba.tecnica.infrastructure.persistence;

import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessedMessageRepositoryAdapter implements ProcessedMessageRepository {

    private final MongoProcessedMessageRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

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
    public boolean incrementMessageCountIfAllowed(String destination, Instant windowStart, int max) {
        Query query = new Query(
                Criteria.where("destination").is(destination)
                        .and("count").lt(max)
                        .and("windowStart").gte(windowStart));

        Update increment = new Update().inc("count", 1);

        DestinationCounterDocument result = mongoTemplate.findAndModify(
                query,
                increment,
                FindAndModifyOptions.options().returnNew(true),
                DestinationCounterDocument.class);

        if (result != null) {
            return true;
        }

        Query createQuery = new Query(Criteria.where("destination").is(destination));
        Update upsert = new Update()
                .setOnInsert("destination", destination)
                .setOnInsert("windowStart", windowStart)
                .setOnInsert("count", 1);

        DestinationCounterDocument created = mongoTemplate.findAndModify(
                createQuery,
                upsert,
                FindAndModifyOptions.options().upsert(true).returnNew(true),
                DestinationCounterDocument.class);

        return created != null && created.getCount() <= max;
    }
}
