package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.ApiKeyEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.mapper.ApiKeyMapper;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.JpaApiKeyRepository;
import com.mohmk10.audittrail.admin.domain.ApiKey;
import com.mohmk10.audittrail.admin.domain.ApiKeyCreationResult;
import com.mohmk10.audittrail.admin.domain.ApiKeyScope;
import com.mohmk10.audittrail.admin.domain.ApiKeyStatus;
import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceImplTest {

    @Mock
    private JpaApiKeyRepository repository;

    @Mock
    private ApiKeyMapper mapper;

    private ApiKeyServiceImpl apiKeyService;

    @BeforeEach
    void setUp() {
        apiKeyService = new ApiKeyServiceImpl(repository, mapper);
    }

    @Test
    void shouldCreateApiKeyWithGeneratedKey() {
        ApiKey apiKey = ApiKey.builder()
                .tenantId("tenant-001")
                .name("Test API Key")
                .scopes(Set.of(ApiKeyScope.EVENTS_WRITE))
                .build();

        ApiKeyEntity entity = new ApiKeyEntity();
        ApiKey savedApiKey = AdminTestFixtures.createApiKey();

        when(mapper.toEntity(any(ApiKey.class))).thenReturn(entity);
        when(repository.save(any(ApiKeyEntity.class))).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(savedApiKey);

        ApiKeyCreationResult result = apiKeyService.create(apiKey);

        assertThat(result).isNotNull();
        assertThat(result.apiKey()).isNotNull();
        assertThat(result.plainTextKey()).startsWith("atk_");
        assertThat(result.plainTextKey()).hasSizeGreaterThan(8);

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(mapper).toEntity(captor.capture());

        ApiKey captured = captor.getValue();
        assertThat(captured.getId()).isNotNull();
        assertThat(captured.getKeyHash()).isNotBlank();
        assertThat(captured.getKeyPrefix()).isNotBlank();
        assertThat(captured.getStatus()).isEqualTo(ApiKeyStatus.ACTIVE);
        assertThat(captured.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCreateApiKeyWithProvidedId() {
        UUID id = UUID.randomUUID();
        ApiKey apiKey = ApiKey.builder()
                .id(id)
                .tenantId("tenant-001")
                .name("Test API Key")
                .scopes(Set.of(ApiKeyScope.EVENTS_READ))
                .build();

        ApiKeyEntity entity = new ApiKeyEntity();
        when(mapper.toEntity(any(ApiKey.class))).thenReturn(entity);
        when(repository.save(any(ApiKeyEntity.class))).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(apiKey);

        apiKeyService.create(apiKey);

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(mapper).toEntity(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(id);
    }

    @Test
    void shouldFindApiKeyById() {
        UUID id = UUID.randomUUID();
        ApiKeyEntity entity = new ApiKeyEntity();
        ApiKey apiKey = AdminTestFixtures.createApiKey();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(apiKey);

        Optional<ApiKey> result = apiKeyService.findById(id);

        assertThat(result).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenApiKeyNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<ApiKey> result = apiKeyService.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindApiKeysByTenantId() {
        String tenantId = "tenant-001";
        ApiKeyEntity entity1 = new ApiKeyEntity();
        ApiKeyEntity entity2 = new ApiKeyEntity();
        ApiKey apiKey1 = AdminTestFixtures.createApiKey();
        ApiKey apiKey2 = AdminTestFixtures.createApiKeyWithScopes(Set.of(ApiKeyScope.ADMIN));

        when(repository.findByTenantId(tenantId)).thenReturn(List.of(entity1, entity2));
        when(mapper.toDomain(entity1)).thenReturn(apiKey1);
        when(mapper.toDomain(entity2)).thenReturn(apiKey2);

        List<ApiKey> result = apiKeyService.findByTenantId(tenantId);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFindApiKeysBySourceId() {
        UUID sourceId = UUID.randomUUID();
        ApiKeyEntity entity = new ApiKeyEntity();
        ApiKey apiKey = AdminTestFixtures.createApiKey();

        when(repository.findBySourceId(sourceId)).thenReturn(List.of(entity));
        when(mapper.toDomain(entity)).thenReturn(apiKey);

        List<ApiKey> result = apiKeyService.findBySourceId(sourceId);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldRotateApiKey() {
        UUID id = UUID.randomUUID();
        ApiKeyEntity existingEntity = mock(ApiKeyEntity.class);
        ApiKey existingApiKey = AdminTestFixtures.createApiKey();
        ApiKeyEntity newEntity = new ApiKeyEntity();

        when(existingEntity.getTenantId()).thenReturn("tenant-001");
        when(existingEntity.getSourceId()).thenReturn(UUID.randomUUID());
        when(existingEntity.getName()).thenReturn("Original Key");
        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(repository.save(any(ApiKeyEntity.class))).thenReturn(existingEntity).thenReturn(newEntity);
        when(mapper.toDomain(existingEntity)).thenReturn(existingApiKey);
        when(mapper.toDomain(newEntity)).thenReturn(existingApiKey);
        when(mapper.toEntity(any(ApiKey.class))).thenReturn(newEntity);

        ApiKeyCreationResult result = apiKeyService.rotate(id);

        assertThat(result).isNotNull();
        assertThat(result.plainTextKey()).startsWith("atk_");
        verify(existingEntity).setStatus(ApiKeyStatus.REVOKED);
    }

    @Test
    void shouldThrowWhenRotatingNonExistentApiKey() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apiKeyService.rotate(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API Key not found");
    }

    @Test
    void shouldRevokeApiKey() {
        UUID id = UUID.randomUUID();
        ApiKeyEntity entity = mock(ApiKeyEntity.class);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        apiKeyService.revoke(id);

        verify(entity).setStatus(ApiKeyStatus.REVOKED);
        verify(repository).save(entity);
    }

    @Test
    void shouldThrowWhenRevokingNonExistentApiKey() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apiKeyService.revoke(id))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldValidateActiveApiKeyWithCorrectScope() {
        ApiKey apiKey = ApiKey.builder()
                .id(UUID.randomUUID())
                .status(ApiKeyStatus.ACTIVE)
                .scopes(Set.of(ApiKeyScope.EVENTS_WRITE))
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        ApiKeyEntity entity = new ApiKeyEntity();
        when(repository.findByKeyHash(any())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(apiKey);

        boolean result = apiKeyService.validate("atk_test123", ApiKeyScope.EVENTS_WRITE);

        assertThat(result).isTrue();
    }

    @Test
    void shouldRejectValidationWhenKeyNotFound() {
        when(repository.findByKeyHash(any())).thenReturn(Optional.empty());

        boolean result = apiKeyService.validate("atk_invalid", ApiKeyScope.EVENTS_WRITE);

        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectValidationWhenKeyIsRevoked() {
        ApiKey apiKey = ApiKey.builder()
                .id(UUID.randomUUID())
                .status(ApiKeyStatus.REVOKED)
                .scopes(Set.of(ApiKeyScope.EVENTS_WRITE))
                .build();

        ApiKeyEntity entity = new ApiKeyEntity();
        when(repository.findByKeyHash(any())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(apiKey);

        boolean result = apiKeyService.validate("atk_test123", ApiKeyScope.EVENTS_WRITE);

        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectValidationWhenKeyIsExpired() {
        ApiKey apiKey = ApiKey.builder()
                .id(UUID.randomUUID())
                .status(ApiKeyStatus.ACTIVE)
                .scopes(Set.of(ApiKeyScope.EVENTS_WRITE))
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();

        ApiKeyEntity entity = new ApiKeyEntity();
        when(repository.findByKeyHash(any())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(apiKey);

        boolean result = apiKeyService.validate("atk_test123", ApiKeyScope.EVENTS_WRITE);

        assertThat(result).isFalse();
    }

    @Test
    void shouldRejectValidationWhenScopeMissing() {
        ApiKey apiKey = ApiKey.builder()
                .id(UUID.randomUUID())
                .status(ApiKeyStatus.ACTIVE)
                .scopes(Set.of(ApiKeyScope.EVENTS_READ))
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        ApiKeyEntity entity = new ApiKeyEntity();
        when(repository.findByKeyHash(any())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(apiKey);

        boolean result = apiKeyService.validate("atk_test123", ApiKeyScope.EVENTS_WRITE);

        assertThat(result).isFalse();
    }

    @Test
    void shouldValidateWithAdminScope() {
        ApiKey apiKey = ApiKey.builder()
                .id(UUID.randomUUID())
                .status(ApiKeyStatus.ACTIVE)
                .scopes(Set.of(ApiKeyScope.ADMIN))
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        ApiKeyEntity entity = new ApiKeyEntity();
        when(repository.findByKeyHash(any())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(apiKey);

        boolean result = apiKeyService.validate("atk_test123", ApiKeyScope.EVENTS_WRITE);

        assertThat(result).isTrue();
    }

    @Test
    void shouldUpdateLastUsed() {
        UUID id = UUID.randomUUID();
        String ip = "192.168.1.1";

        apiKeyService.updateLastUsed(id, ip);

        verify(repository).updateLastUsed(eq(id), any(Instant.class), eq(ip));
    }
}
