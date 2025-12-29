# Kafka Integration

Guide d'integration avec Apache Kafka pour l'ingestion et l'export d'evenements.

## Vue d'ensemble

Audit Trail supporte Kafka pour :
- **Ingestion** : Recevoir des evenements depuis vos applications
- **Export** : Republier les evenements vers d'autres systemes

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Your App  │────►│    Kafka    │────►│ Audit Trail │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                           │
                           ▼
                    ┌─────────────┐
                    │  Your SIEM  │
                    └─────────────┘
```

## Configuration

### application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:9092
    consumer:
      group-id: audit-trail-consumer
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

audit-trail:
  kafka:
    enabled: true
    topics:
      inbound: audit-events-inbound
      outbound: audit-events-outbound
    consumer:
      concurrency: 3
      batch-size: 100
```

### Variables d'environnement

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
AUDIT_TRAIL_KAFKA_ENABLED=true
AUDIT_TRAIL_KAFKA_TOPICS_INBOUND=audit-events-inbound
AUDIT_TRAIL_KAFKA_TOPICS_OUTBOUND=audit-events-outbound
```

## Ingestion via Kafka

### Format du message

**Key**: `{tenantId}:{actorId}`

**Value**:

```json
{
  "actor": {
    "id": "user-123",
    "type": "USER",
    "name": "John Doe"
  },
  "action": {
    "type": "CREATE",
    "description": "Created document"
  },
  "resource": {
    "id": "doc-456",
    "type": "DOCUMENT",
    "name": "Q4 Report"
  },
  "metadata": {
    "source": "kafka-producer",
    "tenantId": "tenant-001",
    "correlationId": "kafka-msg-123"
  }
}
```

### Producer Java

```java
@Service
@RequiredArgsConstructor
public class AuditEventProducer {

    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;

    @Value("${audit-trail.kafka.topics.inbound}")
    private String topic;

    public void sendEvent(AuditEvent event) {
        String key = event.getMetadata().getTenantId() + ":" +
                     event.getActor().getId();

        kafkaTemplate.send(topic, key, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send event", ex);
                } else {
                    log.info("Event sent: {}", result.getRecordMetadata().offset());
                }
            });
    }
}
```

### Producer Node.js

```javascript
const { Kafka } = require('kafkajs');

const kafka = new Kafka({
  clientId: 'my-app',
  brokers: ['kafka:9092']
});

const producer = kafka.producer();

async function sendAuditEvent(event) {
  await producer.connect();

  const key = `${event.metadata.tenantId}:${event.actor.id}`;

  await producer.send({
    topic: 'audit-events-inbound',
    messages: [
      {
        key: key,
        value: JSON.stringify(event),
        headers: {
          'content-type': 'application/json'
        }
      }
    ]
  });
}
```

### Producer Python

```python
from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_servers=['kafka:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8'),
    key_serializer=lambda k: k.encode('utf-8')
)

def send_audit_event(event):
    key = f"{event['metadata']['tenantId']}:{event['actor']['id']}"

    future = producer.send(
        'audit-events-inbound',
        key=key,
        value=event
    )

    # Attendre la confirmation
    result = future.get(timeout=10)
    print(f"Event sent to partition {result.partition} at offset {result.offset}")
```

## Export via Kafka

### Activer l'export

```yaml
audit-trail:
  kafka:
    export:
      enabled: true
      topic: audit-events-outbound
      events:
        - event.created
        - alert.triggered
```

### Format du message exporté

```json
{
  "type": "event.created",
  "timestamp": "2025-01-15T10:30:00Z",
  "tenantId": "tenant-001",
  "event": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "timestamp": "2025-01-15T10:30:00Z",
    "actor": { ... },
    "action": { ... },
    "resource": { ... },
    "hash": "abc123..."
  }
}
```

### Consumer Java

```java
@Component
@KafkaListener(topics = "${audit-trail.kafka.topics.outbound}",
               groupId = "my-consumer-group")
public class AuditEventConsumer {

    @KafkaHandler
    public void handleEvent(ConsumerRecord<String, AuditEventExport> record) {
        AuditEventExport export = record.value();

        switch (export.getType()) {
            case "event.created":
                processNewEvent(export.getEvent());
                break;
            case "alert.triggered":
                processAlert(export.getAlert());
                break;
        }
    }

    private void processNewEvent(AuditEvent event) {
        // Traiter l'evenement
        log.info("Received event: {}", event.getId());
    }
}
```

### Consumer Node.js

```javascript
const { Kafka } = require('kafkajs');

const kafka = new Kafka({
  clientId: 'my-consumer',
  brokers: ['kafka:9092']
});

