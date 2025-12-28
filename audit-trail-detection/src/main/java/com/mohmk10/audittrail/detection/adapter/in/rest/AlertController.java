package com.mohmk10.audittrail.detection.adapter.in.rest;

import com.mohmk10.audittrail.detection.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.detection.adapter.out.persistence.repository.JpaAlertRepository;
import com.mohmk10.audittrail.detection.domain.Alert;
import com.mohmk10.audittrail.detection.domain.AlertStatus;
import com.mohmk10.audittrail.detection.domain.Severity;
import com.mohmk10.audittrail.detection.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;
    private final JpaAlertRepository alertRepository;
    private final RuleDtoMapper mapper;

    public AlertController(
            AlertService alertService,
            JpaAlertRepository alertRepository,
            RuleDtoMapper mapper) {
        this.alertService = alertService;
        this.alertRepository = alertRepository;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<AlertResponse>> list(
            @RequestParam String tenantId,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "triggeredAt"));

        Page<Alert> alerts;
        if (status != null) {
            alerts = alertService.findByTenantId(tenantId, pageRequest);
        } else {
            alerts = alertService.findByTenantId(tenantId, pageRequest);
        }

        List<AlertResponse> responses = alerts.getContent()
                .stream()
                .map(mapper::toAlertResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stats")
    public ResponseEntity<AlertStatsResponse> stats(@RequestParam String tenantId) {
        long totalOpen = alertRepository.countByTenantIdAndStatus(tenantId, AlertStatus.OPEN);
        long totalAcknowledged = alertRepository.countByTenantIdAndStatus(tenantId, AlertStatus.ACKNOWLEDGED);
        long totalResolved = alertRepository.countByTenantIdAndStatus(tenantId, AlertStatus.RESOLVED);
        long totalDismissed = alertRepository.countByTenantIdAndStatus(tenantId, AlertStatus.DISMISSED);

        Map<Severity, Long> bySeverity = new EnumMap<>(Severity.class);
        for (Severity severity : Severity.values()) {
            long count = alertRepository.findByTenantIdAndStatus(tenantId, AlertStatus.OPEN)
                    .stream()
                    .filter(a -> a.getSeverity() == severity)
                    .count();
            bySeverity.put(severity, count);
        }

        return ResponseEntity.ok(new AlertStatsResponse(
                totalOpen,
                totalAcknowledged,
                totalResolved,
                totalDismissed,
                bySeverity
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertResponse> getById(@PathVariable UUID id) {
        return alertService.findById(id)
                .map(mapper::toAlertResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<AlertResponse> acknowledge(
            @PathVariable UUID id,
            @RequestBody @Valid AcknowledgeAlertRequest request) {
        Alert acknowledged = alertService.acknowledge(id, request.acknowledgedBy());
        return ResponseEntity.ok(mapper.toAlertResponse(acknowledged));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<AlertResponse> resolve(
            @PathVariable UUID id,
            @RequestBody @Valid ResolveAlertRequest request) {
        Alert resolved = alertService.resolve(id, request.resolution());
        return ResponseEntity.ok(mapper.toAlertResponse(resolved));
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<AlertResponse> dismiss(@PathVariable UUID id) {
        Alert dismissed = alertService.dismiss(id);
        return ResponseEntity.ok(mapper.toAlertResponse(dismissed));
    }
}
