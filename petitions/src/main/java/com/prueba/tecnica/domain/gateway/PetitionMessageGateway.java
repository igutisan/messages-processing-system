package com.prueba.tecnica.domain.gateway;

import com.prueba.tecnica.application.dto.CreatePetitionRequestDto;

public interface PetitionMessageGateway {

    void publishPetition(CreatePetitionRequestDto request, String receivedAt);
}
