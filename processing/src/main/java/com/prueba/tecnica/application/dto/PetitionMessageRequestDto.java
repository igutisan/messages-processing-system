package com.prueba.tecnica.application.dto;

public record PetitionMessageRequestDto(
        String origin,
        String destination,
        String messageType,
        String content) {
}
