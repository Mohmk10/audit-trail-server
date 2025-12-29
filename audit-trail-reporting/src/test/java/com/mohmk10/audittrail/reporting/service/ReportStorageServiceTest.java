package com.mohmk10.audittrail.reporting.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportStorageServiceTest {

    @TempDir
    Path tempDir;

    private ReportStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new ReportStorageService(tempDir.toString());
    }

    @Test
    void shouldSaveReportToFile() {
        UUID reportId = UUID.randomUUID();
        byte[] content = "Report content".getBytes(StandardCharsets.UTF_8);

        String filePath = storageService.store(reportId, "pdf", content);

        assertThat(filePath).isNotNull();
        assertThat(Files.exists(Path.of(filePath))).isTrue();
    }

    @Test
    void shouldCreateDirectoryIfNotExists() {
        Path subDir = tempDir.resolve("reports/subdir");
        ReportStorageService service = new ReportStorageService(subDir.toString());

        assertThat(Files.exists(subDir)).isTrue();
    }

    @Test
    void shouldLoadReportFromFile() {
        UUID reportId = UUID.randomUUID();
        byte[] originalContent = "Test report data".getBytes(StandardCharsets.UTF_8);

        String filePath = storageService.store(reportId, "pdf", originalContent);
        byte[] loadedContent = storageService.retrieve(filePath);

        assertThat(loadedContent).isEqualTo(originalContent);
    }

    @Test
    void shouldDeleteReport() {
        UUID reportId = UUID.randomUUID();
        byte[] content = "Content to delete".getBytes(StandardCharsets.UTF_8);

        String filePath = storageService.store(reportId, "pdf", content);
        assertThat(Files.exists(Path.of(filePath))).isTrue();

        storageService.delete(filePath);

        assertThat(Files.exists(Path.of(filePath))).isFalse();
    }

    @Test
    void shouldReturnCorrectFilePath() {
        UUID reportId = UUID.randomUUID();
        byte[] content = "Test content".getBytes(StandardCharsets.UTF_8);

        String filePath = storageService.store(reportId, "csv", content);

        assertThat(filePath).contains(reportId.toString());
        assertThat(filePath).endsWith(".csv");
    }

    @Test
    void shouldHandleMissingFile() {
        String nonExistentPath = tempDir.resolve("non-existent-file.pdf").toString();

        assertThatThrownBy(() -> storageService.retrieve(nonExistentPath))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldReturnCorrectFileSize() {
        UUID reportId = UUID.randomUUID();
        byte[] content = "This is test content with specific size".getBytes(StandardCharsets.UTF_8);

        String filePath = storageService.store(reportId, "txt", content);
        long fileSize = storageService.getFileSize(filePath);

        assertThat(fileSize).isEqualTo(content.length);
    }

    @Test
    void shouldHandleDeleteNonExistentFile() {
        // Should not throw exception
        storageService.delete(tempDir.resolve("non-existent.pdf").toString());
    }

    @Test
    void shouldStoreWithDifferentExtensions() {
        UUID reportId = UUID.randomUUID();
        byte[] content = "Test content".getBytes(StandardCharsets.UTF_8);

        String pdfPath = storageService.store(reportId, "PDF", content);
        assertThat(pdfPath).endsWith(".pdf");
    }

    @Test
    void shouldHandleLargeFile() {
        UUID reportId = UUID.randomUUID();
        byte[] content = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 256);
        }

        String filePath = storageService.store(reportId, "bin", content);
        byte[] retrieved = storageService.retrieve(filePath);

        assertThat(retrieved).isEqualTo(content);
    }

    @Test
    void shouldReturnZeroForNonExistentFileSize() {
        long size = storageService.getFileSize(tempDir.resolve("non-existent.pdf").toString());

        assertThat(size).isEqualTo(0);
    }
}
