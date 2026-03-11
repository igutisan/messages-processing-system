package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.ProcessedMessageDto;
import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMessagesByDestinationUseCase {

    private final ProcessedMessageRepository repository;

    public List<ProcessedMessageDto> execute(String recipient) {
        return repository.findByRecipient(recipient).stream()
                .map(this::toDto)
                .toList();
    }

    private ProcessedMessageDto toDto(ProcessedMessage message) {
        return ProcessedMessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .destination(message.getDestination())
                .messageType(message.getMessageType())
                .createdDate(message.getCreatedDate())
                .error(message.getError())
                .processingTime(message.getProcessingTime())
                .build();
    }
}
