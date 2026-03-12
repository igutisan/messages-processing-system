package com.prueba.tecnica.domain.gateway;

import com.prueba.tecnica.domain.model.Petition;

public interface PetitionMessageGateway {

    void publishPetition(Petition petition);
}