const consumer = kafka.consumer({ groupId: 'my-consumer-group' });

async function startConsumer() {
  await consumer.connect();
  await consumer.subscribe({
    topic: 'audit-events-outbound',
    fromBeginning: false
  });

  await consumer.run({
    eachMessage: async ({ topic, partition, message }) => {
      const event = JSON.parse(message.value.toString());

      console.log(`Received ${event.type}:`, event.event.id);

      switch (event.type) {
        case 'event.created':
          await processNewEvent(event.event);
          break;
        case 'alert.triggered':
          await processAlert(event.alert);
          break;
      }
    }
  });
}
```

## Topics

### Configuration recommandee

```bash
# Topic d'ingestion
kafka-topics --create \
  --topic audit-events-inbound \
  --partitions 12 \
  --replication-factor 3 \
  --config retention.ms=86400000 \
  --config cleanup.policy=delete

# Topic d'export
kafka-topics --create \
  --topic audit-events-outbound \
  --partitions 12 \
  --replication-factor 3 \
  --config retention.ms=604800000 \
  --config cleanup.policy=delete
```

### Partitioning

Les messages sont partitionnes par `tenantId` pour garantir l'ordre par tenant :

```java
public class TenantPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                         Object value, byte[] valueBytes, Cluster cluster) {
        String tenantId = extractTenantId((String) key);
        int numPartitions = cluster.partitionCountForTopic(topic);
        return Math.abs(tenantId.hashCode() % numPartitions);
    }

    private String extractTenantId(String key) {
        return key.split(":")[0];
    }
}
```

## Batch Processing

### Consumer avec batch

```java
@Component
public class BatchAuditEventConsumer {

    @KafkaListener(topics = "${audit-trail.kafka.topics.inbound}",
                   containerFactory = "batchListenerFactory")
    public void consumeBatch(List<ConsumerRecord<String, AuditEvent>> records) {
        List<AuditEvent> events = records.stream()
            .map(ConsumerRecord::value)
            .collect(Collectors.toList());

        // Traiter en batch
        eventService.processBatch(events);
    }
}

@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AuditEvent>
           batchListenerFactory(ConsumerFactory<String, AuditEvent> cf) {
        ConcurrentKafkaListenerContainerFactory<String, AuditEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(AckMode.BATCH);
        return factory;
    }
}
```

## Error Handling

### Dead Letter Topic

```yaml
spring:
  kafka:
    consumer:
      properties:
        spring.kafka.listener.log-container-config: true

audit-trail:
  kafka:
    error-handling:
      dead-letter-topic: audit-events-dlq
      max-retries: 3
      retry-backoff-ms: 1000
```

### Implementation

```java
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(template);

    BackOff backOff = new ExponentialBackOff(1000L, 2.0);
    backOff.setMaxElapsedTime(30000L);

    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
    handler.addNotRetryableExceptions(ValidationException.class);

    return handler;
}
```

## Monitoring

### Metriques

Audit Trail expose les metriques Kafka via Micrometer :

```
# Ingestion
audit_trail_kafka_consumer_messages_total
audit_trail_kafka_consumer_errors_total
audit_trail_kafka_consumer_lag

# Export
audit_trail_kafka_producer_messages_total
audit_trail_kafka_producer_errors_total
```

### Health Check

```bash
curl http://localhost:8080/actuator/health/kafka
```

```json
{
  "status": "UP",
  "details": {
    "brokerId": "1",
    "clusterId": "abc123",
    "topics": ["audit-events-inbound", "audit-events-outbound"]
  }
}
```

## Securite

### SASL/SSL

```yaml
spring:
  kafka:
    properties:
      security.protocol: SASL_SSL
      sasl.mechanism: PLAIN
      sasl.jaas.config: >-
        org.apache.kafka.common.security.plain.PlainLoginModule required
        username="${KAFKA_USERNAME}"
        password="${KAFKA_PASSWORD}";
    ssl:
      trust-store-location: classpath:truststore.jks
      trust-store-password: ${KAFKA_SSL_TRUSTSTORE_PASSWORD}
```

### ACLs

```bash
# Permettre l'ingestion
kafka-acls --add --allow-principal User:audit-trail \
  --operation Read --operation Describe \
  --topic audit-events-inbound

# Permettre l'export
kafka-acls --add --allow-principal User:audit-trail \
  --operation Write --operation Describe \
  --topic audit-events-outbound
```

## Best Practices

1. **Utiliser des keys coherentes** pour garantir l'ordre
2. **Configurer la retention** selon vos besoins de replay
3. **Monitorer le lag** des consumers
4. **Implementer des DLQ** pour les erreurs
5. **Utiliser des transactions** pour les operations critiques
