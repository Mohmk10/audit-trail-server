package com.mohmk10.audittrail.integration.kafka;

import com.mohmk10.audittrail.core.domain.Event;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "audit-trail.kafka", name = "enabled", havingValue = "true")
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${audit-trail.kafka.consumer.group-id:audit-trail-group}")
    private String consumerGroupId;

    @Value("${audit-trail.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${audit-trail.kafka.consumer.max-poll-records:500}")
    private int maxPollRecords;

    @Value("${audit-trail.kafka.producer.acks:all}")
    private String producerAcks;

    @Value("${audit-trail.kafka.producer.retries:3}")
    private int producerRetries;

    @Bean
    public ProducerFactory<String, Event> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, producerAcks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, producerRetries);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Event> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, Event> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Event> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Event> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        // Configure error handler with retry
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            new FixedBackOff(1000L, 3)
        );
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
