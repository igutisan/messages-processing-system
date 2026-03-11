package com.prueba.tecnica.application.dto;

public record PetitionMessageDto(
        String origin,
        String destination,
        String messageType,
        String content) {
}
