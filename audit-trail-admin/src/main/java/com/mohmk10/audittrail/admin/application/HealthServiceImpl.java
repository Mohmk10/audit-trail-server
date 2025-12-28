package com.mohmk10.audittrail.admin.application;

import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.UserRepository;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.JpaApiKeyRepository;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.JpaSourceRepository;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.JpaTenantRepository;
import com.mohmk10.audittrail.admin.domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class HealthServiceImpl implements HealthService {

    private final DataSource dataSource;
    private final JpaTenantRepository tenantRepository;
    private final JpaSourceRepository sourceRepository;
    private final JpaApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final Instant startTime;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    public HealthServiceImpl(DataSource dataSource, JpaTenantRepository tenantRepository,
                             JpaSourceRepository sourceRepository, JpaApiKeyRepository apiKeyRepository,
                             UserRepository userRepository) {
        this.dataSource = dataSource;
        this.tenantRepository = tenantRepository;
        this.sourceRepository = sourceRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.userRepository = userRepository;
        this.startTime = Instant.now();
    }

    @Override
    public SystemHealth getSystemHealth() {
        List<ComponentHealth> components = new ArrayList<>();

        // Check PostgreSQL
        components.add(checkPostgres());

        // Calculate overall status
        HealthStatus overallStatus = calculateOverallStatus(components);

        // Build metrics
        Map<String, Object> metrics = buildMetrics();

        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        return SystemHealth.builder()
                .overallStatus(overallStatus)
                .components(components)
                .version(appVersion)
                .startTime(startTime)
                .uptimeSeconds(uptimeSeconds)
                .metrics(metrics)
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public TenantStats getTenantStats(String tenantId) {
        var tenantOpt = tenantRepository.findBySlug(tenantId);
        if (tenantOpt.isEmpty()) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }
        var tenant = tenantOpt.get();

        int activeSources = sourceRepository.countByTenantIdAndStatus(tenantId, SourceStatus.ACTIVE);
        int activeApiKeys = apiKeyRepository.countByTenantIdAndStatus(tenantId, ApiKeyStatus.ACTIVE);
        long activeUsers = userRepository.countByTenantIdAndStatus(tenantId, UserStatus.ACTIVE);

        return TenantStats.builder()
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .activeSources(activeSources)
                .activeUsers((int) activeUsers)
                .activeApiKeys(activeApiKeys)
                .calculatedAt(Instant.now())
                .build();
    }

    private ComponentHealth checkPostgres() {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("SELECT 1");
            long responseTime = System.currentTimeMillis() - startTime;

            Map<String, Object> details = new HashMap<>();
            details.put("database", connection.getCatalog());
            details.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
            details.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());

            return ComponentHealth.builder()
                    .name("PostgreSQL")
                    .status(HealthStatus.HEALTHY)
                    .responseTimeMs(responseTime)
                    .details(details)
                    .lastChecked(Instant.now())
                    .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return ComponentHealth.builder()
                    .name("PostgreSQL")
                    .status(HealthStatus.UNHEALTHY)
                    .message(e.getMessage())
                    .responseTimeMs(responseTime)
                    .lastChecked(Instant.now())
                    .build();
        }
    }

    private HealthStatus calculateOverallStatus(List<ComponentHealth> components) {
        boolean hasUnhealthy = components.stream()
                .anyMatch(c -> c.getStatus() == HealthStatus.UNHEALTHY);
        if (hasUnhealthy) {
            return HealthStatus.UNHEALTHY;
        }

        boolean hasDegraded = components.stream()
                .anyMatch(c -> c.getStatus() == HealthStatus.DEGRADED);
        if (hasDegraded) {
            return HealthStatus.DEGRADED;
        }

        return HealthStatus.HEALTHY;
    }

    private Map<String, Object> buildMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        metrics.put("memory.total", runtime.totalMemory());
        metrics.put("memory.free", runtime.freeMemory());
        metrics.put("memory.used", runtime.totalMemory() - runtime.freeMemory());
        metrics.put("memory.max", runtime.maxMemory());
        metrics.put("processors", runtime.availableProcessors());

        // Count entities
        metrics.put("tenants.total", tenantRepository.count());
        metrics.put("sources.total", sourceRepository.count());
        metrics.put("apiKeys.total", apiKeyRepository.count());
        metrics.put("users.total", userRepository.count());

        return metrics;
    }
}
