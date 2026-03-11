package com.prueba.tecnica.application.dto;

import com.prueba.tecnica.domain.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePetitionRequestDto(

        @NotBlank(message = "Origin is required") String origin,

        @NotBlank(message = "Destination is required") String destination,

        @NotNull(message = "Message type is required") MessageType messageType,

        @NotBlank(message = "Content is required") String content) {
}
