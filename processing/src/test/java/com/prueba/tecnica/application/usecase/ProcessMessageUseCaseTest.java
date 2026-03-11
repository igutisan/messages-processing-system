package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.PetitionMessageRequestDto;
import com.prueba.tecnica.domain.model.MessageType;
import com.prueba.tecnica.domain.model.ProcessedMessage;
import com.prueba.tecnica.domain.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessMessageUseCase")
class ProcessMessageUseCaseTest {

        @Mock
        private ProcessedMessageRepository processedMessageRepository;

        @InjectMocks
        private ProcessMessageUseCase processMessageUseCase;

        private static final String ORIGIN = "+573001234567";
        private static final String DESTINATION = "+573007654321";
        private static final String CONTENT = "Hello world";
        private static final String RECEIVED_AT = Instant.now().toString();

        @Nested
        @DisplayName("Successful message processing")
        class SuccessfulProcessing {

                @Test
                @DisplayName("Should save a TEXT message with null error when within rate limit")
                void shouldSaveTextMessageWithNullError() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(0L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);

                        ProcessedMessage result = processMessageUseCase.process(dto, RECEIVED_AT);

                        assertNull(result.getError(), "Error should be null when within rate limit");
                        assertEquals(ORIGIN, result.getOrigin());
                        assertEquals(DESTINATION, result.getDestination());
                        assertEquals(MessageType.TEXT, result.getMessageType());
                        assertEquals(CONTENT, result.getContent());
                }

                @ParameterizedTest(name = "Should correctly parse messageType: {0}")
                @EnumSource(MessageType.class)
                @DisplayName("Should correctly parse all MessageType values from string")
                void shouldParseAllMessageTypes(MessageType type) {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(0L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, type.name(),
                                        CONTENT);

                        ProcessedMessage result = processMessageUseCase.process(dto, RECEIVED_AT);

                        assertEquals(type, result.getMessageType(),
                                        "MessageType should match the input string");
                }

                @Test
                @DisplayName("Should parse messageType case-insensitively")
                void shouldParseMessageTypeCaseInsensitively() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(0L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "text",
                                        CONTENT);

                        ProcessedMessage result = processMessageUseCase.process(dto, RECEIVED_AT);

