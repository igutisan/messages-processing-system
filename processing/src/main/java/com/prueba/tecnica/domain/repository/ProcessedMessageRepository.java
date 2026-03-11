package com.prueba.tecnica.domain.repository;

import com.prueba.tecnica.domain.model.ProcessedMessage;

/**
 * Repository contract for ProcessedMessage persistence.
 * Implemented by the infrastructure persistence adapter.
 */
public interface ProcessedMessageRepository {

    ProcessedMessage save(ProcessedMessage message);
}
