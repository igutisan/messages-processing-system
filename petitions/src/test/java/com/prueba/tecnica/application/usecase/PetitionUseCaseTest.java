package com.prueba.tecnica.application.usecase;

import com.prueba.tecnica.application.dto.CreatePetitionRequestDto;
import com.prueba.tecnica.domain.enums.MessageType;
import com.prueba.tecnica.domain.exception.DomainException;
import com.prueba.tecnica.domain.exception.OriginNotFoundException;
import com.prueba.tecnica.domain.gateway.PetitionMessageGateway;
import com.prueba.tecnica.domain.repository.OriginLineRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetitionUseCase")
class PetitionUseCaseTest {

    @Mock
    private OriginLineRepository originLineRepository;

    @Mock
    private PetitionMessageGateway petitionMessageGateway;

    @InjectMocks
    private PetitionUseCase petitionUseCase;

    private static final String REGISTERED_ORIGIN = "+573001234567";
    private static final String UNREGISTERED_ORIGIN = "+573009999999";
    private static final String DESTINATION = "+573007654321";
    private static final String TEXT_CONTENT = "Hola, este es un mensaje de texto.";
    private static final String VALID_HTTPS_URL = "https://storage.example.com/files/image.png";
    private static final String VALID_HTTP_URL = "http://cdn.example.com/videos/clip.mp4";

    @Nested
    @DisplayName("Origin validation")
    class OriginValidation {

        @Test
        @DisplayName("Should throw OriginNotFoundException when origin phone number is not registered")
        void shouldThrowWhenOriginNotRegistered() {
            when(originLineRepository.existPhoneNumber(UNREGISTERED_ORIGIN)).thenReturn(false);

            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    UNREGISTERED_ORIGIN, DESTINATION, MessageType.TEXT, TEXT_CONTENT);

            OriginNotFoundException exception = assertThrows(
                    OriginNotFoundException.class,
                    () -> petitionUseCase.processPetition(request));

            assertTrue(exception.getMessage().contains(UNREGISTERED_ORIGIN),
                    "Exception message should reference the unregistered phone number");
            verify(petitionMessageGateway, never()).publishPetition(any(), anyString());
        }

        @Test
        @DisplayName("Should validate origin BEFORE multimedia content so invalid origin is caught first")
        void shouldValidateOriginBeforeMultimediaContent() {
            when(originLineRepository.existPhoneNumber(UNREGISTERED_ORIGIN)).thenReturn(false);

            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    UNREGISTERED_ORIGIN, DESTINATION, MessageType.IMAGE, "not-a-url");

            assertThrows(OriginNotFoundException.class,
                    () -> petitionUseCase.processPetition(request));

