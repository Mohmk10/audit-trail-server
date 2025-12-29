package com.mohmk10.audittrail.reporting.adapter.in.rest;

import com.mohmk10.audittrail.reporting.adapter.in.rest.dto.GenerateReportRequest;
import com.mohmk10.audittrail.reporting.adapter.in.rest.dto.ReportResponse;
import com.mohmk10.audittrail.reporting.adapter.in.rest.dto.ReportTemplateResponse;
import com.mohmk10.audittrail.reporting.adapter.out.persistence.ReportRepository;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.domain.ReportStatus;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import com.mohmk10.audittrail.reporting.service.ReportGenerationService;
import com.mohmk10.audittrail.reporting.service.ReportRequest;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportGenerationService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportTemplate template;

    private ReportController controller;

    @BeforeEach
    void setUp() {
        lenient().when(template.getName()).thenReturn("Audit Report");
        lenient().when(template.getDescription()).thenReturn("Standard audit report template");
        lenient().when(template.getType()).thenReturn(ReportType.AUDIT);
        controller = new ReportController(reportService, reportRepository, List.of(template));
    }

    @Test
    void shouldGenerateReportAndReturn200() {
        Report report = createTestReport(ReportStatus.COMPLETED);
        when(reportService.generate(any(ReportRequest.class))).thenReturn(report);

        GenerateReportRequest request = new GenerateReportRequest(
                "Test Report",
                "AUDIT",
                "PDF",
                "tenant-001",
                null, null, null, null, null, null
        );

        ResponseEntity<ReportResponse> response = controller.generate(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Test Report");
    }

    @Test
    void shouldGetReportStatusAndReturn200() {
        UUID reportId = UUID.randomUUID();
        Report report = createTestReport(ReportStatus.COMPLETED);
        when(reportService.getStatus(reportId)).thenReturn(report);

        ResponseEntity<ReportResponse> response = controller.getStatus(reportId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldDownloadReportAndReturnFile() {
        UUID reportId = UUID.randomUUID();
        byte[] content = "PDF content".getBytes();
        Report report = createTestReport(ReportStatus.COMPLETED);

        when(reportService.getStatus(reportId)).thenReturn(report);
        when(reportService.download(reportId)).thenReturn(content);

        ResponseEntity<Resource> response = controller.download(reportId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldSetCorrectContentTypeForPdf() {
        UUID reportId = UUID.randomUUID();
        Report report = createTestReport(ReportStatus.COMPLETED);

        when(reportService.getStatus(reportId)).thenReturn(report);
        when(reportService.download(reportId)).thenReturn("content".getBytes());

        ResponseEntity<Resource> response = controller.download(reportId);

        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(response.getHeaders().getContentType().toString()).contains("pdf");
    }

    @Test
    void shouldSetChecksumHeader() {
        UUID reportId = UUID.randomUUID();
        Report report = createTestReport(ReportStatus.COMPLETED);

        when(reportService.getStatus(reportId)).thenReturn(report);
        when(reportService.download(reportId)).thenReturn("content".getBytes());

        ResponseEntity<Resource> response = controller.download(reportId);

        assertThat(response.getHeaders().get("X-Report-Checksum")).isNotNull();
    }

    @Test
    void shouldListReportsByTenantId() {
        Report report1 = createTestReport(ReportStatus.COMPLETED);
        Report report2 = createTestReport(ReportStatus.COMPLETED);

        when(reportRepository.findByTenantId("tenant-001")).thenReturn(List.of(report1, report2));

        ResponseEntity<List<ReportResponse>> response = controller.list("tenant-001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldDeleteReportAndReturn204() {
        UUID reportId = UUID.randomUUID();

        ResponseEntity<Void> response = controller.delete(reportId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(reportService).delete(reportId);
    }

    @Test
    void shouldListTemplates() {
        ResponseEntity<List<ReportTemplateResponse>> response = controller.getTemplates();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void shouldHandleCsvFormat() {
        UUID reportId = UUID.randomUUID();
        Report report = Report.builder()
                .id(reportId)
                .name("Test CSV")
                .type(ReportType.AUDIT)
                .format(ReportFormat.CSV)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .checksum("checksum")
                .build();

        when(reportService.getStatus(reportId)).thenReturn(report);
        when(reportService.download(reportId)).thenReturn("csv".getBytes());

        ResponseEntity<Resource> response = controller.download(reportId);

        assertThat(response.getHeaders().getContentType().toString()).contains("csv");
    }

    @Test
    void shouldHandleExcelFormat() {
        UUID reportId = UUID.randomUUID();
        Report report = Report.builder()
                .id(reportId)
                .name("Test Excel")
                .type(ReportType.AUDIT)
                .format(ReportFormat.XLSX)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .checksum("checksum")
                .build();

        when(reportService.getStatus(reportId)).thenReturn(report);
        when(reportService.download(reportId)).thenReturn("xlsx".getBytes());

        ResponseEntity<Resource> response = controller.download(reportId);

        assertThat(response.getHeaders().getContentType().toString()).contains("spreadsheet");
    }

    @Test
    void shouldHandleJsonFormat() {
        UUID reportId = UUID.randomUUID();
        Report report = Report.builder()
                .id(reportId)
                .name("Test JSON")
                .type(ReportType.AUDIT)
                .format(ReportFormat.JSON)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .checksum("checksum")
                .build();

        when(reportService.getStatus(reportId)).thenReturn(report);
        when(reportService.download(reportId)).thenReturn("json".getBytes());

        ResponseEntity<Resource> response = controller.download(reportId);

        assertThat(response.getHeaders().getContentType().toString()).contains("json");
    }

    private Report createTestReport(ReportStatus status) {
        return Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(status)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .checksum("checksum123")
                .signature("signature456")
                .filePath("/reports/test.pdf")
                .fileSize(1024L)
                .build();
    }
}
