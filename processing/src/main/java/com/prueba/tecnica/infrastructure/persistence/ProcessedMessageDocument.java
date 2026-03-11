package com.prueba.tecnica.infrastructure.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "processed_messages")
public class ProcessedMessageDocument {

    @Id
    private String id;

    @Indexed
    private String origin;

    @Indexed
    private String destination;

    private String messageType;
    private String content;
    private Long processingTime;
    private Instant createdDate;
    private String error;
}
