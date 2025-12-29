package com.mohmk10.audittrail.reporting.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTest {

    @Test
    void shouldCreateReportWithAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(86400);
        Map<String, Object> params = Map.of("key", "value");

        Report report = Report.builder()
                .id(id)
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .generatedAt(now)
                .expiresAt(expiresAt)
                .filePath("/reports/test.pdf")
                .fileSize(1024L)
                .checksum("abc123")
                .signature("sig456")
                .parameters(params)
                .createdAt(now)
                .errorMessage(null)
                .build();

        assertThat(report.id()).isEqualTo(id);
        assertThat(report.name()).isEqualTo("Test Report");
        assertThat(report.type()).isEqualTo(ReportType.AUDIT);
        assertThat(report.format()).isEqualTo(ReportFormat.PDF);
        assertThat(report.status()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(report.tenantId()).isEqualTo("tenant-001");
        assertThat(report.generatedAt()).isEqualTo(now);
        assertThat(report.expiresAt()).isEqualTo(expiresAt);
        assertThat(report.filePath()).isEqualTo("/reports/test.pdf");
        assertThat(report.fileSize()).isEqualTo(1024L);
        assertThat(report.checksum()).isEqualTo("abc123");
        assertThat(report.signature()).isEqualTo("sig456");
        assertThat(report.parameters()).containsEntry("key", "value");
        assertThat(report.createdAt()).isEqualTo(now);
        assertThat(report.errorMessage()).isNull();
    }

    @Test
    void shouldHaveDefaultStatusPending() {
        Report report = Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.PENDING)
                .tenantId("tenant-001")
                .build();

        assertThat(report.status()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void shouldTrackGeneratedAt() {
        Instant generatedAt = Instant.now();

        Report report = Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .generatedAt(generatedAt)
                .build();

        assertThat(report.generatedAt()).isEqualTo(generatedAt);
    }

    @Test
    void shouldStoreChecksum() {
        String checksum = "sha256:abc123def456";

        Report report = Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .checksum(checksum)
                .build();

        assertThat(report.checksum()).isEqualTo(checksum);
    }

    @Test
    void shouldStoreSignature() {
        String signature = "base64signature==";

        Report report = Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .signature(signature)
                .build();

        assertThat(report.signature()).isEqualTo(signature);
    }

    @Test
    void shouldCalculateFileSize() {
        Report report = Report.builder()
                .id(UUID.randomUUID())
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .fileSize(2048L)
                .build();

        assertThat(report.fileSize()).isEqualTo(2048L);
    }

    @Test
    void shouldUseToBuilder() {
        Report original = Report.builder()
                .id(UUID.randomUUID())
                .name("Original Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.PENDING)
                .tenantId("tenant-001")
                .build();

        Report modified = original.toBuilder()
                .status(ReportStatus.COMPLETED)
                .name("Modified Report")
                .build();

        assertThat(modified.id()).isEqualTo(original.id());
        assertThat(modified.status()).isEqualTo(ReportStatus.COMPLETED);
        assertThat(modified.name()).isEqualTo("Modified Report");
        assertThat(modified.tenantId()).isEqualTo(original.tenantId());
    }

    @Test
    void shouldSupportAllFormats() {
        for (ReportFormat format : ReportFormat.values()) {
            Report report = Report.builder()
                    .id(UUID.randomUUID())
                    .name("Test Report")
                    .type(ReportType.AUDIT)
                    .format(format)
                    .status(ReportStatus.PENDING)
                    .tenantId("tenant-001")
                    .build();

            assertThat(report.format()).isEqualTo(format);
        }
    }

    @Test
    void shouldStoreErrorMessage() {
        Report report = Report.builder()
                .id(UUID.randomUUID())
                .name("Failed Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.FAILED)
                .tenantId("tenant-001")
                .errorMessage("Generation failed due to timeout")
                .build();

        assertThat(report.status()).isEqualTo(ReportStatus.FAILED);
        assertThat(report.errorMessage()).isEqualTo("Generation failed due to timeout");
    }
}