                        assertEquals(MessageType.TEXT, result.getMessageType());
                }

                @Test
                @DisplayName("Should calculate processingTime as non-negative value")
                void shouldCalculateNonNegativeProcessingTime() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(0L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);
                        String recentReceivedAt = Instant.now().toString();

                        ProcessedMessage result = processMessageUseCase.process(dto, recentReceivedAt);

                        assertNotNull(result.getProcessingTime());
                        assertTrue(result.getProcessingTime() >= 0,
                                        "Processing time should be non-negative");
                }

                @Test
                @DisplayName("Should persist the message via repository save")
                void shouldCallRepositorySave() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(0L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);

                        processMessageUseCase.process(dto, RECEIVED_AT);

                        verify(processedMessageRepository, times(1)).save(any(ProcessedMessage.class));
                }

                @Test
                @DisplayName("Should return the saved message from repository")
                void shouldReturnSavedMessage() {
                        ProcessedMessage savedMessage = new ProcessedMessage(
                                        "saved-id", ORIGIN, DESTINATION, MessageType.TEXT, CONTENT, 10L, Instant.now(),
                                        null);

                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(0L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenReturn(savedMessage);

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);

                        ProcessedMessage result = processMessageUseCase.process(dto, RECEIVED_AT);

                        assertSame(savedMessage, result,
                                        "Should return exactly what the repository returned");
                }
        }

        @Nested
        @DisplayName("Rate limiting (MAX_MESSAGES_PER_DAY = 3)")
        class RateLimiting {

                @Test
                @DisplayName("Should set error message when destination has reached the limit (3)")
                void shouldSetErrorWhenLimitReached() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(3L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);

                        ProcessedMessage result = processMessageUseCase.process(dto, RECEIVED_AT);

                        assertNotNull(result.getError(), "Error should not be null when limit is reached");
                        assertTrue(result.getError().contains(DESTINATION),
                                        "Error message should mention the destination");
                        assertTrue(result.getError().contains("3"),
                                        "Error message should mention the limit number");
                }

                @Test
                @DisplayName("Should set error message when destination has exceeded the limit (>3)")
                void shouldSetErrorWhenLimitExceeded() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(5L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);

                        ProcessedMessage result = processMessageUseCase.process(dto, RECEIVED_AT);

                        assertNotNull(result.getError());
                }

                @Test
                @DisplayName("Should NOT set error when count is below the limit (2 of 3)")
                void shouldNotSetErrorWhenBelowLimit() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(2L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);

                        ProcessedMessage result = processMessageUseCase.process(dto, RECEIVED_AT);

                        assertNull(result.getError(),
                                        "Error should be null when count is below the limit");
                }

                @Test
                @DisplayName("Should still SAVE the message even when rate limit is exceeded")
                void shouldStillSaveWhenRateLimited() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(3L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);

                        processMessageUseCase.process(dto, RECEIVED_AT);

                        verify(processedMessageRepository).save(any(ProcessedMessage.class));
                }

                @Test
                @DisplayName("Should use a 24-hour window for rate limit check")
                void shouldUse24HourWindowForRateLimit() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(0L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);

                        Instant before = Instant.now().minusSeconds(1);
                        processMessageUseCase.process(dto, RECEIVED_AT);

                        ArgumentCaptor<Instant> windowCaptor = ArgumentCaptor.forClass(Instant.class);
                        verify(processedMessageRepository).countSuccessfulByDestinationSince(eq(DESTINATION),
                                        windowCaptor.capture());

                        Instant windowStart = windowCaptor.getValue();

                        Instant expected24hAgo = before.minusSeconds(24 * 60 * 60);
                        long diffSeconds = Math.abs(windowStart.getEpochSecond() - expected24hAgo.getEpochSecond());
                        assertTrue(diffSeconds < 5,
                                        "Rate limit window should start approximately 24 hours ago, but diff was "
                                                        + diffSeconds + "s");
                }

                @Test
                @DisplayName("Should allow 1st, 2nd, 3rd message, then BLOCK the 4th for the same destination")
                void shouldAllowUpToLimitThenBlock() {
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        // Simulate sequential calls: repo returns 0, 1, 2 (OK), then 3 (blocked)
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(0L, 1L, 2L, 3L);

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);
                        String receivedAt = Instant.now().toString();

                        ProcessedMessage result1 = processMessageUseCase.process(dto, receivedAt);
                        ProcessedMessage result2 = processMessageUseCase.process(dto, receivedAt);
                        ProcessedMessage result3 = processMessageUseCase.process(dto, receivedAt);
                        ProcessedMessage result4 = processMessageUseCase.process(dto, receivedAt);

                        assertNull(result1.getError(), "1st message should pass");
                        assertNull(result2.getError(), "2nd message should pass");
                        assertNull(result3.getError(), "3rd message should pass");
                        assertNotNull(result4.getError(), "4th message should be blocked");
                        assertTrue(result4.getError().contains(DESTINATION));
                }

                @Test
                @DisplayName("Should evaluate rate limit PER DESTINATION — blocking one should not affect another")
                void shouldEvaluateRateLimitPerDestination() {
                        String destinationA = "+573001111111";
                        String destinationB = "+573002222222";

                        // Destination A has reached the limit, destination B has not
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(destinationA),
                                        any(Instant.class)))
                                        .thenReturn(3L);
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(destinationB),
                                        any(Instant.class)))
                                        .thenReturn(1L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        String receivedAt = Instant.now().toString();

                        PetitionMessageRequestDto dtoA = new PetitionMessageRequestDto(ORIGIN, destinationA, "TEXT",
                                        CONTENT);
                        PetitionMessageRequestDto dtoB = new PetitionMessageRequestDto(ORIGIN, destinationB, "TEXT",
                                        CONTENT);

                        ProcessedMessage resultA = processMessageUseCase.process(dtoA, receivedAt);
                        ProcessedMessage resultB = processMessageUseCase.process(dtoB, receivedAt);

                        assertNotNull(resultA.getError(), "Destination A should be blocked (3 messages)");
                        assertNull(resultB.getError(), "Destination B should still be allowed (1 message)");
                }

                @Test
                @DisplayName("Should query the repository with a fresh 24h window on EACH call")
                void shouldQueryFresh24hWindowOnEachCall() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(eq(DESTINATION),
                                        any(Instant.class)))
                                        .thenReturn(0L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);
                        String receivedAt = Instant.now().toString();

                        processMessageUseCase.process(dto, receivedAt);
                        processMessageUseCase.process(dto, receivedAt);

                        ArgumentCaptor<Instant> windowCaptor = ArgumentCaptor.forClass(Instant.class);
                        verify(processedMessageRepository, times(2))
                                        .countSuccessfulByDestinationSince(eq(DESTINATION), windowCaptor.capture());

                        List<Instant> windows = windowCaptor.getAllValues();
                        assertEquals(2, windows.size(), "Should have queried the 24h window exactly twice");

                        // Both windows should be approximately 24h ago (within seconds of each other)
                        long diffBetweenWindows = Math.abs(
                                        windows.get(0).getEpochSecond() - windows.get(1).getEpochSecond());
                        assertTrue(diffBetweenWindows < 2,
                                        "Both calls should use a fresh 24h window from 'now', diff was "
                                                        + diffBetweenWindows + "s");
                }
        }

        @Nested
        @DisplayName("Data integrity of saved message")
        class DataIntegrity {

                @Test
                @DisplayName("Should map origin from DTO to ProcessedMessage")
                void shouldMapOrigin() {
                        when(processedMessageRepository.countSuccessfulByDestinationSince(anyString(),
                                        any(Instant.class)))
                                        .thenReturn(0L);
                        when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);

                        processMessageUseCase.process(dto, RECEIVED_AT);

                        ArgumentCaptor<ProcessedMessage> captor = ArgumentCaptor.forClass(ProcessedMessage.class);
                        verify(processedMessageRepository).save(captor.capture());

                        ProcessedMessage saved = captor.getValue();
                        assertEquals(ORIGIN, saved.getOrigin());
                        assertEquals(DESTINATION, saved.getDestination());
                        assertEquals(CONTENT, saved.getContent());
                        assertEquals(MessageType.TEXT, saved.getMessageType());
                }

                @Test
                @DisplayName("Should throw IllegalArgumentException for invalid messageType string")
                void shouldThrowForInvalidMessageType() {
                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION,
                                        "INVALID_TYPE", CONTENT);

                        assertThrows(IllegalArgumentException.class,
                                        () -> processMessageUseCase.process(dto, RECEIVED_AT));
                }
        }
}
