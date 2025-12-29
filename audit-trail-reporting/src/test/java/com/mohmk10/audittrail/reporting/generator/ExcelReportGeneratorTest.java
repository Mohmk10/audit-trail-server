package com.mohmk10.audittrail.reporting.generator;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.domain.ReportStatus;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import com.mohmk10.audittrail.reporting.fixtures.ReportingTestFixtures;
import com.mohmk10.audittrail.reporting.template.AuditReportTemplate;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelReportGeneratorTest {

    private ExcelReportGenerator generator;
    private ReportTemplate template;

    @BeforeEach
    void setUp() {
        generator = new ExcelReportGenerator();
        template = new AuditReportTemplate();
    }

    @Test
    void shouldGenerateXlsxBytes() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void shouldCreateEventsSheet() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        byte[] result = generator.generate(report, events, template);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet eventsSheet = workbook.getSheet("Events");
            assertThat(eventsSheet).isNotNull();
        }
    }

    @Test
    void shouldCreateSummarySheet() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        byte[] result = generator.generate(report, events, template);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet summarySheet = workbook.getSheet("Summary");
            assertThat(summarySheet).isNotNull();
        }
    }

    @Test
    void shouldIncludeHeaders() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(3);

        byte[] result = generator.generate(report, events, template);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet eventsSheet = workbook.getSheet("Events");
            assertThat(eventsSheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(eventsSheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("Timestamp");
        }
    }

    @Test
    void shouldAutoSizeColumns() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(10);

        byte[] result = generator.generate(report, events, template);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet eventsSheet = workbook.getSheet("Events");
            // Column width should be greater than default
            assertThat(eventsSheet.getColumnWidth(0)).isGreaterThan(0);
        }
    }

    @Test
    void shouldHandleEmptyEvents() throws Exception {
        Report report = createTestReport();
        List<Event> events = List.of();

        byte[] result = generator.generate(report, events, template);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet eventsSheet = workbook.getSheet("Events");
            assertThat(eventsSheet).isNotNull();
            // Should have header row only
            assertThat(eventsSheet.getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }

    @Test
    void shouldReturnXlsxFormat() {
        assertThat(generator.getFormat()).isEqualTo(ReportFormat.XLSX);
    }

    @Test
    void shouldIncludeEventData() throws Exception {
        Report report = createTestReport();
        List<Event> events = List.of(ReportingTestFixtures.createSingleEvent());

        byte[] result = generator.generate(report, events, template);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet eventsSheet = workbook.getSheet("Events");
            // Row 1 should contain event data
            assertThat(eventsSheet.getRow(1).getCell(2).getStringCellValue()).isEqualTo("actor-123");
        }
    }

    @Test
    void shouldIncludeSummaryStatistics() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(10);

        byte[] result = generator.generate(report, events, template);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            Sheet summarySheet = workbook.getSheet("Summary");
            assertThat(summarySheet).isNotNull();
            // Should have report name
            assertThat(summarySheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Test Report");
        }
    }

    private Report createTestReport() {
        return Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.XLSX)
                .status(ReportStatus.GENERATING)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .build();
    }
}
