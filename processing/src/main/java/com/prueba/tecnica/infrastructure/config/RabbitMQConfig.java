package com.prueba.tecnica.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:petitions-exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue:petitions-queue}")
    private String queue;

    @Value("${app.rabbitmq.routing-key:petition.created}")
    private String routingKey;

    @Bean
    public TopicExchange petitionsExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue petitionsQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public Binding petitionsBinding(Queue petitionsQueue, TopicExchange petitionsExchange) {
        return BindingBuilder.bind(petitionsQueue)
                .to(petitionsExchange)
                .with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /*
     * Configures the listener container to use the JSON converter automatically,
     * so @RabbitListener methods receive already-deserialized objects.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
