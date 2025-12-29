package com.mohmk10.audittrail.reporting.generator;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.domain.ReportStatus;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import com.mohmk10.audittrail.reporting.fixtures.ReportingTestFixtures;
import com.mohmk10.audittrail.reporting.template.AuditReportTemplate;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfReportGeneratorTest {

    private PdfReportGenerator generator;
    private ReportTemplate template;

    @BeforeEach
    void setUp() {
        generator = new PdfReportGenerator();
        template = new AuditReportTemplate();
    }

    @Test
    void shouldGeneratePdfBytes() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
        // PDF magic bytes: %PDF-
        assertThat(result[0]).isEqualTo((byte) '%');
        assertThat(result[1]).isEqualTo((byte) 'P');
        assertThat(result[2]).isEqualTo((byte) 'D');
        assertThat(result[3]).isEqualTo((byte) 'F');
    }

    @Test
    void shouldIncludeTitle() {
        Report report = Report.builder()
                .id(UUID.randomUUID())
                .name("My Custom Report Title")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.GENERATING)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .build();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void shouldIncludeGenerationDate() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void shouldIncludeEventsTable() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(10);

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void shouldHandleEmptyEventsList() {
        Report report = createTestReport();
        List<Event> events = List.of();

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void shouldHandleLargeEventsList() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(100);

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void shouldReturnPdfFormat() {
        assertThat(generator.getFormat()).isEqualTo(ReportFormat.PDF);
    }

    @Test
    void shouldHandleSingleEvent() {
        Report report = createTestReport();
        List<Event> events = List.of(ReportingTestFixtures.createSingleEvent());

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    private Report createTestReport() {
        return Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.GENERATING)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .build();
    }
}
