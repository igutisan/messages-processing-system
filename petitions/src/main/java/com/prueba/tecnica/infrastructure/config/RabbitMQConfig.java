package com.prueba.tecnica.infrastructure.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:petitions-exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue:petitions-queue}")
    private String queue;

    @Bean
    public TopicExchange petitionsExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue petitionsQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
