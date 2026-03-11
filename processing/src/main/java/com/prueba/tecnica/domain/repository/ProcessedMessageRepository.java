package com.prueba.tecnica.domain.repository;

import java.time.Instant;
import java.util.List;

import com.prueba.tecnica.domain.model.ProcessedMessage;

public interface ProcessedMessageRepository {

    ProcessedMessage save(ProcessedMessage message);

    List<ProcessedMessage> findByDestination(String destination);

    List<ProcessedMessage> findByDestinationPaged(String destination, int page, int size);

    long countByDestination(String destination);

    long countSuccessfulByDestinationSince(String destination, Instant since);
}
