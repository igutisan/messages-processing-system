package com.prueba.tecnica.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoProcessedMessageRepository extends MongoRepository<ProcessedMessageDocument, String> {

    List<ProcessedMessageDocument> findByDestination(String destination, Pageable pageable);

    long countByDestination(String destination);

    List<ProcessedMessageDocument> findByDestinationAndErrorIsNull(String destination, Pageable pageable);

    List<ProcessedMessageDocument> findByDestinationAndErrorIsNotNull(String destination, Pageable pageable);

    long countByDestinationAndErrorIsNull(String destination);

    long countByDestinationAndErrorIsNotNull(String destination);
}
