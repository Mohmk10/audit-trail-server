package com.mohmk10.audittrail.reporting.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JsonReportGenerator implements ReportGenerator {

    private final ObjectMapper objectMapper;

    public JsonReportGenerator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.JSON;
    }

    @Override
    public byte[] generate(Report report, List<Event> events, ReportTemplate template) {
        try {
            Map<String, Object> reportData = new HashMap<>();

            reportData.put("metadata", createMetadata(report, template));
            reportData.put("events", events);
            reportData.put("summary", createSummary(events));

            return objectMapper.writeValueAsBytes(reportData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JSON report", e);
        }
    }

    private Map<String, Object> createMetadata(Report report, ReportTemplate template) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reportId", report.id().toString());
        metadata.put("name", report.name());
        metadata.put("type", report.type().name());
        metadata.put("format", report.format().name());
        metadata.put("tenantId", report.tenantId());
        metadata.put("generatedAt", report.generatedAt().toString());
        metadata.put("templateName", template.getName());
        metadata.put("templateDescription", template.getDescription());
        if (report.checksum() != null) {
            metadata.put("checksum", report.checksum());
        }
        if (report.signature() != null) {
            metadata.put("signature", report.signature());
        }
        return metadata;
    }

    private Map<String, Object> createSummary(List<Event> events) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalEvents", events.size());

        Map<String, Long> actionCounts = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.action().type().name(),
                        Collectors.counting()
                ));
        summary.put("byActionType", actionCounts);

        Map<String, Long> resourceCounts = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.resource().type().name(),
                        Collectors.counting()
                ));
        summary.put("byResourceType", resourceCounts);

        Map<String, Long> actorCounts = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.actor().id(),
                        Collectors.counting()
                ));
        summary.put("byActor", actorCounts);

        if (!events.isEmpty()) {
            summary.put("firstEventAt", events.get(events.size() - 1).timestamp().toString());
            summary.put("lastEventAt", events.get(0).timestamp().toString());
        }

        return summary;
    }
}
