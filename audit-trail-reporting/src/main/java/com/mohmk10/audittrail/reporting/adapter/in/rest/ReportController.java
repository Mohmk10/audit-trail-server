package com.mohmk10.audittrail.reporting.adapter.in.rest;

import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.reporting.adapter.in.rest.dto.GenerateReportRequest;
import com.mohmk10.audittrail.reporting.adapter.in.rest.dto.ReportResponse;
import com.mohmk10.audittrail.reporting.adapter.in.rest.dto.ReportTemplateResponse;
import com.mohmk10.audittrail.reporting.adapter.out.persistence.ReportRepository;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import com.mohmk10.audittrail.reporting.service.ReportGenerationService;
import com.mohmk10.audittrail.reporting.service.ReportRequest;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportGenerationService reportGenerationService;
    private final ReportRepository reportRepository;
    private final List<ReportTemplate> templates;

    public ReportController(
            ReportGenerationService reportGenerationService,
            ReportRepository reportRepository,
            List<ReportTemplate> templates) {
        this.reportGenerationService = reportGenerationService;
        this.reportRepository = reportRepository;
        this.templates = templates;
    }

    @PostMapping
    public ResponseEntity<ReportResponse> generate(@RequestBody @Valid GenerateReportRequest request) {
        DateRange dateRange = null;
        if (request.fromDate() != null || request.toDate() != null) {
            dateRange = new DateRange(request.fromDate(), request.toDate());
        }

        ReportRequest reportRequest = ReportRequest.builder()
                .name(request.name())
                .type(ReportType.valueOf(request.type()))
                .format(ReportFormat.valueOf(request.format()))
                .tenantId(request.tenantId())
                .actorId(request.actorId())
                .actionType(request.actionType())
                .resourceType(request.resourceType())
                .dateRange(dateRange)
                .parameters(request.parameters())
                .build();

        Report report = reportGenerationService.generate(reportRequest);
        return ResponseEntity.ok(ReportResponse.from(report));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getStatus(@PathVariable UUID id) {
        Report report = reportGenerationService.getStatus(id);
        return ResponseEntity.ok(ReportResponse.from(report));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        Report report = reportGenerationService.getStatus(id);
        byte[] content = reportGenerationService.download(id);

        String filename = report.name().replaceAll("[^a-zA-Z0-9.-]", "_") + "." + report.format().name().toLowerCase();
        MediaType mediaType = getMediaType(report.format());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header("X-Report-Checksum", report.checksum())
                .body(new ByteArrayResource(content));
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> list(@RequestParam String tenantId) {
        List<ReportResponse> reports = reportRepository.findByTenantId(tenantId)
                .stream()
                .map(ReportResponse::from)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reportGenerationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/templates")
    public ResponseEntity<List<ReportTemplateResponse>> getTemplates() {
        List<ReportTemplateResponse> templateResponses = templates.stream()
                .map(ReportTemplateResponse::from)
                .toList();
        return ResponseEntity.ok(templateResponses);
    }

    private MediaType getMediaType(ReportFormat format) {
        return switch (format) {
            case PDF -> MediaType.APPLICATION_PDF;
            case CSV -> MediaType.parseMediaType("text/csv");
            case XLSX -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case JSON -> MediaType.APPLICATION_JSON;
        };
    }
}
