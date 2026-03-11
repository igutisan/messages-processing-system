package com.prueba.tecnica.infrastructure.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for ProcessedMessageDocument.
 */
@Repository
public interface MongoProcessedMessageRepository extends MongoRepository<ProcessedMessageDocument, String> {
}
