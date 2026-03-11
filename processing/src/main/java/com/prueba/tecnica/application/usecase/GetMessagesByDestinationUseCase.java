package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.ProcessedMessageDto;
import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import com.prueba.tecnica.infrastructure.rest.common.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetMessagesByDestinationUseCase {

    private final ProcessedMessageRepository repository;

    public PagedResponse<ProcessedMessageDto> execute(String destination, int page, int size) {
        List<ProcessedMessage> msgPage = repository.findByDestinationPaged(destination, page, size);
        long totalElements = repository.countByDestination(destination);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = page >= (totalPages - 1);

        log.info("Retrieved {} messages for destination '{}' (Total in system: {})", msgPage.size(), destination,
                totalElements);

        List<ProcessedMessageDto> dtos = msgPage.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                dtos,
                page,
                size,
                totalElements,
                totalPages,
                isLast);
    }

    private ProcessedMessageDto toDto(ProcessedMessage message) {
        return ProcessedMessageDto.builder()
                .id(message.getId())
                .origin(message.getOrigin())
                .destination(message.getDestination())
                .messageType(message.getMessageType())
                .createdDate(message.getCreatedDate())
                .error(message.getError())
                .processingTime(message.getProcessingTime())
                .build();
    }
}
