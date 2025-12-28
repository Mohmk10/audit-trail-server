package com.mohmk10.audittrail.reporting.domain;

import com.mohmk10.audittrail.core.dto.SearchCriteria;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Report(
        UUID id,
        String name,
        ReportType type,
        ReportFormat format,
        ReportStatus status,
        String tenantId,
        SearchCriteria criteria,
        Instant generatedAt,
        Instant expiresAt,
        String filePath,
        long fileSize,
        String checksum,
        String signature,
        Map<String, Object> parameters,
        Instant createdAt,
        String errorMessage
) {
    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .name(name)
                .type(type)
                .format(format)
                .status(status)
                .tenantId(tenantId)
                .criteria(criteria)
                .generatedAt(generatedAt)
                .expiresAt(expiresAt)
                .filePath(filePath)
                .fileSize(fileSize)
                .checksum(checksum)
                .signature(signature)
                .parameters(parameters)
                .createdAt(createdAt)
                .errorMessage(errorMessage);
    }

    public static class Builder {
        private UUID id;
        private String name;
        private ReportType type;
        private ReportFormat format;
        private ReportStatus status;
        private String tenantId;
        private SearchCriteria criteria;
        private Instant generatedAt;
        private Instant expiresAt;
        private String filePath;
        private long fileSize;
        private String checksum;
        private String signature;
        private Map<String, Object> parameters;
        private Instant createdAt;
        private String errorMessage;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(ReportType type) {
            this.type = type;
            return this;
        }

        public Builder format(ReportFormat format) {
            this.format = format;
            return this;
        }

        public Builder status(ReportStatus status) {
            this.status = status;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder criteria(SearchCriteria criteria) {
            this.criteria = criteria;
            return this;
        }

        public Builder generatedAt(Instant generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder fileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder signature(String signature) {
            this.signature = signature;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Report build() {
            return new Report(id, name, type, format, status, tenantId, criteria,
                    generatedAt, expiresAt, filePath, fileSize, checksum, signature,
                    parameters, createdAt, errorMessage);
        }
    }
}
