package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.ProcessedMessageResponseDto;
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

    public PagedResponse<ProcessedMessageResponseDto> execute(String destination, int page, int size, Boolean success) {
        List<ProcessedMessage> msgPage = repository.findByDestinationPagedFiltered(destination, page, size, success);
        long totalElements = repository.countByDestinationFiltered(destination, success);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isLast = (page + 1) >= totalPages;

        log.info("Retrieved {} messages for destination '{}' (Total in system: {}, Success filter: {})", msgPage.size(),
                destination,
                totalElements, success);

        List<ProcessedMessageResponseDto> dtos = msgPage.stream()
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

    private ProcessedMessageResponseDto toDto(ProcessedMessage message) {
        return ProcessedMessageResponseDto.builder()
                .id(message.getId())
                .origin(message.getOrigin())
                .content(message.getContent())
                .destination(message.getDestination())
                .messageType(message.getMessageType())
                .createdDate(message.getCreatedDate())
                .error(message.getError())
                .processingTime(message.getProcessingTime())
                .build();
    }
}
