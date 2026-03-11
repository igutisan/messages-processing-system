package com.prueba.tecnica.domain.repository;

import java.util.List;

import com.prueba.tecnica.domain.model.ProcessedMessage;

public interface ProcessedMessageRepository {

    ProcessedMessage save(ProcessedMessage message);
    List<ProcessedMessage> findByRecipient(String recipient);
}
