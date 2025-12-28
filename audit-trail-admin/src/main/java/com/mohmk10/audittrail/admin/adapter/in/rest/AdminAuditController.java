package com.mohmk10.audittrail.admin.adapter.in.rest;

import com.mohmk10.audittrail.admin.adapter.in.rest.dto.AdminAuditLogResponse;
import com.mohmk10.audittrail.admin.application.AdminAuditService;
import com.mohmk10.audittrail.admin.domain.AdminAction;
import com.mohmk10.audittrail.admin.domain.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants/{tenantId}/audit-logs")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    public AdminAuditController(AdminAuditService adminAuditService) {
        this.adminAuditService = adminAuditService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminAuditLogResponse>> listAuditLogs(
            @PathVariable String tenantId,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) AdminAction action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Pageable pageable) {
        Page<AdminAuditLog> logs;
        // Use simple query when no filters are provided (avoids PostgreSQL parameter type issues)
        if (actorId == null && action == null && resourceType == null && from == null && to == null) {
            logs = adminAuditService.findByTenantId(tenantId, pageable);
        } else {
            logs = adminAuditService.findByFilters(tenantId, actorId, action, resourceType, from, to, pageable);
        }
        return ResponseEntity.ok(logs.map(AdminAuditLogResponse::from));
    }

    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<List<AdminAuditLogResponse>> getResourceHistory(
            @PathVariable String tenantId,
            @PathVariable String resourceType,
            @PathVariable String resourceId) {
        List<AdminAuditLog> logs = adminAuditService.findByResource(resourceType, resourceId);
        List<AdminAuditLogResponse> responses = logs.stream()
                .map(AdminAuditLogResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @PathVariable String tenantId,
            @RequestParam(defaultValue = "24") int hoursAgo) {
        Instant since = Instant.now().minusSeconds(hoursAgo * 3600L);
        Map<AdminAction, Long> actionStats = adminAuditService.getActionStatsSince(tenantId, since);
        long todayCount = adminAuditService.countByTenantIdToday(tenantId);

        Map<String, Object> stats = Map.of(
                "totalToday", todayCount,
                "actionBreakdown", actionStats,
                "period", Map.of(
                        "hours", hoursAgo,
                        "since", since
                )
        );
        return ResponseEntity.ok(stats);
    }
}
