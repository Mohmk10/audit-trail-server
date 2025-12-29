package com.mohmk10.audittrail.reporting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReportCertificationServiceTest {

    private ReportCertificationService certificationService;

    @BeforeEach
    void setUp() {
        certificationService = new ReportCertificationService();
    }

    @Test
    void shouldCalculateSha256Checksum() {
        byte[] content = "Test report content".getBytes(StandardCharsets.UTF_8);

        String checksum = certificationService.calculateChecksum(content);

        assertThat(checksum).isNotNull();
        assertThat(checksum).hasSize(64); // SHA-256 produces 64 hex characters
    }

    @Test
    void shouldProduceSameChecksumForSameContent() {
        byte[] content = "Identical content".getBytes(StandardCharsets.UTF_8);

        String checksum1 = certificationService.calculateChecksum(content);
        String checksum2 = certificationService.calculateChecksum(content);

        assertThat(checksum1).isEqualTo(checksum2);
    }

    @Test
    void shouldProduceDifferentChecksumForDifferentContent() {
        byte[] content1 = "First content".getBytes(StandardCharsets.UTF_8);
        byte[] content2 = "Second content".getBytes(StandardCharsets.UTF_8);

        String checksum1 = certificationService.calculateChecksum(content1);
        String checksum2 = certificationService.calculateChecksum(content2);

        assertThat(checksum1).isNotEqualTo(checksum2);
    }

    @Test
    void shouldSignChecksum() {
        byte[] content = "Report content to sign".getBytes(StandardCharsets.UTF_8);
        String reportId = UUID.randomUUID().toString();

        String signature = certificationService.sign(content, reportId);

        assertThat(signature).isNotNull();
        assertThat(signature).isNotEmpty();
    }

    @Test
    void shouldVerifyValidSignature() {
        byte[] content = "Report content to verify".getBytes(StandardCharsets.UTF_8);
        String reportId = UUID.randomUUID().toString();

        String signature = certificationService.sign(content, reportId);
        boolean isValid = certificationService.verify(content, reportId, signature);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectInvalidSignature() {
        byte[] content = "Original content".getBytes(StandardCharsets.UTF_8);
        byte[] modifiedContent = "Modified content".getBytes(StandardCharsets.UTF_8);
        String reportId = UUID.randomUUID().toString();

        String signature = certificationService.sign(content, reportId);
        boolean isValid = certificationService.verify(modifiedContent, reportId, signature);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectSignatureWithWrongReportId() {
        byte[] content = "Report content".getBytes(StandardCharsets.UTF_8);
        String reportId1 = UUID.randomUUID().toString();
        String reportId2 = UUID.randomUUID().toString();

        String signature = certificationService.sign(content, reportId1);
        boolean isValid = certificationService.verify(content, reportId2, signature);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldProduceBase64EncodedSignature() {
        byte[] content = "Test content".getBytes(StandardCharsets.UTF_8);
        String reportId = UUID.randomUUID().toString();

        String signature = certificationService.sign(content, reportId);

        // Base64 characters only
        assertThat(signature).matches("[A-Za-z0-9+/=]+");
    }

    @Test
    void shouldHandleEmptyContent() {
        byte[] content = new byte[0];

        String checksum = certificationService.calculateChecksum(content);

        assertThat(checksum).isNotNull();
        assertThat(checksum).hasSize(64);
    }

    @Test
    void shouldHandleLargeContent() {
        byte[] content = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        String checksum = certificationService.calculateChecksum(content);

        assertThat(checksum).isNotNull();
        assertThat(checksum).hasSize(64);
    }

    @Test
    void shouldProduceDifferentSignaturesForDifferentReports() {
        byte[] content = "Same content".getBytes(StandardCharsets.UTF_8);
        String reportId1 = UUID.randomUUID().toString();
        String reportId2 = UUID.randomUUID().toString();

        String signature1 = certificationService.sign(content, reportId1);
        String signature2 = certificationService.sign(content, reportId2);

        assertThat(signature1).isNotEqualTo(signature2);
    }
}
