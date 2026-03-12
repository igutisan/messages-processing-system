package com.prueba.tecnica.infrastructure.messaging;

import com.prueba.tecnica.domain.gateway.PetitionMessageGateway;
import com.prueba.tecnica.domain.model.Petition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    public void publishPetition(Petition petition) {
        MessagePostProcessor addHeaders = message -> {
            message.getMessageProperties().setHeader("receivedAt", petition.getReceivedAt());
            return message;
        };

        Map<String, Object> payload = Map.of(
                "origin", petition.getOrigin(),
                "destination", petition.getDestination(),
                "messageType", petition.getMessageType().name(),
                "content", petition.getContent());

        rabbitTemplate.convertAndSend(exchange, routingKey, payload, addHeaders);
        log.info("Petition published to queue — origin: {}, receivedAt: {}", petition.getOrigin(),
                petition.getReceivedAt());
    }
}
