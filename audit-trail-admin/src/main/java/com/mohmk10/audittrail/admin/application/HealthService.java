package com.mohmk10.audittrail.admin.application;

import com.mohmk10.audittrail.admin.domain.SystemHealth;
import com.mohmk10.audittrail.admin.domain.TenantStats;

import java.util.UUID;

public interface HealthService {

    SystemHealth getSystemHealth();

    TenantStats getTenantStats(String tenantId);
}
