package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.SourceEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.mapper.SourceMapper;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.JpaSourceRepository;
import com.mohmk10.audittrail.admin.domain.Source;
import com.mohmk10.audittrail.admin.domain.SourceStatus;
import com.mohmk10.audittrail.admin.domain.SourceType;
import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourceServiceImplTest {

    @Mock
    private JpaSourceRepository repository;

    @Mock
    private SourceMapper mapper;

    private SourceServiceImpl sourceService;

    @BeforeEach
    void setUp() {
        sourceService = new SourceServiceImpl(repository, mapper);
    }

    @Test
    void shouldCreateSourceWithDefaults() {
        Source source = Source.builder()
                .tenantId("tenant-001")
                .name("New Source")
                .type(SourceType.WEB_APP)
                .build();

        SourceEntity entity = new SourceEntity();
        when(mapper.toEntity(any(Source.class))).thenReturn(entity);
        when(repository.save(any(SourceEntity.class))).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(AdminTestFixtures.createSource());

        Source result = sourceService.create(source);

        assertThat(result).isNotNull();

        ArgumentCaptor<Source> captor = ArgumentCaptor.forClass(Source.class);
        verify(mapper).toEntity(captor.capture());

        Source captured = captor.getValue();
        assertThat(captured.getId()).isNotNull();
        assertThat(captured.getStatus()).isEqualTo(SourceStatus.ACTIVE);
        assertThat(captured.getCreatedAt()).isNotNull();
        assertThat(captured.getEventCount()).isZero();
    }

    @Test
    void shouldCreateSourceWithProvidedId() {
        UUID id = UUID.randomUUID();
        Source source = Source.builder()
                .id(id)
                .tenantId("tenant-001")
                .name("Source with ID")
                .type(SourceType.BACKEND_SERVICE)
                .status(SourceStatus.INACTIVE)
                .build();

        SourceEntity entity = new SourceEntity();
        when(mapper.toEntity(any(Source.class))).thenReturn(entity);
        when(repository.save(any(SourceEntity.class))).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(source);

        sourceService.create(source);

        ArgumentCaptor<Source> captor = ArgumentCaptor.forClass(Source.class);
        verify(mapper).toEntity(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(id);
        assertThat(captor.getValue().getStatus()).isEqualTo(SourceStatus.INACTIVE);
    }

    @Test
    void shouldUpdateSourceName() {
        UUID id = UUID.randomUUID();
        SourceEntity existingEntity = mock(SourceEntity.class);
        Source updated = AdminTestFixtures.createSource();

        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any(SourceEntity.class))).thenReturn(existingEntity);
        when(mapper.toDomain(existingEntity)).thenReturn(updated);

        Source updateData = Source.builder().name("Updated Name").build();
        Source result = sourceService.update(id, updateData);

        assertThat(result).isNotNull();
        verify(existingEntity).setName("Updated Name");
    }

    @Test
    void shouldUpdateSourceDescription() {
        UUID id = UUID.randomUUID();
        SourceEntity existingEntity = mock(SourceEntity.class);
        Source updated = AdminTestFixtures.createSource();

        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any(SourceEntity.class))).thenReturn(existingEntity);
        when(mapper.toDomain(existingEntity)).thenReturn(updated);

        Source updateData = Source.builder().description("New description").build();
        sourceService.update(id, updateData);

        verify(existingEntity).setDescription("New description");
    }

    @Test
    void shouldUpdateSourceType() {
        UUID id = UUID.randomUUID();
        SourceEntity existingEntity = mock(SourceEntity.class);
        Source updated = AdminTestFixtures.createSource();

        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any(SourceEntity.class))).thenReturn(existingEntity);
        when(mapper.toDomain(existingEntity)).thenReturn(updated);

        Source updateData = Source.builder().type(SourceType.MOBILE_APP).build();
        sourceService.update(id, updateData);

        verify(existingEntity).setType(SourceType.MOBILE_APP);
    }

    @Test
    void shouldUpdateSourceConfig() {
        UUID id = UUID.randomUUID();
        SourceEntity existingEntity = mock(SourceEntity.class);
        Source updated = AdminTestFixtures.createSource();
        Map<String, String> config = Map.of("key", "value");

        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any(SourceEntity.class))).thenReturn(existingEntity);
        when(mapper.toDomain(existingEntity)).thenReturn(updated);

        Source updateData = Source.builder().config(config).build();
        sourceService.update(id, updateData);

        verify(existingEntity).setConfig(config);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentSource() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Source updateData = Source.builder().name("Updated").build();

        assertThatThrownBy(() -> sourceService.update(id, updateData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Source not found");
    }

    @Test
    void shouldFindSourceById() {
        UUID id = UUID.randomUUID();
        SourceEntity entity = new SourceEntity();
        Source source = AdminTestFixtures.createSource();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(source);

        Optional<Source> result = sourceService.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(source);
    }

    @Test
    void shouldReturnEmptyWhenSourceNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<Source> result = sourceService.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindSourcesByTenantId() {
        String tenantId = "tenant-001";
        SourceEntity entity1 = new SourceEntity();
        SourceEntity entity2 = new SourceEntity();
        Source source1 = AdminTestFixtures.createSourceWithType(SourceType.WEB_APP);
        Source source2 = AdminTestFixtures.createSourceWithType(SourceType.MOBILE_APP);

        when(repository.findByTenantId(tenantId)).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(source1);
        when(mapper.toDomain(entity2)).thenReturn(source2);

        List<Source> result = sourceService.findByTenantId(tenantId);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldActivateSource() {
        UUID id = UUID.randomUUID();
        SourceEntity entity = mock(SourceEntity.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        sourceService.activate(id);

        verify(entity).setStatus(SourceStatus.ACTIVE);
        verify(repository).save(entity);
    }

    @Test
    void shouldThrowWhenActivatingNonExistentSource() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sourceService.activate(id))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDeactivateSource() {
        UUID id = UUID.randomUUID();
        SourceEntity entity = mock(SourceEntity.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        sourceService.deactivate(id);

        verify(entity).setStatus(SourceStatus.INACTIVE);
        verify(repository).save(entity);
    }

    @Test
    void shouldDeleteSource() {
        UUID id = UUID.randomUUID();

        sourceService.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void shouldIncrementEventCount() {
        UUID id = UUID.randomUUID();

        sourceService.incrementEventCount(id);

        verify(repository).incrementEventCount(id);
    }

    @Test
    void shouldUpdateLastEventAt() {
        UUID id = UUID.randomUUID();

        sourceService.updateLastEventAt(id);

        verify(repository).updateLastEventAt(eq(id), any());
    }
}
