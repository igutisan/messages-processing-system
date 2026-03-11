package com.prueba.tecnica.infrastructure.messaging;

import com.prueba.tecnica.application.dto.CreatePetitionRequestDto;
import com.prueba.tecnica.domain.gateway.PetitionMessageGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ adapter — publishes petition messages with reception timestamp in
 * headers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitPetitionPublisher implements PetitionMessageGateway {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:petitions-exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.created:petition.created}")
    private String routingKey;

    @Override
    public void publishPetition(CreatePetitionRequestDto request, String receivedAt) {
        MessagePostProcessor addHeaders = message -> {
            message.getMessageProperties().setHeader("receivedAt", receivedAt);
            return message;
        };

        rabbitTemplate.convertAndSend(exchange, routingKey, request, addHeaders);
        log.info("Petition published to queue — origin: {}, receivedAt: {}", request.origin(), receivedAt);
    }
}
