package com.mohmk10.audittrail.reporting.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ScheduledReport;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    private final ObjectMapper objectMapper;

    public ReportMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public Report toDomain(ReportEntity entity) {
        return Report.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .format(entity.getFormat())
                .status(entity.getStatus())
                .tenantId(entity.getTenantId())
                .criteria(deserializeCriteria(entity.getCriteria()))
                .parameters(entity.getParameters())
                .generatedAt(entity.getGeneratedAt())
                .expiresAt(entity.getExpiresAt())
                .filePath(entity.getFilePath())
                .fileSize(entity.getFileSize() != null ? entity.getFileSize() : 0)
                .checksum(entity.getChecksum())
                .signature(entity.getSignature())
                .createdAt(entity.getCreatedAt())
                .errorMessage(entity.getErrorMessage())
                .build();
    }

    public ReportEntity toEntity(Report report) {
        ReportEntity entity = new ReportEntity();
        entity.setId(report.id());
        entity.setName(report.name());
        entity.setType(report.type());
        entity.setFormat(report.format());
        entity.setStatus(report.status());
        entity.setTenantId(report.tenantId());
        entity.setCriteria(serializeCriteria(report.criteria()));
        entity.setParameters(report.parameters());
        entity.setGeneratedAt(report.generatedAt());
        entity.setExpiresAt(report.expiresAt());
        entity.setFilePath(report.filePath());
        entity.setFileSize(report.fileSize());
        entity.setChecksum(report.checksum());
        entity.setSignature(report.signature());
        entity.setCreatedAt(report.createdAt());
        entity.setErrorMessage(report.errorMessage());
        return entity;
    }

    public ScheduledReport toDomain(ScheduledReportEntity entity) {
        return ScheduledReport.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .format(entity.getFormat())
                .tenantId(entity.getTenantId())
                .criteria(deserializeCriteria(entity.getCriteria()))
                .cronExpression(entity.getCronExpression())
                .enabled(entity.isEnabled())
                .lastRunAt(entity.getLastRunAt())
                .nextRunAt(entity.getNextRunAt())
                .recipients(entity.getRecipients())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ScheduledReportEntity toEntity(ScheduledReport report) {
        ScheduledReportEntity entity = new ScheduledReportEntity();
        entity.setId(report.id());
        entity.setName(report.name());
        entity.setType(report.type());
        entity.setFormat(report.format());
        entity.setTenantId(report.tenantId());
        entity.setCriteria(serializeCriteria(report.criteria()));
        entity.setCronExpression(report.cronExpression());
        entity.setEnabled(report.enabled());
        entity.setLastRunAt(report.lastRunAt());
        entity.setNextRunAt(report.nextRunAt());
        entity.setRecipients(report.recipients());
        entity.setCreatedAt(report.createdAt());
        return entity;
    }

    private String serializeCriteria(SearchCriteria criteria) {
        if (criteria == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(criteria);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private SearchCriteria deserializeCriteria(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, SearchCriteria.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
