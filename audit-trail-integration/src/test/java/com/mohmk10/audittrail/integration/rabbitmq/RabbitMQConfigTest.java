package com.mohmk10.audittrail.integration.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RabbitMQConfig.class)
@TestPropertySource(properties = {
    "audit-trail.rabbitmq.enabled=true",
    "audit-trail.rabbitmq.queue.events=test-queue",
    "audit-trail.rabbitmq.exchange.events=test-exchange",
    "audit-trail.rabbitmq.routing-key.events=test.routing"
})
class RabbitMQConfigTest {

    @MockBean
    private ConnectionFactory connectionFactory;

    @Autowired
    private Queue eventsQueue;

    @Autowired
    private Queue deadLetterQueue;

    @Autowired
    private TopicExchange eventsExchange;

    @Autowired
    private TopicExchange deadLetterExchange;

    @Autowired
    private Binding eventsBinding;

    @Autowired
    private Binding deadLetterBinding;

    @Autowired
    private MessageConverter jsonMessageConverter;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void eventsQueue_shouldBeConfigured() {
        assertThat(eventsQueue).isNotNull();
        assertThat(eventsQueue.getName()).isEqualTo("test-queue");
        assertThat(eventsQueue.isDurable()).isTrue();
    }

    @Test
    void deadLetterQueue_shouldBeConfigured() {
        assertThat(deadLetterQueue).isNotNull();
        assertThat(deadLetterQueue.getName()).isEqualTo("test-queue.dlq");
        assertThat(deadLetterQueue.isDurable()).isTrue();
    }

    @Test
    void eventsExchange_shouldBeConfigured() {
        assertThat(eventsExchange).isNotNull();
        assertThat(eventsExchange.getName()).isEqualTo("test-exchange");
    }

    @Test
    void deadLetterExchange_shouldBeConfigured() {
        assertThat(deadLetterExchange).isNotNull();
        assertThat(deadLetterExchange.getName()).isEqualTo("test-exchange.dlx");
    }

    @Test
    void eventsBinding_shouldBeConfigured() {
        assertThat(eventsBinding).isNotNull();
        assertThat(eventsBinding.getRoutingKey()).isEqualTo("test.routing");
    }

    @Test
    void deadLetterBinding_shouldBeConfigured() {
        assertThat(deadLetterBinding).isNotNull();
        assertThat(deadLetterBinding.getRoutingKey()).isEqualTo("test.routing.dlq");
    }

    @Test
    void jsonMessageConverter_shouldBeConfigured() {
        assertThat(jsonMessageConverter).isNotNull();
        assertThat(jsonMessageConverter).isInstanceOf(Jackson2JsonMessageConverter.class);
    }

    @Test
    void rabbitTemplate_shouldUseJsonConverter() {
        assertThat(rabbitTemplate).isNotNull();
        assertThat(rabbitTemplate.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}
