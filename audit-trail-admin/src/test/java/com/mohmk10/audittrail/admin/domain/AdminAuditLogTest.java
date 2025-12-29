package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdminAuditLogTest {

    @Test
    void shouldBuildAdminAuditLogWithAllFields() {
        UUID id = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Instant now = Instant.now();
        Map<String, Object> previousState = Map.of("status", "INACTIVE");
        Map<String, Object> newState = Map.of("status", "ACTIVE");

        AdminAuditLog log = AdminAuditLog.builder()
                .id(id)
                .tenantId("tenant-001")
                .actorId(actorId)
                .actorEmail("admin@example.com")
                .action(AdminAction.USER_ACTIVATED)
                .resourceType("User")
                .resourceId("user-123")
                .previousState(previousState)
                .newState(newState)
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .timestamp(now)
                .details("Activated user account")
                .build();

        assertThat(log.getId()).isEqualTo(id);
        assertThat(log.getTenantId()).isEqualTo("tenant-001");
        assertThat(log.getActorId()).isEqualTo(actorId);
        assertThat(log.getActorEmail()).isEqualTo("admin@example.com");
        assertThat(log.getAction()).isEqualTo(AdminAction.USER_ACTIVATED);
        assertThat(log.getResourceType()).isEqualTo("User");
        assertThat(log.getResourceId()).isEqualTo("user-123");
        assertThat(log.getPreviousState()).isEqualTo(previousState);
        assertThat(log.getNewState()).isEqualTo(newState);
        assertThat(log.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(log.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(log.getTimestamp()).isEqualTo(now);
        assertThat(log.getDetails()).isEqualTo("Activated user account");
    }

    @Test
    void shouldCreateEmptyAdminAuditLog() {
        AdminAuditLog log = new AdminAuditLog();

        assertThat(log.getId()).isNull();
        assertThat(log.getAction()).isNull();
        assertThat(log.getResourceType()).isNull();
    }

    @Test
    void shouldSetAndGetId() {
        AdminAuditLog log = new AdminAuditLog();
        UUID id = UUID.randomUUID();

        log.setId(id);

        assertThat(log.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetTenantId() {
        AdminAuditLog log = new AdminAuditLog();

        log.setTenantId("tenant-002");

        assertThat(log.getTenantId()).isEqualTo("tenant-002");
    }

    @Test
    void shouldSetAndGetActorId() {
        AdminAuditLog log = new AdminAuditLog();
        UUID actorId = UUID.randomUUID();

        log.setActorId(actorId);

        assertThat(log.getActorId()).isEqualTo(actorId);
    }

    @Test
    void shouldSetAndGetActorEmail() {
        AdminAuditLog log = new AdminAuditLog();

        log.setActorEmail("user@example.com");

        assertThat(log.getActorEmail()).isEqualTo("user@example.com");
    }

    @Test
    void shouldSetAndGetAction() {
        AdminAuditLog log = new AdminAuditLog();

        log.setAction(AdminAction.SOURCE_CREATED);

        assertThat(log.getAction()).isEqualTo(AdminAction.SOURCE_CREATED);
    }

    @Test
    void shouldSetAndGetResourceType() {
        AdminAuditLog log = new AdminAuditLog();

        log.setResourceType("ApiKey");

        assertThat(log.getResourceType()).isEqualTo("ApiKey");
    }

    @Test
    void shouldSetAndGetResourceId() {
        AdminAuditLog log = new AdminAuditLog();

        log.setResourceId("resource-456");

        assertThat(log.getResourceId()).isEqualTo("resource-456");
    }

    @Test
    void shouldSetAndGetPreviousState() {
        AdminAuditLog log = new AdminAuditLog();
        Map<String, Object> state = Map.of("key", "oldValue");

        log.setPreviousState(state);

        assertThat(log.getPreviousState()).isEqualTo(state);
    }

    @Test
    void shouldSetAndGetNewState() {
        AdminAuditLog log = new AdminAuditLog();
        Map<String, Object> state = Map.of("key", "newValue");

        log.setNewState(state);

        assertThat(log.getNewState()).isEqualTo(state);
    }

    @Test
    void shouldSetAndGetIpAddress() {
        AdminAuditLog log = new AdminAuditLog();

        log.setIpAddress("10.0.0.1");

        assertThat(log.getIpAddress()).isEqualTo("10.0.0.1");
    }

    @Test
    void shouldSetAndGetUserAgent() {
        AdminAuditLog log = new AdminAuditLog();

        log.setUserAgent("Chrome/100.0");

        assertThat(log.getUserAgent()).isEqualTo("Chrome/100.0");
    }

    @Test
    void shouldSetAndGetTimestamp() {
        AdminAuditLog log = new AdminAuditLog();
        Instant now = Instant.now();

        log.setTimestamp(now);

        assertThat(log.getTimestamp()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetDetails() {
        AdminAuditLog log = new AdminAuditLog();

        log.setDetails("Additional details");

        assertThat(log.getDetails()).isEqualTo("Additional details");
    }

    @Test
    void shouldCreateAdminAuditLogFromFixtures() {
        AdminAuditLog log = AdminTestFixtures.createAdminAuditLog();

        assertThat(log).isNotNull();
        assertThat(log.getId()).isNotNull();
        assertThat(log.getTenantId()).isNotNull();
        assertThat(log.getActorId()).isNotNull();
        assertThat(log.getAction()).isNotNull();
        assertThat(log.getTimestamp()).isNotNull();
    }

    @Test
    void shouldCreateAdminAuditLogWithDifferentActions() {
        AdminAuditLog userLog = AdminTestFixtures.createAdminAuditLogWithAction(AdminAction.USER_CREATED);
        AdminAuditLog tenantLog = AdminTestFixtures.createAdminAuditLogWithAction(AdminAction.TENANT_CREATED);
        AdminAuditLog apiKeyLog = AdminTestFixtures.createAdminAuditLogWithAction(AdminAction.API_KEY_CREATED);

        assertThat(userLog.getAction()).isEqualTo(AdminAction.USER_CREATED);
        assertThat(userLog.getResourceType()).isEqualTo("User");

        assertThat(tenantLog.getAction()).isEqualTo(AdminAction.TENANT_CREATED);
        assertThat(tenantLog.getResourceType()).isEqualTo("Tenant");

        assertThat(apiKeyLog.getAction()).isEqualTo(AdminAction.API_KEY_CREATED);
        assertThat(apiKeyLog.getResourceType()).isEqualTo("ApiKey");
    }
}
