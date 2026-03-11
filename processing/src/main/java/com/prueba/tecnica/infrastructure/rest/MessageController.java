package com.prueba.tecnica.infrastructure.rest;

import com.prueba.tecnica.application.dto.ProcessedMessageDto;
import com.prueba.tecnica.application.usecase.GetMessagesByDestinationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final GetMessagesByDestinationUseCase getMessagesByDestinationUseCase;

    @GetMapping("/destination/{destination}")
    public ResponseEntity<List<ProcessedMessageDto>> getMessagesByDestination(
            @PathVariable String destination) {

        List<ProcessedMessageDto> messages = getMessagesByDestinationUseCase.execute(destination);

        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(messages);
    }
}
