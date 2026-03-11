package com.prueba.tecnica.infrastructure.rest;

import com.prueba.tecnica.application.dto.ProcessedMessageDto;
import com.prueba.tecnica.application.usecase.GetMessagesByDestinationUseCase;
import com.prueba.tecnica.infrastructure.rest.common.ApiResponse;
import com.prueba.tecnica.infrastructure.rest.common.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final GetMessagesByDestinationUseCase getMessagesByDestinationUseCase;

    @GetMapping("/destination/{destination}")
    public ResponseEntity<ApiResponse<PagedResponse<ProcessedMessageDto>>> getMessagesByDestination(
            @PathVariable String destination,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<ProcessedMessageDto> pagedMessages = getMessagesByDestinationUseCase.execute(destination, page,
                size);

        if (pagedMessages.totalElements() == 0) {
            return ResponseEntity.ok(
                    ApiResponse.success(pagedMessages, "No messages found for this destination"));
        }

        return ResponseEntity.ok(ApiResponse.success(pagedMessages));
    }
}
