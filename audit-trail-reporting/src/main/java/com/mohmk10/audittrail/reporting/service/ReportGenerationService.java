package com.mohmk10.audittrail.reporting.service;

import com.mohmk10.audittrail.reporting.domain.Report;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ReportGenerationService {
    Report generate(ReportRequest request);
    CompletableFuture<Report> generateAsync(ReportRequest request);
    byte[] download(UUID reportId);
    void delete(UUID reportId);
    Report getStatus(UUID reportId);
}
