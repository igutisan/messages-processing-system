package com.prueba.tecnica.infrastructure.persistence;

import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    public boolean incrementMessageCountIfAllowed(String destination, Instant now, int max) {
        Instant windowThreshold = now.minus(24, ChronoUnit.HOURS);

        Query withinLimitQuery = new Query(
                Criteria.where("destination").is(destination)
                        .and("count").lt(max)
                        .and("windowStart").gte(windowThreshold));

        DestinationCounterDocument incremented = mongoTemplate.findAndModify(
                withinLimitQuery,
                new Update().inc("count", 1),
                FindAndModifyOptions.options().returnNew(true),
                DestinationCounterDocument.class);

        if (incremented != null) {
            return true;
        }

        Query expiredWindowQuery = new Query(
                Criteria.where("destination").is(destination)
                        .and("windowStart").lt(windowThreshold));

        DestinationCounterDocument reset = mongoTemplate.findAndModify(
                expiredWindowQuery,
                new Update().set("count", 1).set("windowStart", now),
                FindAndModifyOptions.options().returnNew(false),
                DestinationCounterDocument.class);

        if (reset != null) {
            return true;
        }

        Query newDestQuery = new Query(Criteria.where("destination").is(destination));
        Update insertOnly = new Update()
                .setOnInsert("destination", destination)
                .setOnInsert("count", 1)
                .setOnInsert("windowStart", now);

        DestinationCounterDocument created = mongoTemplate.findAndModify(
                newDestQuery,
                insertOnly,
                FindAndModifyOptions.options().upsert(true).returnNew(true),
                DestinationCounterDocument.class);

        return created != null && created.getCount() == 1;
    }
}
