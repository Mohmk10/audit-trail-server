package com.mohmk10.audittrail.reporting.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.reporting.adapter.out.persistence.ReportRepository;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.domain.ReportStatus;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import com.mohmk10.audittrail.reporting.fixtures.ReportingTestFixtures;
import com.mohmk10.audittrail.reporting.generator.CsvReportGenerator;
import com.mohmk10.audittrail.reporting.generator.JsonReportGenerator;
import com.mohmk10.audittrail.reporting.generator.PdfReportGenerator;
import com.mohmk10.audittrail.reporting.generator.ReportGenerator;
import com.mohmk10.audittrail.reporting.template.AuditReportTemplate;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import com.mohmk10.audittrail.search.service.EventSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportGenerationServiceImplTest {

    @Mock
    private EventSearchService searchService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportStorageService storageService;

    @Mock
    private ReportCertificationService certificationService;

    private ReportGenerationServiceImpl service;

    @BeforeEach
    void setUp() {
        List<ReportGenerator> generators = List.of(
                new PdfReportGenerator(),
                new CsvReportGenerator(),
                new JsonReportGenerator()
        );
        List<ReportTemplate> templates = List.of(new AuditReportTemplate());

        service = new ReportGenerationServiceImpl(
                searchService,
                reportRepository,
                storageService,
                certificationService,
                generators,
                templates,
                30
        );
    }

    @Test
    void shouldGeneratePdfReport() {
        ReportRequest request = ReportingTestFixtures.createGenerateRequest(ReportFormat.PDF);
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
        when(searchService.search(any(SearchCriteria.class))).thenReturn(SearchResult.of(events, events.size(), 0, 1000));
        when(certificationService.calculateChecksum(any())).thenReturn("checksum123");
        when(certificationService.sign(any(), any())).thenReturn("signature456");
        when(storageService.store(any(), any(), any())).thenReturn("/reports/test.pdf");
        when(storageService.getFileSize(any())).thenReturn(1024L);

        Report result = service.generate(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(result.format()).isEqualTo(ReportFormat.PDF);
    }

    @Test
    void shouldGenerateCsvReport() {
        ReportRequest request = ReportingTestFixtures.createGenerateRequest(ReportFormat.CSV);
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
        when(searchService.search(any(SearchCriteria.class))).thenReturn(SearchResult.of(events, events.size(), 0, 1000));
        when(certificationService.calculateChecksum(any())).thenReturn("checksum123");
        when(certificationService.sign(any(), any())).thenReturn("signature456");
        when(storageService.store(any(), any(), any())).thenReturn("/reports/test.csv");
        when(storageService.getFileSize(any())).thenReturn(512L);

        Report result = service.generate(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(result.format()).isEqualTo(ReportFormat.CSV);
    }

    @Test
    void shouldGenerateJsonReport() {
        ReportRequest request = ReportingTestFixtures.createGenerateRequest(ReportFormat.JSON);
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
        when(searchService.search(any(SearchCriteria.class))).thenReturn(SearchResult.of(events, events.size(), 0, 1000));
        when(certificationService.calculateChecksum(any())).thenReturn("checksum123");
        when(certificationService.sign(any(), any())).thenReturn("signature456");
        when(storageService.store(any(), any(), any())).thenReturn("/reports/test.json");
        when(storageService.getFileSize(any())).thenReturn(2048L);

        Report result = service.generate(request);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(result.format()).isEqualTo(ReportFormat.JSON);
    }

    @Test
    void shouldSearchEventsWithCriteria() {
        ReportRequest request = ReportingTestFixtures.createGenerateRequest();
        List<Event> events = ReportingTestFixtures.createTestEvents(3);

        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
        when(searchService.search(any(SearchCriteria.class))).thenReturn(SearchResult.of(events, events.size(), 0, 1000));
        when(certificationService.calculateChecksum(any())).thenReturn("checksum");
        when(certificationService.sign(any(), any())).thenReturn("signature");
        when(storageService.store(any(), any(), any())).thenReturn("/path");
        when(storageService.getFileSize(any())).thenReturn(100L);

        service.generate(request);

        verify(searchService, atLeastOnce()).search(any(SearchCriteria.class));
    }

    @Test
    void shouldCalculateChecksum() {
        ReportRequest request = ReportingTestFixtures.createGenerateRequest();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
        when(searchService.search(any(SearchCriteria.class))).thenReturn(SearchResult.of(events, events.size(), 0, 1000));
        when(certificationService.calculateChecksum(any())).thenReturn("testchecksum");
        when(certificationService.sign(any(), any())).thenReturn("testsig");
        when(storageService.store(any(), any(), any())).thenReturn("/path");
        when(storageService.getFileSize(any())).thenReturn(100L);

        Report result = service.generate(request);

        assertThat(result.checksum()).isEqualTo("testchecksum");
        verify(certificationService).calculateChecksum(any());
    }

    @Test
    void shouldSaveReportToStorage() {
        ReportRequest request = ReportingTestFixtures.createGenerateRequest();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
        when(searchService.search(any(SearchCriteria.class))).thenReturn(SearchResult.of(events, events.size(), 0, 1000));
        when(certificationService.calculateChecksum(any())).thenReturn("checksum");
        when(certificationService.sign(any(), any())).thenReturn("sig");
        when(storageService.store(any(), any(), any())).thenReturn("/stored/path.pdf");
        when(storageService.getFileSize(any())).thenReturn(100L);

        Report result = service.generate(request);

        assertThat(result.filePath()).isEqualTo("/stored/path.pdf");
        verify(storageService).store(any(), eq("pdf"), any());
    }

    @Test
    void shouldPersistReportMetadata() {
        ReportRequest request = ReportingTestFixtures.createGenerateRequest();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
        when(searchService.search(any(SearchCriteria.class))).thenReturn(SearchResult.of(events, events.size(), 0, 1000));
        when(certificationService.calculateChecksum(any())).thenReturn("checksum");
        when(certificationService.sign(any(), any())).thenReturn("sig");
        when(storageService.store(any(), any(), any())).thenReturn("/path");
        when(storageService.getFileSize(any())).thenReturn(100L);

        service.generate(request);

        verify(reportRepository, atLeast(2)).save(any(Report.class));
    }

    @Test
    void shouldUpdateStatusToCompleted() {
        ReportRequest request = ReportingTestFixtures.createGenerateRequest();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        when(reportRepository.save(reportCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
        when(searchService.search(any(SearchCriteria.class))).thenReturn(SearchResult.of(events, events.size(), 0, 1000));
        when(certificationService.calculateChecksum(any())).thenReturn("checksum");
        when(certificationService.sign(any(), any())).thenReturn("sig");
        when(storageService.store(any(), any(), any())).thenReturn("/path");
        when(storageService.getFileSize(any())).thenReturn(100L);

        Report result = service.generate(request);

        assertThat(result.status()).isEqualTo(ReportStatus.COMPLETED);
    }

    @Test
    void shouldHandleGenerationError() {
        ReportRequest request = ReportingTestFixtures.createGenerateRequest();

        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
        when(searchService.search(any(SearchCriteria.class))).thenThrow(new RuntimeException("Search failed"));

        Report result = service.generate(request);

        assertThat(result.status()).isEqualTo(ReportStatus.FAILED);
        assertThat(result.errorMessage()).contains("Search failed");
    }

    @Test
    void shouldFindReportById() {
        UUID reportId = UUID.randomUUID();
        Report report = ReportingTestFixtures.createCompletedReport();
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        Report result = service.getStatus(reportId);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldThrowWhenReportNotFound() {
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatus(reportId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Report not found");
    }

    @Test
    void shouldDownloadReport() {
        UUID reportId = UUID.randomUUID();
        Report report = Report.builder()
                .id(reportId)
                .name("Test")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .filePath("/reports/test.pdf")
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(storageService.retrieve("/reports/test.pdf")).thenReturn("PDF content".getBytes());

        byte[] result = service.download(reportId);

        assertThat(result).isNotNull();
        verify(storageService).retrieve("/reports/test.pdf");
    }

    @Test
    void shouldThrowWhenDownloadingIncompleteReport() {
        UUID reportId = UUID.randomUUID();
        Report report = Report.builder()
                .id(reportId)
                .name("Test")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.GENERATING)
                .tenantId("tenant-001")
                .build();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> service.download(reportId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not ready");
    }

    @Test
    void shouldDeleteReport() {
        UUID reportId = UUID.randomUUID();
        Report report = Report.builder()
                .id(reportId)
                .name("Test")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .filePath("/reports/test.pdf")
                .build();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        service.delete(reportId);

        verify(storageService).delete("/reports/test.pdf");
        verify(reportRepository).delete(report);
    }

    @Test
    void shouldThrowForMissingTenantId() {
        ReportRequest request = ReportRequest.builder()
                .name("Test")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .tenantId(null)
                .build();

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID");
    }

    @Test
    void shouldThrowForMissingReportType() {
        ReportRequest request = ReportRequest.builder()
                .name("Test")
                .type(null)
                .format(ReportFormat.PDF)
                .tenantId("tenant-001")
                .build();

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type");
    }

    @Test
    void shouldThrowForMissingFormat() {
        ReportRequest request = ReportRequest.builder()
                .name("Test")
                .type(ReportType.AUDIT)
                .format(null)
                .tenantId("tenant-001")
                .build();

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format");
    }
}
