package com.mohmk10.audittrail.reporting.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

class JsonReportGeneratorTest {

    private JsonReportGenerator generator;
    private ReportTemplate template;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        generator = new JsonReportGenerator();
        template = new AuditReportTemplate();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldGenerateJsonBytes() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        byte[] result = generator.generate(report, events, template);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void shouldIncludeMetadata() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        byte[] result = generator.generate(report, events, template);
        JsonNode json = objectMapper.readTree(result);

        assertThat(json.has("metadata")).isTrue();
        JsonNode metadata = json.get("metadata");
        assertThat(metadata.get("name").asText()).isEqualTo("Test Report");
        assertThat(metadata.get("type").asText()).isEqualTo("AUDIT");
        assertThat(metadata.get("format").asText()).isEqualTo("JSON");
        assertThat(metadata.get("tenantId").asText()).isEqualTo("tenant-001");
    }

    @Test
    void shouldIncludeEvents() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        byte[] result = generator.generate(report, events, template);
        JsonNode json = objectMapper.readTree(result);

        assertThat(json.has("events")).isTrue();
        assertThat(json.get("events").isArray()).isTrue();
        assertThat(json.get("events").size()).isEqualTo(5);
    }

    @Test
    void shouldIncludeSummary() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(10);

        byte[] result = generator.generate(report, events, template);
        JsonNode json = objectMapper.readTree(result);

        assertThat(json.has("summary")).isTrue();
        JsonNode summary = json.get("summary");
        assertThat(summary.get("totalEvents").asInt()).isEqualTo(10);
        assertThat(summary.has("byActionType")).isTrue();
        assertThat(summary.has("byResourceType")).isTrue();
        assertThat(summary.has("byActor")).isTrue();
    }

    @Test
    void shouldFormatDatesCorrectly() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        byte[] result = generator.generate(report, events, template);
        JsonNode json = objectMapper.readTree(result);

        JsonNode metadata = json.get("metadata");
        String generatedAt = metadata.get("generatedAt").asText();
        // ISO-8601 format check
        assertThat(generatedAt).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
    }

    @Test
    void shouldHandleEmptyEvents() throws Exception {
        Report report = createTestReport();
        List<Event> events = List.of();

        byte[] result = generator.generate(report, events, template);
        JsonNode json = objectMapper.readTree(result);

        assertThat(json.get("events").size()).isEqualTo(0);
        assertThat(json.get("summary").get("totalEvents").asInt()).isEqualTo(0);
    }

    @Test
    void shouldReturnJsonFormat() {
        assertThat(generator.getFormat()).isEqualTo(ReportFormat.JSON);
    }

    @Test
    void shouldIncludeChecksumWhenPresent() throws Exception {
        Report report = Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.JSON)
                .status(ReportStatus.GENERATING)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .checksum("abc123checksum")
                .signature("xyz789signature")
                .build();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        byte[] result = generator.generate(report, events, template);
        JsonNode json = objectMapper.readTree(result);

        JsonNode metadata = json.get("metadata");
        assertThat(metadata.get("checksum").asText()).isEqualTo("abc123checksum");
        assertThat(metadata.get("signature").asText()).isEqualTo("xyz789signature");
    }

    @Test
    void shouldBeValidJson() {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(5);

        byte[] result = generator.generate(report, events, template);
        String jsonString = new String(result, StandardCharsets.UTF_8);

        // Should parse without exception
        assertThat(jsonString).startsWith("{");
        assertThat(jsonString).endsWith("}");
    }

    @Test
    void shouldIncludeTemplateName() throws Exception {
        Report report = createTestReport();
        List<Event> events = ReportingTestFixtures.createTestEvents(2);

        byte[] result = generator.generate(report, events, template);
        JsonNode json = objectMapper.readTree(result);

        JsonNode metadata = json.get("metadata");
        assertThat(metadata.get("templateName").asText()).isEqualTo("Audit Report");
    }

    private Report createTestReport() {
        return Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.JSON)
                .status(ReportStatus.GENERATING)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .build();
    }
}
