package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.CreatePetitionRequestDto;
import com.prueba.tecnica.domain.enums.MessageType;
import com.prueba.tecnica.domain.exception.DomainException;
import com.prueba.tecnica.domain.exception.OriginNotFoundException;
import com.prueba.tecnica.domain.gateway.PetitionMessageGateway;
import com.prueba.tecnica.domain.repository.OriginLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PetitionUseCase {

    private final OriginLineRepository originLineRepository;
    private final PetitionMessageGateway petitionMessageGateway;

    public void processPetition(CreatePetitionRequestDto request) {
        String receivedAt = Instant.now().toString();

        validateOriginExists(request.origin());
        validateMultimediaContent(request);

        petitionMessageGateway.publishPetition(request, receivedAt);
    }

    private void validateOriginExists(String origin) {
        if (!originLineRepository.existPhoneNumber(origin)) {
            throw new OriginNotFoundException(origin);
        }
    }

    private void validateMultimediaContent(CreatePetitionRequestDto request) {
        boolean isMultimedia = request.messageType() == MessageType.IMAGE
                || request.messageType() == MessageType.VIDEO
                || request.messageType() == MessageType.DOCUMENT;

        if (!isMultimedia)
            return;

        try {
            URI uri = URI.create(request.content());
            String scheme = uri.getScheme();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                throw new DomainException(
                        "content must be a valid http/https URL."
                                .formatted(request.messageType()));
            }
        } catch (IllegalArgumentException e) {
            throw new DomainException(
                    "content must be a valid URL. Received: '%s'."
                            .formatted(request.messageType(), request.content()));
        }
    }
}