            verify(petitionMessageGateway, never()).publishPetition(any(), anyString());
        }
    }

    @Nested
    @DisplayName("TEXT message processing")
    class TextMessageProcessing {

        @BeforeEach
        void setUp() {
            when(originLineRepository.existPhoneNumber(REGISTERED_ORIGIN)).thenReturn(true);
        }

        @Test
        @DisplayName("Should publish a valid TEXT message successfully")
        void shouldPublishValidTextMessage() {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, MessageType.TEXT, TEXT_CONTENT);

            petitionUseCase.processPetition(request);

            ArgumentCaptor<String> receivedAtCaptor = ArgumentCaptor.forClass(String.class);
            verify(petitionMessageGateway).publishPetition(eq(request), receivedAtCaptor.capture());

            String receivedAt = receivedAtCaptor.getValue();
            assertDoesNotThrow(() -> Instant.parse(receivedAt),
                    "receivedAt should be a valid ISO-8601 instant");
        }

        @Test
        @DisplayName("Should NOT validate URL format for TEXT messages even when content is not a URL")
        void shouldNotValidateUrlForTextMessages() {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, MessageType.TEXT, "esto no es una url ::: |||");

            assertDoesNotThrow(() -> petitionUseCase.processPetition(request));
            verify(petitionMessageGateway).publishPetition(eq(request), anyString());
        }

        @Test
        @DisplayName("Should pass the exact same request DTO to the gateway without modification")
        void shouldPassExactRequestDtoToGateway() {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, MessageType.TEXT, TEXT_CONTENT);

            petitionUseCase.processPetition(request);

            ArgumentCaptor<CreatePetitionRequestDto> requestCaptor = ArgumentCaptor
                    .forClass(CreatePetitionRequestDto.class);
            verify(petitionMessageGateway).publishPetition(requestCaptor.capture(), anyString());

            CreatePetitionRequestDto captured = requestCaptor.getValue();
            assertEquals(REGISTERED_ORIGIN, captured.origin());
            assertEquals(DESTINATION, captured.destination());
            assertEquals(MessageType.TEXT, captured.messageType());
            assertEquals(TEXT_CONTENT, captured.content());
        }
    }

    @Nested
    @DisplayName("Multimedia content validation")
    class MultimediaContentValidation {

        @BeforeEach
        void setUp() {
            when(originLineRepository.existPhoneNumber(REGISTERED_ORIGIN)).thenReturn(true);
        }

        @ParameterizedTest(name = "Should publish {0} with valid HTTPS URL")
        @EnumSource(value = MessageType.class, names = { "IMAGE", "VIDEO", "DOCUMENT" })
        @DisplayName("Should publish multimedia message with valid HTTPS URL")
        void shouldPublishMultimediaWithValidHttpsUrl(MessageType type) {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, type, VALID_HTTPS_URL);

            petitionUseCase.processPetition(request);

            verify(petitionMessageGateway).publishPetition(eq(request), anyString());
        }

        @ParameterizedTest(name = "Should publish {0} with valid HTTP URL")
        @EnumSource(value = MessageType.class, names = { "IMAGE", "VIDEO", "DOCUMENT" })
        @DisplayName("Should publish multimedia message with valid HTTP URL")
        void shouldPublishMultimediaWithValidHttpUrl(MessageType type) {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, type, VALID_HTTP_URL);

            petitionUseCase.processPetition(request);

            verify(petitionMessageGateway).publishPetition(eq(request), anyString());
        }

        @ParameterizedTest(name = "Should reject {0} with ftp:// scheme")
        @EnumSource(value = MessageType.class, names = { "IMAGE", "VIDEO", "DOCUMENT" })
        @DisplayName("Should reject multimedia with non-http/https scheme (ftp)")
        void shouldRejectMultimediaWithFtpScheme(MessageType type) {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, type, "ftp://files.example.com/doc.pdf");

            DomainException exception = assertThrows(DomainException.class,
                    () -> petitionUseCase.processPetition(request));

            assertNotNull(exception.getMessage());
            verify(petitionMessageGateway, never()).publishPetition(any(), anyString());
        }

        @ParameterizedTest(name = "Should reject {0} with file:// scheme")
        @EnumSource(value = MessageType.class, names = { "IMAGE", "VIDEO", "DOCUMENT" })
        @DisplayName("Should reject multimedia with file:// scheme")
        void shouldRejectMultimediaWithFileScheme(MessageType type) {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, type, "file:///etc/passwd");

            DomainException exception = assertThrows(DomainException.class,
                    () -> petitionUseCase.processPetition(request));

            assertNotNull(exception.getMessage());
            verify(petitionMessageGateway, never()).publishPetition(any(), anyString());
        }

        @ParameterizedTest(name = "Should reject {0} with malformed URL")
        @EnumSource(value = MessageType.class, names = { "IMAGE", "VIDEO", "DOCUMENT" })
        @DisplayName("Should reject multimedia with malformed URL")
        void shouldRejectMultimediaWithMalformedUrl(MessageType type) {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, type, ":::not-a-valid-uri:::");

            DomainException exception = assertThrows(DomainException.class,
                    () -> petitionUseCase.processPetition(request));

            assertNotNull(exception.getMessage());
            verify(petitionMessageGateway, never()).publishPetition(any(), anyString());
        }

        @Test
        @DisplayName("Should reject IMAGE with empty string as URL")
        void shouldRejectImageWithEmptyUrl() {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, MessageType.IMAGE, "");

            DomainException exception = assertThrows(DomainException.class,
                    () -> petitionUseCase.processPetition(request));

            assertNotNull(exception.getMessage());
            verify(petitionMessageGateway, never()).publishPetition(any(), anyString());
        }

        @Test
        @DisplayName("Should reject VIDEO with plain text content instead of URL")
        void shouldRejectVideoWithPlainTextContent() {
            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, MessageType.VIDEO, "esto es texto, no una url");

            DomainException exception = assertThrows(DomainException.class,
                    () -> petitionUseCase.processPetition(request));

            assertNotNull(exception.getMessage());
            verify(petitionMessageGateway, never()).publishPetition(any(), anyString());
        }
    }

    @Nested
    @DisplayName("Gateway interaction contract")
    class GatewayInteractionContract {

        @Test
        @DisplayName("Should call publishPetition exactly once per valid petition")
        void shouldCallPublishExactlyOnce() {
            when(originLineRepository.existPhoneNumber(REGISTERED_ORIGIN)).thenReturn(true);

            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, MessageType.TEXT, TEXT_CONTENT);

            petitionUseCase.processPetition(request);

            verify(petitionMessageGateway, times(1)).publishPetition(any(), anyString());
        }

        @Test
        @DisplayName("Should provide a receivedAt timestamp that is close to 'now'")
        void shouldProvideRecentTimestamp() {
            when(originLineRepository.existPhoneNumber(REGISTERED_ORIGIN)).thenReturn(true);

            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, MessageType.TEXT, TEXT_CONTENT);

            Instant before = Instant.now();
            petitionUseCase.processPetition(request);
            Instant after = Instant.now();

            ArgumentCaptor<String> receivedAtCaptor = ArgumentCaptor.forClass(String.class);
            verify(petitionMessageGateway).publishPetition(eq(request), receivedAtCaptor.capture());

            Instant receivedAt = Instant.parse(receivedAtCaptor.getValue());
            assertFalse(receivedAt.isBefore(before),
                    "receivedAt should not be before the test started");
            assertFalse(receivedAt.isAfter(after),
                    "receivedAt should not be after the test ended");
        }

        @Test
        @DisplayName("Should never call gateway when origin validation fails")
        void shouldNeverCallGatewayOnOriginFailure() {
            when(originLineRepository.existPhoneNumber(UNREGISTERED_ORIGIN)).thenReturn(false);

            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    UNREGISTERED_ORIGIN, DESTINATION, MessageType.TEXT, TEXT_CONTENT);

            assertThrows(OriginNotFoundException.class,
                    () -> petitionUseCase.processPetition(request));

            verifyNoInteractions(petitionMessageGateway);
        }

        @Test
        @DisplayName("Should never call gateway when multimedia URL validation fails")
        void shouldNeverCallGatewayOnUrlFailure() {
            when(originLineRepository.existPhoneNumber(REGISTERED_ORIGIN)).thenReturn(true);

            CreatePetitionRequestDto request = new CreatePetitionRequestDto(
                    REGISTERED_ORIGIN, DESTINATION, MessageType.IMAGE, "ftp://bad.com/img.png");

            assertThrows(DomainException.class,
                    () -> petitionUseCase.processPetition(request));

            verify(petitionMessageGateway, never()).publishPetition(any(), anyString());
        }
    }
}
