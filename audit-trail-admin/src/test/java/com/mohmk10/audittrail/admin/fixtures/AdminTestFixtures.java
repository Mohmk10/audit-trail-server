package com.mohmk10.audittrail.admin.fixtures;

import com.mohmk10.audittrail.admin.domain.*;

import java.time.Instant;
import java.util.*;

public final class AdminTestFixtures {

    public static final String DEFAULT_TENANT_ID = "tenant-001";
    public static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID DEFAULT_SOURCE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID DEFAULT_API_KEY_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private AdminTestFixtures() {
    }

    // ==================== Tenant ====================

    public static Tenant createTenant() {
        return Tenant.builder()
                .id(UUID.randomUUID())
                .name("Test Tenant")
                .slug("test-tenant")
                .description("A test tenant for unit testing")
                .status(TenantStatus.ACTIVE)
                .plan(TenantPlan.PRO)
                .quota(createTenantQuota())
                .settings(Map.of("timezone", "UTC", "locale", "en_US"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Tenant createTenantWithPlan(TenantPlan plan) {
        return Tenant.builder()
                .id(UUID.randomUUID())
                .name("Tenant " + plan.name())
                .slug("tenant-" + plan.name().toLowerCase())
                .status(TenantStatus.ACTIVE)
                .plan(plan)
                .quota(createQuotaForPlan(plan))
                .createdAt(Instant.now())
                .build();
    }

    public static Tenant createTenantWithStatus(TenantStatus status) {
        return Tenant.builder()
                .id(UUID.randomUUID())
                .name("Tenant " + status.name())
                .slug("tenant-" + status.name().toLowerCase())
                .status(status)
                .plan(TenantPlan.STARTER)
                .createdAt(Instant.now())
                .build();
    }

    // ==================== TenantQuota ====================

    public static TenantQuota createTenantQuota() {
        return new TenantQuota(10000L, 300000L, 10, 20, 50, 90);
    }

    public static TenantQuota createQuotaForPlan(TenantPlan plan) {
        return switch (plan) {
            case FREE -> new TenantQuota(1000L, 30000L, 2, 2, 2, 30);
            case STARTER -> new TenantQuota(10000L, 300000L, 5, 10, 10, 60);
            case PRO -> new TenantQuota(100000L, 3000000L, 20, 50, 100, 180);
            case ENTERPRISE -> new TenantQuota(1000000L, 30000000L, 100, 200, 500, 365);
        };
    }

    // ==================== Source ====================

    public static Source createSource() {
        return Source.builder()
                .id(DEFAULT_SOURCE_ID)
                .tenantId(DEFAULT_TENANT_ID)
                .name("Test Source")
                .description("A test source for unit testing")
                .type(SourceType.WEB_APP)
                .status(SourceStatus.ACTIVE)
                .config(Map.of("environment", "test"))
                .createdAt(Instant.now())
                .eventCount(0L)
                .build();
    }

    public static Source createSourceWithType(SourceType type) {
        return Source.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .name("Source " + type.name())
                .type(type)
                .status(SourceStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    public static Source createSourceWithStatus(SourceStatus status) {
        return Source.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .name("Source " + status.name())
                .type(SourceType.BACKEND_SERVICE)
                .status(status)
                .createdAt(Instant.now())
                .build();
    }

    // ==================== ApiKey ====================

    public static ApiKey createApiKey() {
        return ApiKey.builder()
                .id(DEFAULT_API_KEY_ID)
                .tenantId(DEFAULT_TENANT_ID)
                .sourceId(DEFAULT_SOURCE_ID)
                .name("Test API Key")
                .keyHash("hashed_key_value")
                .keyPrefix("atk_test")
                .scopes(Set.of(ApiKeyScope.EVENTS_WRITE, ApiKeyScope.EVENTS_READ))
                .status(ApiKeyStatus.ACTIVE)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86400 * 365))
                .build();
    }

    public static ApiKey createApiKeyWithScopes(Set<ApiKeyScope> scopes) {
        return ApiKey.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .name("API Key with scopes")
                .keyHash("hashed_key_value")
                .keyPrefix("atk_scope")
                .scopes(scopes)
                .status(ApiKeyStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    public static ApiKey createApiKeyWithStatus(ApiKeyStatus status) {
        return ApiKey.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .name("API Key " + status.name())
                .keyHash("hashed_key_value")
                .keyPrefix("atk_stat")
                .scopes(Set.of(ApiKeyScope.EVENTS_READ))
                .status(status)
                .createdAt(Instant.now())
                .build();
    }

    public static ApiKey createExpiredApiKey() {
        return ApiKey.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .name("Expired API Key")
                .keyHash("hashed_key_value")
                .keyPrefix("atk_exp")
                .scopes(Set.of(ApiKeyScope.EVENTS_READ))
                .status(ApiKeyStatus.EXPIRED)
                .createdAt(Instant.now().minusSeconds(86400 * 365))
                .expiresAt(Instant.now().minusSeconds(3600))
                .build();
    }

    public static ApiKeyCreationResult createApiKeyCreationResult() {
        return new ApiKeyCreationResult(createApiKey(), "atk_test_plain_text_key_value_12345");
    }

    // ==================== User ====================

    public static User createUser() {
        return User.builder()
                .id(DEFAULT_USER_ID)
                .tenantId(DEFAULT_TENANT_ID)
                .email("test@example.com")
                .passwordHash("hashed_password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static User createUserWithRole(Role role) {
        return User.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .email(role.name().toLowerCase() + "@example.com")
                .passwordHash("hashed_password")
                .firstName(role.name())
                .lastName("User")
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    public static User createUserWithStatus(UserStatus status) {
        return User.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .email("user_" + status.name().toLowerCase() + "@example.com")
                .passwordHash("hashed_password")
                .firstName("Test")
                .lastName("User")
                .role(Role.VIEWER)
                .status(status)
                .createdAt(Instant.now())
                .build();
    }

    public static User createUserWithoutNames() {
        return User.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .email("noname@example.com")
                .passwordHash("hashed_password")
                .role(Role.VIEWER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }

    // ==================== AdminAuditLog ====================

    public static AdminAuditLog createAdminAuditLog() {
        return AdminAuditLog.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .actorId(DEFAULT_USER_ID)
                .actorEmail("admin@example.com")
                .action(AdminAction.USER_CREATED)
                .resourceType("User")
                .resourceId(UUID.randomUUID().toString())
                .previousState(null)
                .newState(Map.of("email", "newuser@example.com", "role", "VIEWER"))
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .timestamp(Instant.now())
                .details("Created new user")
                .build();
    }

    public static AdminAuditLog createAdminAuditLogWithAction(AdminAction action) {
        return AdminAuditLog.builder()
                .id(UUID.randomUUID())
                .tenantId(DEFAULT_TENANT_ID)
                .actorId(DEFAULT_USER_ID)
                .actorEmail("admin@example.com")
                .action(action)
                .resourceType(getResourceTypeForAction(action))
                .resourceId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .build();
    }

    private static String getResourceTypeForAction(AdminAction action) {
        String name = action.name();
        if (name.startsWith("USER_")) return "User";
        if (name.startsWith("TENANT_")) return "Tenant";
        if (name.startsWith("SOURCE_")) return "Source";
        if (name.startsWith("API_KEY_")) return "ApiKey";
        if (name.startsWith("RULE_")) return "Rule";
        if (name.startsWith("REPORT_")) return "Report";
        return "System";
    }

    // ==================== SystemHealth ====================

    public static SystemHealth createSystemHealth() {
        return SystemHealth.builder()
                .overallStatus(HealthStatus.HEALTHY)
                .components(List.of(
                        createHealthyComponent("database"),
                        createHealthyComponent("elasticsearch"),
                        createHealthyComponent("cache")
                ))
                .version("1.0.0")
                .startTime(Instant.now().minusSeconds(3600))
                .uptimeSeconds(3600L)
                .metrics(Map.of(
                        "totalRequests", 1000L,
                        "averageResponseTimeMs", 50L
                ))
                .timestamp(Instant.now())
                .build();
    }

    public static SystemHealth createDegradedSystemHealth() {
        return SystemHealth.builder()
                .overallStatus(HealthStatus.DEGRADED)
                .components(List.of(
                        createHealthyComponent("database"),
                        ComponentHealth.unhealthy("elasticsearch", "Connection timeout")
                ))
                .version("1.0.0")
                .startTime(Instant.now().minusSeconds(3600))
                .uptimeSeconds(3600L)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== ComponentHealth ====================

    public static ComponentHealth createHealthyComponent(String name) {
        return ComponentHealth.builder()
                .name(name)
                .status(HealthStatus.HEALTHY)
                .responseTimeMs(10L)
                .lastChecked(Instant.now())
                .build();
    }

    public static ComponentHealth createUnhealthyComponent(String name, String message) {
        return ComponentHealth.builder()
                .name(name)
                .status(HealthStatus.UNHEALTHY)
                .message(message)
                .lastChecked(Instant.now())
                .build();
    }

    // ==================== TenantStats ====================

    public static TenantStats createTenantStats() {
        return TenantStats.builder()
                .tenantId(UUID.randomUUID())
                .tenantName("Test Tenant")
                .totalEvents(100000L)
                .eventsToday(500L)
                .eventsThisMonth(15000L)
                .storageUsedBytes(1073741824L) // 1 GB
                .activeSources(5)
                .activeUsers(10)
                .activeApiKeys(8)
                .alertsTriggeredToday(3)
                .eventsByType(Map.of("CREATE", 300L, "UPDATE", 150L, "DELETE", 50L))
                .eventsBySource(Map.of("web-app", 400L, "backend", 100L))
                .averageEventsPerDay(500.0)
                .lastEventAt(Instant.now())
                .calculatedAt(Instant.now())
                .build();
    }
}
