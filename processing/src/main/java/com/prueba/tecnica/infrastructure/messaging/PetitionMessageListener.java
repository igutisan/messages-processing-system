package com.prueba.tecnica.infrastructure.messaging;

import com.prueba.tecnica.application.dto.PetitionMessageDto;
import com.prueba.tecnica.application.usecase.ProcessMessageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer — listens to the petitions queue and delegates processing
 * to the use case.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PetitionMessageListener {

    private final ProcessMessageUseCase processMessageUseCase;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receive(PetitionMessageDto message,
            @Header("receivedAt") String receivedAt) {
        log.info("Message received from queue — origin: {}, receivedAt: {}", message.origin(), receivedAt);
        processMessageUseCase.process(message, receivedAt);
    }
}
