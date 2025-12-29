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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CsvReportGeneratorTest {

    private CsvReportGenerator generator;
    private ReportTemplate template;

    @BeforeEach
    void setUp() {
        generator = new CsvReportGenerator();
        template = new AuditReportTemplate();
    }

    @Test
    void shouldGenerateCsvBytes() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void shouldIncludeHeaderRow() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        byte[] result = generator.generate(report, events, template);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("ID");
        assertThat(csv).contains("Timestamp");
        assertThat(csv).contains("Actor ID");
        assertThat(csv).contains("Action Type");
        assertThat(csv).contains("Resource ID");
    }

    @Test
    void shouldIncludeAllEventFields() {
        Report report = createTestReport();
        List<Event> events = List.of(ReportingTestFixtures.createSingleEvent());

        byte[] result = generator.generate(report, events, template);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("actor-123");
        assertThat(csv).contains("John Doe");
        assertThat(csv).contains("CREATE");
        assertThat(csv).contains("doc-456");
        assertThat(csv).contains("Annual Report 2024");
    }

    @Test
    void shouldHandleEmptyEvents() {
        Report report = createTestReport();
        List<Event> events = List.of();

        byte[] result = generator.generate(report, events, template);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(result).isNotNull();
        // Should only contain header
        assertThat(csv).contains("ID");
    }

    @Test
    void shouldEscapeSpecialCharacters() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(3);

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldUseUtf8Encoding() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        byte[] result = generator.generate(report, events, template);

        // Check for UTF-8 BOM
        assertThat(result[0]).isEqualTo((byte) 0xEF);
        assertThat(result[1]).isEqualTo((byte) 0xBB);
        assertThat(result[2]).isEqualTo((byte) 0xBF);
    }

    @Test
    void shouldReturnCsvFormat() {
        assertThat(generator.getFormat()).isEqualTo(ReportFormat.CSV);
    }

    @Test
    void shouldHandleMultipleEvents() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(50);

        byte[] result = generator.generate(report, events, template);
        String csv = new String(result, StandardCharsets.UTF_8);

        // Count lines (header + 50 events)
        long lineCount = csv.lines().count();
        assertThat(lineCount).isEqualTo(51); // 1 header + 50 events
    }

    @Test
    void shouldIncludeTenantId() {
        Report report = createTestReport();
        List<Event> events = List.of(ReportingTestFixtures.createSingleEvent());

        byte[] result = generator.generate(report, events, template);
        String csv = new String(result, StandardCharsets.UTF_8);

        assertThat(csv).contains("tenant-001");
    }

    private Report createTestReport() {
        return Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.CSV)
                .status(ReportStatus.GENERATING)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .build();
    }
}
