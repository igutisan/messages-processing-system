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
            when(repository.findByDestinationPagedFiltered(DESTINATION, 0, 3, null))
                    .thenReturn(List.of(
                            buildMessage("1", "+57300A", MessageType.TEXT, null),
                            buildMessage("2", "+57300B", MessageType.TEXT, null),
                            buildMessage("3", "+57300C", MessageType.TEXT, null)));
            when(repository.countByDestinationFiltered(DESTINATION, null)).thenReturn(10L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 3, null);

            assertEquals(4, response.totalPages());
            assertEquals(10, response.totalElements());
        }

        @Test
        @DisplayName("Should mark last=true when on the last page")
        void shouldMarkLastTrueOnLastPage() {
            when(repository.findByDestinationPagedFiltered(DESTINATION, 2, 5, null))
                    .thenReturn(List.of(buildMessage("1", "+57300A", MessageType.TEXT, null)));
            when(repository.countByDestinationFiltered(DESTINATION, null)).thenReturn(11L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    2, 5, null);

            assertTrue(response.last(), "Should be last page (page 2 of 3, zero-indexed)");
        }

        @Test
        @DisplayName("Should mark last=false when NOT on the last page")
        void shouldMarkLastFalseWhenNotLastPage() {
            when(repository.findByDestinationPagedFiltered(DESTINATION, 0, 5, null))
                    .thenReturn(List.of(
                            buildMessage("1", "+57300A", MessageType.TEXT, null),
                            buildMessage("2", "+57300B", MessageType.TEXT, null),
                            buildMessage("3", "+57300C", MessageType.TEXT, null),
                            buildMessage("4", "+57300D", MessageType.TEXT, null),
                            buildMessage("5", "+57300E", MessageType.TEXT, null)));
            when(repository.countByDestinationFiltered(DESTINATION, null)).thenReturn(11L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 5, null);

            assertFalse(response.last(), "Should not be last page");
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

            when(repository.findByDestinationPagedFiltered(DESTINATION, 0, 10, null))
                    .thenReturn(List.of(message));
            when(repository.countByDestinationFiltered(DESTINATION, null)).thenReturn(1L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 10, null);

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

            when(repository.findByDestinationPagedFiltered(DESTINATION, 0, 10, null))
                    .thenReturn(List.of(message));
            when(repository.countByDestinationFiltered(DESTINATION, null)).thenReturn(1L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 10, null);

            ProcessedMessageResponseDto dto = response.content().get(0);
            assertEquals(errorMsg, dto.error());
        }

        @Test
        @DisplayName("Should map multiple messages preserving order from repository")
        void shouldMapMultipleMessagesPreservingOrder() {
            ProcessedMessage msg1 = buildMessage("first", "+57300A", MessageType.TEXT, null);
            ProcessedMessage msg2 = buildMessage("second", "+57300B", MessageType.VIDEO, null);
            ProcessedMessage msg3 = buildMessage("third", "+57300C", MessageType.DOCUMENT, "error");

            when(repository.findByDestinationPagedFiltered(DESTINATION, 0, 10, null))
                    .thenReturn(List.of(msg1, msg2, msg3));
            when(repository.countByDestinationFiltered(DESTINATION, null)).thenReturn(3L);

            PagedResponse<ProcessedMessageResponseDto> response = getMessagesByDestinationUseCase.execute(DESTINATION,
                    0, 10, null);

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
        @DisplayName("Should call findByDestinationPagedFiltered with correct parameters when success filter is null")
        void shouldCallFindWithNullFilter() {
            when(repository.findByDestinationPagedFiltered(DESTINATION, 2, 5, null))
                    .thenReturn(Collections.emptyList());
            when(repository.countByDestinationFiltered(DESTINATION, null)).thenReturn(0L);

            getMessagesByDestinationUseCase.execute(DESTINATION, 2, 5, null);

            verify(repository).findByDestinationPagedFiltered(DESTINATION, 2, 5, null);
            verify(repository).countByDestinationFiltered(DESTINATION, null);
        }

        @Test
        @DisplayName("Should call findByDestinationPagedFiltered with true when success filter is true")
        void shouldCallFindWithTrueFilter() {
            when(repository.findByDestinationPagedFiltered(DESTINATION, 0, 10, true))
                    .thenReturn(Collections.emptyList());
            when(repository.countByDestinationFiltered(DESTINATION, true)).thenReturn(0L);

            getMessagesByDestinationUseCase.execute(DESTINATION, 0, 10, true);

            verify(repository).findByDestinationPagedFiltered(DESTINATION, 0, 10, true);
            verify(repository).countByDestinationFiltered(DESTINATION, true);
        }

        @Test
        @DisplayName("Should call findByDestinationPagedFiltered with false when success filter is false")
        void shouldCallFindWithFalseFilter() {
            when(repository.findByDestinationPagedFiltered(DESTINATION, 1, 20, false))
                    .thenReturn(Collections.emptyList());
            when(repository.countByDestinationFiltered(DESTINATION, false)).thenReturn(0L);

            getMessagesByDestinationUseCase.execute(DESTINATION, 1, 20, false);

            verify(repository).findByDestinationPagedFiltered(DESTINATION, 1, 20, false);
            verify(repository).countByDestinationFiltered(DESTINATION, false);
        }
    }
}
