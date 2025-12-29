package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyTest {

    @Test
    void shouldBuildApiKeyWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        Instant now = Instant.now();
        Set<ApiKeyScope> scopes = Set.of(ApiKeyScope.EVENTS_READ, ApiKeyScope.EVENTS_WRITE);

        ApiKey apiKey = ApiKey.builder()
                .id(id)
                .tenantId("tenant-001")
                .sourceId(sourceId)
                .name("Production API Key")
                .keyHash("hashed_value")
                .keyPrefix("atk_prod")
                .scopes(scopes)
                .status(ApiKeyStatus.ACTIVE)
                .createdAt(now)
                .expiresAt(now.plusSeconds(86400))
                .lastUsedAt(now)
                .lastUsedIp("192.168.1.1")
                .build();

        assertThat(apiKey.getId()).isEqualTo(id);
        assertThat(apiKey.getTenantId()).isEqualTo("tenant-001");
        assertThat(apiKey.getSourceId()).isEqualTo(sourceId);
        assertThat(apiKey.getName()).isEqualTo("Production API Key");
        assertThat(apiKey.getKeyHash()).isEqualTo("hashed_value");
        assertThat(apiKey.getKeyPrefix()).isEqualTo("atk_prod");
        assertThat(apiKey.getScopes()).isEqualTo(scopes);
        assertThat(apiKey.getStatus()).isEqualTo(ApiKeyStatus.ACTIVE);
        assertThat(apiKey.getCreatedAt()).isEqualTo(now);
        assertThat(apiKey.getExpiresAt()).isEqualTo(now.plusSeconds(86400));
        assertThat(apiKey.getLastUsedAt()).isEqualTo(now);
        assertThat(apiKey.getLastUsedIp()).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldCreateEmptyApiKey() {
        ApiKey apiKey = new ApiKey();

        assertThat(apiKey.getId()).isNull();
        assertThat(apiKey.getName()).isNull();
        assertThat(apiKey.getScopes()).isNull();
    }

    @Test
    void shouldSetAndGetId() {
        ApiKey apiKey = new ApiKey();
        UUID id = UUID.randomUUID();

        apiKey.setId(id);

        assertThat(apiKey.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetTenantId() {
        ApiKey apiKey = new ApiKey();

        apiKey.setTenantId("tenant-002");

        assertThat(apiKey.getTenantId()).isEqualTo("tenant-002");
    }

    @Test
    void shouldSetAndGetSourceId() {
        ApiKey apiKey = new ApiKey();
        UUID sourceId = UUID.randomUUID();

        apiKey.setSourceId(sourceId);

        assertThat(apiKey.getSourceId()).isEqualTo(sourceId);
    }

    @Test
    void shouldSetAndGetName() {
        ApiKey apiKey = new ApiKey();

        apiKey.setName("Test Key");

        assertThat(apiKey.getName()).isEqualTo("Test Key");
    }

    @Test
    void shouldSetAndGetKeyHash() {
        ApiKey apiKey = new ApiKey();

        apiKey.setKeyHash("new_hash");

        assertThat(apiKey.getKeyHash()).isEqualTo("new_hash");
    }

    @Test
    void shouldSetAndGetKeyPrefix() {
        ApiKey apiKey = new ApiKey();

        apiKey.setKeyPrefix("atk_test");

        assertThat(apiKey.getKeyPrefix()).isEqualTo("atk_test");
    }

    @Test
    void shouldSetAndGetScopes() {
        ApiKey apiKey = new ApiKey();
        Set<ApiKeyScope> scopes = Set.of(ApiKeyScope.SEARCH, ApiKeyScope.REPORTS_READ);

        apiKey.setScopes(scopes);

        assertThat(apiKey.getScopes()).isEqualTo(scopes);
    }

    @Test
    void shouldSetAndGetStatus() {
        ApiKey apiKey = new ApiKey();

        apiKey.setStatus(ApiKeyStatus.REVOKED);

        assertThat(apiKey.getStatus()).isEqualTo(ApiKeyStatus.REVOKED);
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        ApiKey apiKey = new ApiKey();
        Instant now = Instant.now();

        apiKey.setCreatedAt(now);

        assertThat(apiKey.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetExpiresAt() {
        ApiKey apiKey = new ApiKey();
        Instant future = Instant.now().plusSeconds(86400);

        apiKey.setExpiresAt(future);

        assertThat(apiKey.getExpiresAt()).isEqualTo(future);
    }

    @Test
    void shouldSetAndGetLastUsedAt() {
        ApiKey apiKey = new ApiKey();
        Instant now = Instant.now();

        apiKey.setLastUsedAt(now);

        assertThat(apiKey.getLastUsedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetLastUsedIp() {
        ApiKey apiKey = new ApiKey();

        apiKey.setLastUsedIp("10.0.0.1");

        assertThat(apiKey.getLastUsedIp()).isEqualTo("10.0.0.1");
    }

    @Test
    void shouldCreateApiKeyFromFixtures() {
        ApiKey apiKey = AdminTestFixtures.createApiKey();

        assertThat(apiKey).isNotNull();
        assertThat(apiKey.getId()).isNotNull();
        assertThat(apiKey.getTenantId()).isNotNull();
        assertThat(apiKey.getName()).isNotNull();
        assertThat(apiKey.getKeyHash()).isNotNull();
        assertThat(apiKey.getScopes()).isNotEmpty();
        assertThat(apiKey.getStatus()).isEqualTo(ApiKeyStatus.ACTIVE);
    }

    @Test
    void shouldCreateApiKeyWithCustomScopes() {
        Set<ApiKeyScope> scopes = Set.of(ApiKeyScope.ADMIN, ApiKeyScope.EVENTS_WRITE);
        ApiKey apiKey = AdminTestFixtures.createApiKeyWithScopes(scopes);

        assertThat(apiKey.getScopes()).isEqualTo(scopes);
    }

    @Test
    void shouldCreateApiKeyWithDifferentStatuses() {
        for (ApiKeyStatus status : ApiKeyStatus.values()) {
            ApiKey apiKey = AdminTestFixtures.createApiKeyWithStatus(status);

            assertThat(apiKey.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void shouldCreateExpiredApiKey() {
        ApiKey apiKey = AdminTestFixtures.createExpiredApiKey();

        assertThat(apiKey.getStatus()).isEqualTo(ApiKeyStatus.EXPIRED);
        assertThat(apiKey.getExpiresAt()).isBefore(Instant.now());
    }
}
