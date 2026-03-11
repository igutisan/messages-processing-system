package com.prueba.tecnica.infrastructure.messaging;

import com.prueba.tecnica.application.dto.PetitionMessageRequestDto;
import com.prueba.tecnica.application.usecase.ProcessMessageUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class PetitionMessageListener {

    private final ProcessMessageUseCase processMessageUseCase;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void receive(PetitionMessageRequestDto message,
            @Header("receivedAt") String receivedAt) {
        log.info("Message received from queue — origin: {}, receivedAt: {}", message.origin(), receivedAt);
        processMessageUseCase.process(message, receivedAt);
    }
}
