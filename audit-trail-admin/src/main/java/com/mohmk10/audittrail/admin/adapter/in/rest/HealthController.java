package com.mohmk10.audittrail.admin.adapter.in.rest;

import com.mohmk10.audittrail.admin.adapter.in.rest.dto.SystemHealthResponse;
import com.mohmk10.audittrail.admin.adapter.in.rest.dto.TenantStatsResponse;
import com.mohmk10.audittrail.admin.application.HealthService;
import com.mohmk10.audittrail.admin.domain.HealthStatus;
import com.mohmk10.audittrail.admin.domain.SystemHealth;
import com.mohmk10.audittrail.admin.domain.TenantStats;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/health")
    public ResponseEntity<SystemHealthResponse> getSystemHealth() {
        SystemHealth health = healthService.getSystemHealth();
        SystemHealthResponse response = SystemHealthResponse.from(health);

        if (health.getOverallStatus() == HealthStatus.UNHEALTHY) {
            return ResponseEntity.status(503).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health/live")
    public ResponseEntity<String> liveness() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/health/ready")
    public ResponseEntity<String> readiness() {
        SystemHealth health = healthService.getSystemHealth();
        if (health.getOverallStatus() == HealthStatus.UNHEALTHY) {
            return ResponseEntity.status(503).body("NOT READY");
        }
        return ResponseEntity.ok("READY");
    }

    @GetMapping("/tenants/{tenantId}/stats")
    public ResponseEntity<TenantStatsResponse> getTenantStats(@PathVariable String tenantId) {
        TenantStats stats = healthService.getTenantStats(tenantId);
        return ResponseEntity.ok(TenantStatsResponse.from(stats));
    }
}
