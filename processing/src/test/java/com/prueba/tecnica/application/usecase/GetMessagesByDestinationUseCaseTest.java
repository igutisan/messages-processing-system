package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.ProcessedMessageResponseDto;
import com.prueba.tecnica.domain.model.MessageType;
import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import com.prueba.tecnica.infrastructure.rest.common.PagedResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMessagesByDestinationUseCase")
class GetMessagesByDestinationUseCaseTest {

    @Mock
    private ProcessedMessageRepository repository;

    @InjectMocks
    private GetMessagesByDestinationUseCase getMessagesByDestinationUseCase;

    private static final String DESTINATION = "+573007654321";

    private ProcessedMessage buildMessage(String id, String origin, MessageType type, String error) {
        return new ProcessedMessage(id, origin, DESTINATION, type, "content", 15L, Instant.now(), error);
    }

    @Nested
    @DisplayName("Pagination logic")
    class PaginationLogic {

        @Test
        @DisplayName("Should calculate totalPages correctly (10 elements / size 3 = 4 pages)")
        void shouldCalculateTotalPagesCorrectly() {
            when(repository.findByDestinationPaged(DESTINATION, 0, 3))
                    .thenReturn(List.of(
                            buildMessage("1", "+57300A", MessageType.TEXT, null),
                            buildMessage("2", "+57300B", MessageType.TEXT, null),
                            buildMessage("3", "+57300C", MessageType.TEXT, null)));
            when(repository.countByDestination(DESTINATION)).thenReturn(10L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 3);

            assertEquals(4, response.totalPages());
            assertEquals(10, response.totalElements());
        }

        @Test
        @DisplayName("Should mark last=true when on the last page")
        void shouldMarkLastTrueOnLastPage() {
            when(repository.findByDestinationPaged(DESTINATION, 2, 5))
                    .thenReturn(List.of(buildMessage("1", "+57300A", MessageType.TEXT, null)));
            when(repository.countByDestination(DESTINATION)).thenReturn(11L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    2, 5);

            assertTrue(response.last(), "Should be last page (page 2 of 3, zero-indexed)");
        }

        @Test
        @DisplayName("Should mark last=false when NOT on the last page")
        void shouldMarkLastFalseWhenNotLastPage() {
            when(repository.findByDestinationPaged(DESTINATION, 0, 5))
                    .thenReturn(List.of(
                            buildMessage("1", "+57300A", MessageType.TEXT, null),
                            buildMessage("2", "+57300B", MessageType.TEXT, null),
                            buildMessage("3", "+57300C", MessageType.TEXT, null),
                            buildMessage("4", "+57300D", MessageType.TEXT, null),
                            buildMessage("5", "+57300E", MessageType.TEXT, null)));
            when(repository.countByDestination(DESTINATION)).thenReturn(11L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 5);

            assertFalse(response.last(), "Should not be last page");
        }

        @Test
        @DisplayName("Should set page and size in the response correctly")
        void shouldSetPageAndSizeCorrectly() {
            when(repository.findByDestinationPaged(DESTINATION, 1, 10))
                    .thenReturn(Collections.emptyList());
            when(repository.countByDestination(DESTINATION)).thenReturn(5L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    1, 10);

            assertEquals(1, response.page());
            assertEquals(10, response.size());
        }

        @Test
        @DisplayName("Should return empty content list when no messages for destination")
        void shouldReturnEmptyContentWhenNoMessages() {
            when(repository.findByDestinationPaged(DESTINATION, 0, 10))
                    .thenReturn(Collections.emptyList());
            when(repository.countByDestination(DESTINATION)).thenReturn(0L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 10);

            assertTrue(response.content().isEmpty());
            assertEquals(0, response.totalElements());
            assertTrue(response.last());
        }

        @Test
        @DisplayName("Should return totalPages=1 when all elements fit in one page")
        void shouldReturnSinglePageWhenAllFit() {
            when(repository.findByDestinationPaged(DESTINATION, 0, 10))
                    .thenReturn(List.of(buildMessage("1", "+57300A", MessageType.TEXT, null)));
            when(repository.countByDestination(DESTINATION)).thenReturn(1L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 10);

            assertEquals(1, response.totalPages());
            assertTrue(response.last());
        }
    }

