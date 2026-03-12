package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.CreatePetitionRequestDto;
import com.prueba.tecnica.domain.enums.MessageType;
import com.prueba.tecnica.domain.exception.DomainException;
import com.prueba.tecnica.domain.exception.OriginNotFoundException;
import com.prueba.tecnica.domain.gateway.PetitionMessageGateway;
import com.prueba.tecnica.domain.model.Petition;
import com.prueba.tecnica.domain.repository.OriginLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetitionUseCase {

    private final OriginLineRepository originLineRepository;
    private final PetitionMessageGateway petitionMessageGateway;

    public void processPetition(CreatePetitionRequestDto request) {
        log.info("Receiving new petition from '{}' to '{}' [type: {}]", request.origin(), request.destination(),
                request.messageType());

        String receivedAt = Instant.now().toString();

        validateOriginExists(request.origin());
        validateMultimediaContent(request);

        Petition petition = new Petition(
                request.origin(),
                request.destination(),
                request.messageType(),
                request.content(),
                receivedAt);

        log.info("Validation successful. Forwarding petition to message broker.");
        petitionMessageGateway.publishPetition(petition);
    }

    private void validateOriginExists(String origin) {
        if (!originLineRepository.existPhoneNumber(origin)) {
            log.warn("Petition rejected: Origin phone number '{}' is not registered in the system", origin);
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
                log.warn("Petition rejected: Multimedia content does not have a valid http/https scheme");
                throw new DomainException(
                        "content must be a valid http/https URL."
                                .formatted(request.messageType()));
            }
            log.info("Multimedia content URL validated successfully");
        } catch (IllegalArgumentException e) {
            log.warn("Petition rejected: Multimedia content is not a valid URI format");
            throw new DomainException(
                    "content must be a valid URL. Received: '%s'."
                            .formatted(request.messageType(), request.content()));
        }
    }
}
