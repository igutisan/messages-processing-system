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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

        private void givenSlotAvailable() {
                when(processedMessageRepository.incrementMessageCountIfAllowed(anyString(), any(Instant.class),
                                anyInt()))
                                .thenReturn(true);
        }

        private void givenSlotExhausted() {
                when(processedMessageRepository.incrementMessageCountIfAllowed(anyString(), any(Instant.class),
                                anyInt()))
                                .thenReturn(false);
        }

        private void givenSaveReturnsInput() {
                when(processedMessageRepository.save(any(ProcessedMessage.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
        }

        @Nested
        @DisplayName("Successful message processing")
        class SuccessfulProcessing {

                @Test
                @DisplayName("Should save a TEXT message with null error when within rate limit")
                void shouldSaveTextMessageWithNullError() {
                        givenSlotAvailable();
                        givenSaveReturnsInput();

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
                        givenSlotAvailable();
                        givenSaveReturnsInput();

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, type.name(),
                                        CONTENT);
                        ProcessedMessage result = processMessageUseCase.process(dto, RECEIVED_AT);

                        assertEquals(type, result.getMessageType());
                }

                @Test
                @DisplayName("Should parse messageType case-insensitively")
                void shouldParseMessageTypeCaseInsensitively() {
                        givenSlotAvailable();
                        givenSaveReturnsInput();

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "text",
                                        CONTENT);
                        ProcessedMessage result = processMessageUseCase.process(dto, RECEIVED_AT);

                        assertEquals(MessageType.TEXT, result.getMessageType());
                }

                @Test
                @DisplayName("Should calculate processingTime as non-negative value")
                void shouldCalculateNonNegativeProcessingTime() {
                        givenSlotAvailable();
                        givenSaveReturnsInput();

                        PetitionMessageRequestDto dto = new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT",
                                        CONTENT);
                        ProcessedMessage result = processMessageUseCase.process(dto, Instant.now().toString());

                        assertNotNull(result.getProcessingTime());
                        assertTrue(result.getProcessingTime() >= 0);
                }

                @Test
                @DisplayName("Should return the saved message from repository")
                void shouldReturnSavedMessage() {
                        ProcessedMessage savedMessage = new ProcessedMessage(
                                        "saved-id", ORIGIN, DESTINATION, MessageType.TEXT, CONTENT, 10L, Instant.now(),
                                        null);

                        when(processedMessageRepository.incrementMessageCountIfAllowed(anyString(), any(Instant.class),
                                        anyInt()))
                                        .thenReturn(true);
                        when(processedMessageRepository.save(any(ProcessedMessage.class))).thenReturn(savedMessage);

                        ProcessedMessage result = processMessageUseCase.process(
                                        new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT", CONTENT),
                                        RECEIVED_AT);

                        assertSame(savedMessage, result);
                }
        }

        @Nested
        @DisplayName("Rate limiting (MAX_MESSAGES_PER_DAY = 3)")
        class RateLimiting {

                @Test
                @DisplayName("Should set error message when slot is not acquired (limit reached)")
                void shouldSetErrorWhenLimitReached() {
                        givenSlotExhausted();
                        givenSaveReturnsInput();

                        ProcessedMessage result = processMessageUseCase.process(
                                        new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT", CONTENT),
                                        RECEIVED_AT);

                        assertNotNull(result.getError());
                        assertTrue(result.getError().contains(DESTINATION));
                        assertTrue(result.getError().contains("3"));
                }

                @Test
                @DisplayName("Should still SAVE the message even when rate limit is exceeded")
                void shouldStillSaveWhenRateLimited() {
                        givenSlotExhausted();
                        givenSaveReturnsInput();

                        processMessageUseCase.process(
                                        new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT", CONTENT),
                                        RECEIVED_AT);

                        verify(processedMessageRepository).save(any(ProcessedMessage.class));
                }

                @Test
                @DisplayName("Should call incrementMessageCountIfAllowed with approximately the current time")
                void shouldUse24HourWindowForRateLimit() {
                        givenSlotAvailable();
                        givenSaveReturnsInput();

                        Instant before = Instant.now();
                        processMessageUseCase.process(
                                        new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT", CONTENT),
                                        RECEIVED_AT);

                        ArgumentCaptor<Instant> windowCaptor = ArgumentCaptor.forClass(Instant.class);
                        verify(processedMessageRepository).incrementMessageCountIfAllowed(eq(DESTINATION),
                                        windowCaptor.capture(),
                                        eq(3));

                        Instant capturedNow = windowCaptor.getValue();
                        long diffSeconds = Math.abs(capturedNow.getEpochSecond() - before.getEpochSecond());
                        assertTrue(diffSeconds < 5, "Captured instant should be ~now, diff was " + diffSeconds + "s");
                }

                @Test
                @DisplayName("Should evaluate rate limit per destination independently")
                void shouldEvaluateRateLimitPerDestination() {
                        String destinationA = "+573001111111";
                        String destinationB = "+573002222222";

                        when(processedMessageRepository.incrementMessageCountIfAllowed(eq(destinationA),
                                        any(Instant.class), anyInt()))
                                        .thenReturn(false);
                        when(processedMessageRepository.incrementMessageCountIfAllowed(eq(destinationB),
                                        any(Instant.class), anyInt()))
                                        .thenReturn(true);
                        givenSaveReturnsInput();

                        ProcessedMessage resultA = processMessageUseCase.process(
                                        new PetitionMessageRequestDto(ORIGIN, destinationA, "TEXT", CONTENT),
                                        RECEIVED_AT);
                        ProcessedMessage resultB = processMessageUseCase.process(
                                        new PetitionMessageRequestDto(ORIGIN, destinationB, "TEXT", CONTENT),
                                        RECEIVED_AT);

                        assertNotNull(resultA.getError(), "Destination A should be blocked");
                        assertNull(resultB.getError(), "Destination B should be allowed");
                }

                @Test
                @DisplayName("Should call incrementMessageCountIfAllowed with MAX=3")
                void shouldCallTryAcquireSlotWithCorrectMax() {
                        givenSlotAvailable();
                        givenSaveReturnsInput();

                        processMessageUseCase.process(
                                        new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT", CONTENT),
                                        RECEIVED_AT);

                        verify(processedMessageRepository).incrementMessageCountIfAllowed(eq(DESTINATION),
                                        any(Instant.class), eq(3));
                }
        }

        @Nested
        @DisplayName("Data integrity of saved message")
        class DataIntegrity {

                @Test
                @DisplayName("Should map all fields from DTO to ProcessedMessage")
                void shouldMapAllFieldsFromDto() {
                        givenSlotAvailable();
                        givenSaveReturnsInput();

                        processMessageUseCase.process(
                                        new PetitionMessageRequestDto(ORIGIN, DESTINATION, "TEXT", CONTENT),
                                        RECEIVED_AT);

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
                        assertThrows(IllegalArgumentException.class,
                                        () -> processMessageUseCase.process(
                                                        new PetitionMessageRequestDto(ORIGIN, DESTINATION,
                                                                        "INVALID_TYPE", CONTENT),
                                                        RECEIVED_AT));
                }
        }
}
