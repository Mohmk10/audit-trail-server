package com.mohmk10.audittrail.admin.adapter.in.rest;

import com.mohmk10.audittrail.admin.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.admin.domain.ApiKey;
import com.mohmk10.audittrail.admin.domain.ApiKeyCreationResult;
import com.mohmk10.audittrail.admin.domain.ApiKeyScope;
import com.mohmk10.audittrail.admin.domain.ApiKeyStatus;
import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import com.mohmk10.audittrail.admin.service.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyControllerTest {

    @Mock
    private ApiKeyService apiKeyService;

    @Mock
    private AdminDtoMapper dtoMapper;

    private ApiKeyController controller;

    @BeforeEach
    void setUp() {
        controller = new ApiKeyController(apiKeyService, dtoMapper);
    }

    @Test
    void shouldCreateApiKeyAndReturn201() {
        CreateApiKeyRequest request = new CreateApiKeyRequest(
                "tenant-001",
                UUID.randomUUID(),
                "Test API Key",
                Set.of(ApiKeyScope.EVENTS_WRITE),
                null
        );
        ApiKey apiKey = AdminTestFixtures.createApiKey();
        ApiKeyCreationResult creationResult = new ApiKeyCreationResult(apiKey, "atk_test_key_123");
        ApiKeyCreatedResponse response = new ApiKeyCreatedResponse(
                apiKey.getId(),
                apiKey.getTenantId(),
                apiKey.getSourceId(),
                apiKey.getName(),
                "atk_test_key_123",
                apiKey.getKeyPrefix(),
                apiKey.getScopes(),
                apiKey.getStatus(),
                apiKey.getCreatedAt(),
                apiKey.getExpiresAt(),
                null
        );

        when(dtoMapper.toApiKeyDomain(request)).thenReturn(apiKey);
        when(apiKeyService.create(apiKey)).thenReturn(creationResult);
        when(dtoMapper.toApiKeyCreatedResponse(creationResult)).thenReturn(response);

        ResponseEntity<ApiKeyCreatedResponse> result = controller.create(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().key()).isEqualTo("atk_test_key_123");
    }

    @Test
    void shouldFindApiKeysByTenantId() {
        String tenantId = "tenant-001";
        ApiKey apiKey1 = AdminTestFixtures.createApiKey();
        ApiKey apiKey2 = AdminTestFixtures.createApiKeyWithScopes(Set.of(ApiKeyScope.ADMIN));
        ApiKeyResponse response1 = createApiKeyResponse(apiKey1);
        ApiKeyResponse response2 = createApiKeyResponse(apiKey2);

        when(apiKeyService.findByTenantId(tenantId)).thenReturn(List.of(apiKey1, apiKey2));
        when(dtoMapper.toApiKeyResponse(apiKey1)).thenReturn(response1);
        when(dtoMapper.toApiKeyResponse(apiKey2)).thenReturn(response2);

        ResponseEntity<List<ApiKeyResponse>> result = controller.findByTenantId(tenantId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(2);
    }

    @Test
    void shouldFindApiKeyByIdAndReturn200() {
        UUID id = UUID.randomUUID();
        ApiKey apiKey = AdminTestFixtures.createApiKey();
        ApiKeyResponse response = createApiKeyResponse(apiKey);

        when(apiKeyService.findById(id)).thenReturn(Optional.of(apiKey));
        when(dtoMapper.toApiKeyResponse(apiKey)).thenReturn(response);

        ResponseEntity<ApiKeyResponse> result = controller.findById(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
    }

    @Test
    void shouldReturn404WhenApiKeyNotFoundById() {
        UUID id = UUID.randomUUID();
        when(apiKeyService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<ApiKeyResponse> result = controller.findById(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldFindApiKeysBySourceId() {
        UUID sourceId = UUID.randomUUID();
        ApiKey apiKey = AdminTestFixtures.createApiKey();
        ApiKeyResponse response = createApiKeyResponse(apiKey);

        when(apiKeyService.findBySourceId(sourceId)).thenReturn(List.of(apiKey));
        when(dtoMapper.toApiKeyResponse(apiKey)).thenReturn(response);

        ResponseEntity<List<ApiKeyResponse>> result = controller.findBySourceId(sourceId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    void shouldRotateApiKeyAndReturn200() {
        UUID id = UUID.randomUUID();
        ApiKey newApiKey = AdminTestFixtures.createApiKey();
        ApiKeyCreationResult creationResult = new ApiKeyCreationResult(newApiKey, "atk_new_key_456");

        when(apiKeyService.rotate(id)).thenReturn(creationResult);

        ResponseEntity<RotateApiKeyResponse> result = controller.rotate(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().newKey()).isEqualTo("atk_new_key_456");
        assertThat(result.getBody().oldKeyId()).isEqualTo(id);
    }

    @Test
    void shouldRevokeApiKeyAndReturn200() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.revoke(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(apiKeyService).revoke(id);
    }

    @Test
    void shouldDeleteApiKeyAndReturn204() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> result = controller.delete(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(apiKeyService).revoke(id);
    }

    private ApiKeyResponse createApiKeyResponse(ApiKey apiKey) {
        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getTenantId(),
                apiKey.getSourceId(),
                apiKey.getName(),
                apiKey.getKeyPrefix(),
                apiKey.getScopes(),
                apiKey.getStatus(),
                apiKey.getCreatedAt(),
                apiKey.getExpiresAt(),
                apiKey.getLastUsedAt(),
                apiKey.getLastUsedIp()
        );
    }
}
