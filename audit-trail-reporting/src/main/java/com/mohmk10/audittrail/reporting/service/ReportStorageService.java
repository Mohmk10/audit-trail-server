package com.mohmk10.audittrail.reporting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ReportStorageService {

    private static final Logger log = LoggerFactory.getLogger(ReportStorageService.class);

    private final Path storagePath;

    public ReportStorageService(@Value("${reporting.storage.path:./reports}") String storagePath) {
        this.storagePath = Paths.get(storagePath);
        initStorage();
    }

    private void initStorage() {
        try {
            Files.createDirectories(storagePath);
            log.info("Report storage initialized at: {}", storagePath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create report storage directory", e);
        }
    }

    public String store(UUID reportId, String extension, byte[] content) {
        String filename = reportId.toString() + "." + extension.toLowerCase();
        Path filePath = storagePath.resolve(filename);

        try {
            Files.write(filePath, content);
            log.debug("Stored report: {}", filePath);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store report file", e);
        }
    }

    public byte[] retrieve(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("Report file not found: " + filePath);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve report file", e);
        }
    }

    public void delete(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.debug("Deleted report: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete report file: {}", filePath, e);
        }
    }

    public long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return 0;
        }
    }
}
