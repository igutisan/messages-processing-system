package com.prueba.tecnica.infrastructure.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "destination_counters")
public class DestinationCounterDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String destination;

    private int count;

    private Instant windowStart;
}
