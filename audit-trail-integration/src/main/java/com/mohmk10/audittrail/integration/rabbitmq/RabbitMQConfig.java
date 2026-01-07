package com.mohmk10.audittrail.integration.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "audit-trail.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitMQConfig {

    @Value("${audit-trail.rabbitmq.queue.events:audit-events-queue}")
    private String eventsQueue;

    @Value("${audit-trail.rabbitmq.exchange.events:audit-events-exchange}")
    private String eventsExchange;

    @Value("${audit-trail.rabbitmq.routing-key.events:audit.events}")
    private String eventsRoutingKey;

    @Bean
    public Queue eventsQueue() {
        return QueueBuilder.durable(eventsQueue)
                .withArgument("x-dead-letter-exchange", eventsExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", eventsRoutingKey + ".dlq")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(eventsQueue + ".dlq").build();
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(eventsExchange);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(eventsExchange + ".dlx");
    }

    @Bean
    public Binding eventsBinding(Queue eventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(eventsQueue).to(eventsExchange).with(eventsRoutingKey);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(eventsRoutingKey + ".dlq");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
