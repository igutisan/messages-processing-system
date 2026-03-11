package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.ProcessedMessageDto;
import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import com.prueba.tecnica.infrastructure.rest.common.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMessagesByDestinationUseCase {

    private final ProcessedMessageRepository repository;

    public List<ProcessedMessageDto> execute(String destination) {
        return repository.findByDestination(destination).stream()
                .map(this::toDto)
                .toList();
    }

    public PagedResponse<ProcessedMessageDto> execute(String destination, int page, int size) {
        List<ProcessedMessageDto> content = repository.findByDestinationPaged(destination, page, size)
                .stream()
                .map(this::toDto)
                .toList();

        long totalElements = repository.countByDestination(destination);
        return PagedResponse.of(content, page, size, totalElements);
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