    @Nested
    @DisplayName("DTO mapping from ProcessedMessage to ProcessedMessageDto")
    class DtoMapping {

        @Test
        @DisplayName("Should map all fields correctly from domain model to DTO")
        void shouldMapAllFieldsCorrectly() {
            Instant createdDate = Instant.parse("2026-03-11T10:00:00Z");
            ProcessedMessage message = new ProcessedMessage(
                    "msg-123", "+57300ORIGIN", DESTINATION, MessageType.IMAGE, "https://img.com/pic.png",
                    42L, createdDate, null);

            when(repository.findByDestinationPaged(DESTINATION, 0, 10))
                    .thenReturn(List.of(message));
            when(repository.countByDestination(DESTINATION)).thenReturn(1L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 10);

            assertEquals(1, response.content().size());

            ProcessedMessageResponseDto dto = response.content().get(0);
            assertEquals("msg-123", dto.id());
            assertEquals("+57300ORIGIN", dto.origin());
            assertEquals("https://img.com/pic.png", dto.content());
            assertEquals(DESTINATION, dto.destination());
            assertEquals(MessageType.IMAGE, dto.messageType());
            assertEquals(createdDate, dto.createdDate());
            assertNull(dto.error());
            assertEquals(42L, dto.processingTime());
        }

        @Test
        @DisplayName("Should preserve error field in DTO when message has an error")
        void shouldPreserveErrorInDto() {
            String errorMsg = "Message limit exceeded";
            ProcessedMessage message = new ProcessedMessage(
                    "msg-err", "+57300ORIGIN", DESTINATION, MessageType.TEXT, "hello",
                    10L, Instant.now(), errorMsg);

            when(repository.findByDestinationPaged(DESTINATION, 0, 10))
                    .thenReturn(List.of(message));
            when(repository.countByDestination(DESTINATION)).thenReturn(1L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 10);

            ProcessedMessageResponseDto dto = response.content().get(0);
            assertEquals(errorMsg, dto.error());
        }

        @Test
        @DisplayName("Should map multiple messages preserving order from repository")
        void shouldMapMultipleMessagesPreservingOrder() {
            ProcessedMessage msg1 = buildMessage("first", "+57300A", MessageType.TEXT, null);
            ProcessedMessage msg2 = buildMessage("second", "+57300B", MessageType.VIDEO, null);
            ProcessedMessage msg3 = buildMessage("third", "+57300C", MessageType.DOCUMENT, "error");

            when(repository.findByDestinationPaged(DESTINATION, 0, 10))
                    .thenReturn(List.of(msg1, msg2, msg3));
            when(repository.countByDestination(DESTINATION)).thenReturn(3L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 10);

            assertEquals(3, response.content().size());
            assertEquals("first", response.content().get(0).id());
            assertEquals("second", response.content().get(1).id());
            assertEquals("third", response.content().get(2).id());
            assertEquals(MessageType.TEXT, response.content().get(0).messageType());
            assertEquals(MessageType.VIDEO, response.content().get(1).messageType());
            assertEquals(MessageType.DOCUMENT, response.content().get(2).messageType());
        }
    }

    @Nested
    @DisplayName("Repository interaction")
    class RepositoryInteraction {

        @Test
        @DisplayName("Should call findByDestinationPaged with correct parameters")
        void shouldCallFindWithCorrectParams() {
            when(repository.findByDestinationPaged(DESTINATION, 2, 5))
                    .thenReturn(Collections.emptyList());
            when(repository.countByDestination(DESTINATION)).thenReturn(0L);

            getMessagesByDestinationUseCase.execute(DESTINATION, 2, 5);

            verify(repository).findByDestinationPaged(DESTINATION, 2, 5);
        }

        @Test
        @DisplayName("Should call countByDestination with the destination parameter")
        void shouldCallCountWithDestination() {
            when(repository.findByDestinationPaged(DESTINATION, 0, 10))
                    .thenReturn(Collections.emptyList());
            when(repository.countByDestination(DESTINATION)).thenReturn(0L);

            getMessagesByDestinationUseCase.execute(DESTINATION, 0, 10);

            verify(repository).countByDestination(DESTINATION);
        }
    }
}
