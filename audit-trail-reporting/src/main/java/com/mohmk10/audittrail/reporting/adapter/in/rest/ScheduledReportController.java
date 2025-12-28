package com.mohmk10.audittrail.reporting.adapter.in.rest;

import com.mohmk10.audittrail.reporting.adapter.in.rest.dto.ScheduleReportRequest;
import com.mohmk10.audittrail.reporting.adapter.in.rest.dto.ScheduledReportResponse;
import com.mohmk10.audittrail.reporting.adapter.out.persistence.ScheduledReportRepository;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import com.mohmk10.audittrail.reporting.domain.ScheduledReport;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports/scheduled")
public class ScheduledReportController {

    private final ScheduledReportRepository scheduledReportRepository;

    public ScheduledReportController(ScheduledReportRepository scheduledReportRepository) {
        this.scheduledReportRepository = scheduledReportRepository;
    }

    @PostMapping
    public ResponseEntity<ScheduledReportResponse> schedule(@RequestBody @Valid ScheduleReportRequest request) {
        ScheduledReport scheduledReport = ScheduledReport.builder()
                .id(UUID.randomUUID())
                .name(request.name())
                .type(ReportType.valueOf(request.type()))
                .format(ReportFormat.valueOf(request.format()))
                .tenantId(request.tenantId())
                .cronExpression(request.cronExpression())
                .enabled(true)
                .recipients(request.recipients())
                .createdAt(Instant.now())
                .build();

        ScheduledReport saved = scheduledReportRepository.save(scheduledReport);
        return ResponseEntity.ok(ScheduledReportResponse.from(saved));
    }

    @GetMapping
    public ResponseEntity<List<ScheduledReportResponse>> list(@RequestParam String tenantId) {
        List<ScheduledReportResponse> reports = scheduledReportRepository.findByTenantId(tenantId)
                .stream()
                .map(ScheduledReportResponse::from)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        scheduledReportRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ScheduledReportResponse> pause(@PathVariable UUID id) {
        ScheduledReport report = scheduledReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled report not found: " + id));

        ScheduledReport updated = report.toBuilder()
                .enabled(false)
                .build();

        ScheduledReport saved = scheduledReportRepository.save(updated);
        return ResponseEntity.ok(ScheduledReportResponse.from(saved));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ScheduledReportResponse> resume(@PathVariable UUID id) {
        ScheduledReport report = scheduledReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled report not found: " + id));

        ScheduledReport updated = report.toBuilder()
                .enabled(true)
                .build();

        ScheduledReport saved = scheduledReportRepository.save(updated);
        return ResponseEntity.ok(ScheduledReportResponse.from(saved));
    }
}
