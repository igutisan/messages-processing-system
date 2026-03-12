package com.prueba.tecnica.domain.repository;

import java.time.Instant;
import java.util.List;

import com.prueba.tecnica.domain.model.ProcessedMessage;

public interface ProcessedMessageRepository {

    ProcessedMessage save(ProcessedMessage message);

    List<ProcessedMessage> findByDestinationPagedFiltered(String destination, int page, int size, Boolean success);

    long countByDestinationFiltered(String destination, Boolean success);

    long countSuccessfulByDestinationSince(String destination, Instant since);
}
