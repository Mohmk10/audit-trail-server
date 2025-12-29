package com.mohmk10.audittrail.storage.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.storage.TestFixtures;
import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import com.mohmk10.audittrail.storage.adapter.out.persistence.repository.JpaEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImmutableStorageServiceTest {

    @Mock
    private JpaEventRepository jpaEventRepository;

    @Mock
    private HashChainService hashChainService;

    @Mock
    private SignatureService signatureService;

    private ImmutableStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ImmutableStorageServiceImpl(jpaEventRepository, hashChainService, signatureService);
    }

    @Test
    void shouldStoreEventWithHash() {
        Event event = TestFixtures.createTestEvent();
        String expectedHash = "computed-hash-value";

        when(hashChainService.getLastHash(anyString())).thenReturn("GENESIS");
        when(hashChainService.calculateHash(any(Event.class), anyString())).thenReturn(expectedHash);
        when(signatureService.sign(anyString())).thenReturn("signature");
        when(jpaEventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event stored = service.store(event);

        assertThat(stored).isNotNull();
        ArgumentCaptor<EventEntity> entityCaptor = ArgumentCaptor.forClass(EventEntity.class);
        verify(jpaEventRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getHash()).isEqualTo(expectedHash);
    }

    @Test
    void shouldStoreEventWithSignature() {
        Event event = TestFixtures.createTestEvent();
        String expectedSignature = "digital-signature";

        when(hashChainService.getLastHash(anyString())).thenReturn("GENESIS");
        when(hashChainService.calculateHash(any(Event.class), anyString())).thenReturn("hash");
        when(signatureService.sign(anyString())).thenReturn(expectedSignature);
        when(jpaEventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event stored = service.store(event);

        assertThat(stored).isNotNull();
        ArgumentCaptor<EventEntity> entityCaptor = ArgumentCaptor.forClass(EventEntity.class);
        verify(jpaEventRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getSignature()).isEqualTo(expectedSignature);
    }

    @Test
    void shouldChainWithPreviousEvent() {
        Event event = TestFixtures.createTestEvent();
        String previousHash = "previous-event-hash";

        when(hashChainService.getLastHash(anyString())).thenReturn(previousHash);
        when(hashChainService.calculateHash(any(Event.class), eq(previousHash))).thenReturn("new-hash");
        when(signatureService.sign(anyString())).thenReturn("signature");
        when(jpaEventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.store(event);

        ArgumentCaptor<EventEntity> entityCaptor = ArgumentCaptor.forClass(EventEntity.class);
        verify(jpaEventRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getPreviousHash()).isEqualTo(previousHash);
    }

    @Test
    void shouldHandleFirstEventOfTenant() {
        Event event = TestFixtures.createTestEventWithTenant("new-tenant");

        when(hashChainService.getLastHash("new-tenant")).thenReturn("GENESIS");
        when(hashChainService.calculateHash(any(Event.class), eq("GENESIS"))).thenReturn("first-hash");
        when(signatureService.sign(anyString())).thenReturn("signature");
        when(jpaEventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.store(event);

        verify(hashChainService).getLastHash("new-tenant");
        verify(hashChainService).calculateHash(any(Event.class), eq("GENESIS"));
    }

    @Test
    void shouldStoreBatchInOrder() {
        Event event1 = TestFixtures.createTestEvent();
        Event event2 = TestFixtures.createTestEvent();
        Event event3 = TestFixtures.createTestEvent();
        List<Event> events = List.of(event1, event2, event3);

        when(hashChainService.getLastHash(anyString())).thenReturn("GENESIS", "hash1", "hash2");
        when(hashChainService.calculateHash(any(Event.class), anyString()))
                .thenReturn("hash1", "hash2", "hash3");
        when(signatureService.sign(anyString())).thenReturn("sig");
        when(jpaEventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Event> stored = service.storeBatch(events);

        assertThat(stored).hasSize(3);
        verify(jpaEventRepository, times(3)).save(any(EventEntity.class));
    }

    @Test
    void shouldMaintainChainInBatch() {
        Event event1 = TestFixtures.createTestEvent();
        Event event2 = TestFixtures.createTestEvent();
        List<Event> events = List.of(event1, event2);

        when(hashChainService.getLastHash(anyString()))
                .thenReturn("GENESIS")
                .thenReturn("hash1");
        when(hashChainService.calculateHash(any(Event.class), eq("GENESIS"))).thenReturn("hash1");
        when(hashChainService.calculateHash(any(Event.class), eq("hash1"))).thenReturn("hash2");
        when(signatureService.sign(anyString())).thenReturn("sig");
        when(jpaEventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.storeBatch(events);

        verify(hashChainService, times(2)).getLastHash(anyString());
    }

    @Test
    void shouldFindEventById() {
        UUID eventId = UUID.randomUUID();
        EventEntity entity = TestFixtures.createTestEventEntity();
        entity.setId(eventId);

        when(jpaEventRepository.findById(eventId)).thenReturn(Optional.of(entity));

        Optional<Event> result = service.findById(eventId);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(eventId);
    }

    @Test
    void shouldReturnEmptyForNonExistentEvent() {
        UUID nonExistentId = UUID.randomUUID();
        when(jpaEventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Event> result = service.findById(nonExistentId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldVerifyIntegrityOfValidEvent() {
        UUID eventId = UUID.randomUUID();
        EventEntity entity = TestFixtures.createTestEventEntity();
        entity.setId(eventId);
        entity.setPreviousHash("prev-hash");
        entity.setHash("valid-hash");
        entity.setSignature("valid-signature");

        when(jpaEventRepository.findById(eventId)).thenReturn(Optional.of(entity));
        when(hashChainService.calculateHash(any(Event.class), eq("prev-hash"))).thenReturn("valid-hash");
        when(signatureService.verify("valid-hash", "valid-signature")).thenReturn(true);

        boolean isValid = service.verifyIntegrity(eventId);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldDetectIntegrityViolationWithWrongHash() {
        UUID eventId = UUID.randomUUID();
        EventEntity entity = TestFixtures.createTestEventEntity();
        entity.setId(eventId);
        entity.setPreviousHash("prev-hash");
        entity.setHash("stored-hash");
        entity.setSignature("signature");

        when(jpaEventRepository.findById(eventId)).thenReturn(Optional.of(entity));
        when(hashChainService.calculateHash(any(Event.class), eq("prev-hash"))).thenReturn("different-hash");

        boolean isValid = service.verifyIntegrity(eventId);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldDetectIntegrityViolationWithInvalidSignature() {
        UUID eventId = UUID.randomUUID();
        EventEntity entity = TestFixtures.createTestEventEntity();
        entity.setId(eventId);
        entity.setPreviousHash("prev-hash");
        entity.setHash("valid-hash");
        entity.setSignature("invalid-signature");

        when(jpaEventRepository.findById(eventId)).thenReturn(Optional.of(entity));
        when(hashChainService.calculateHash(any(Event.class), eq("prev-hash"))).thenReturn("valid-hash");
        when(signatureService.verify("valid-hash", "invalid-signature")).thenReturn(false);

        boolean isValid = service.verifyIntegrity(eventId);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseForNonExistentEventIntegrity() {
        UUID nonExistentId = UUID.randomUUID();
        when(jpaEventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        boolean isValid = service.verifyIntegrity(nonExistentId);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldHandleEventWithNullMetadata() {
        Event eventWithNullMetadata = new Event(
                UUID.randomUUID(),
                java.time.Instant.now(),
                TestFixtures.createTestActor(),
                TestFixtures.createTestAction(),
                TestFixtures.createTestResource(),
                null,
                null,
                null,
                null
        );

        when(hashChainService.getLastHash("default")).thenReturn("GENESIS");
        when(hashChainService.calculateHash(any(Event.class), anyString())).thenReturn("hash");
        when(signatureService.sign(anyString())).thenReturn("sig");
        when(jpaEventRepository.save(any(EventEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event stored = service.store(eventWithNullMetadata);

        assertThat(stored).isNotNull();
        verify(hashChainService).getLastHash("default");
    }

    @Test
    void shouldStoreBatchReturnsEmptyListForEmptyInput() {
        List<Event> stored = service.storeBatch(List.of());

        assertThat(stored).isEmpty();
        verify(jpaEventRepository, never()).save(any());
    }
}
