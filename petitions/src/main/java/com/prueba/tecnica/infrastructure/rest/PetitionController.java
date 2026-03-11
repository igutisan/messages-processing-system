package com.prueba.tecnica.infrastructure.rest;

import com.prueba.tecnica.application.dto.CreatePetitionRequestDto;
import com.prueba.tecnica.application.usecase.PetitionUseCase;
import com.prueba.tecnica.infrastructure.rest.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/petitions")
@RequiredArgsConstructor
public class PetitionController {

    private final PetitionUseCase petitionUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody CreatePetitionRequestDto request) {
        petitionUseCase.processPetition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(null, "Petition processed successfully", HttpStatus.CREATED.value()));
    }
}
