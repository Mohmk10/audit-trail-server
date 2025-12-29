package com.mohmk10.audittrail.reporting.fixtures;

import com.mohmk10.audittrail.core.domain.*;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.domain.ReportStatus;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import com.mohmk10.audittrail.reporting.service.ReportRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ReportingTestFixtures {

    private ReportingTestFixtures() {}

    public static Report createTestReport() {
        return Report.builder()
                .id(UUID.randomUUID())
                .name("Test Audit Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.PENDING)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .createdAt(Instant.now())
                .build();
    }

    public static Report createCompletedReport() {
        return Report.builder()
                .id(UUID.randomUUID())
                .name("Completed Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .status(ReportStatus.COMPLETED)
                .tenantId("tenant-001")
                .generatedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(86400 * 30))
                .filePath("/reports/test.pdf")
                .fileSize(1024L)
                .checksum("abc123")
                .signature("sig456")
                .createdAt(Instant.now())
                .build();
    }

    public static ReportRequest createGenerateRequest() {
        return ReportRequest.builder()
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(ReportFormat.PDF)
                .tenantId("tenant-001")
                .parameters(Map.of("includeDetails", true))
                .build();
    }

    public static ReportRequest createGenerateRequest(ReportFormat format) {
        return ReportRequest.builder()
                .name("Test Report")
                .type(ReportType.AUDIT)
                .format(format)
                .tenantId("tenant-001")
                .build();
    }

    public static List<Event> createTestEvents(int count) {
        List<Event> events = new ArrayList<>();
        Instant baseTime = Instant.now();

        for (int i = 0; i < count; i++) {
            Action.ActionType actionType = switch (i % 4) {
                case 0 -> Action.ActionType.CREATE;
                case 1 -> Action.ActionType.READ;
                case 2 -> Action.ActionType.UPDATE;
                default -> Action.ActionType.DELETE;
            };

            Event event = new Event(
                    UUID.randomUUID(),
                    baseTime.minusSeconds(i * 60L),
                    new Actor(
                            "actor-" + (i % 3),
                            Actor.ActorType.USER,
                            "User " + (i % 3),
                            "192.168.1." + (i % 256),
                            null,
                            null
                    ),
                    new Action(
                            actionType,
                            "Action description " + i,
                            "CATEGORY_" + (i % 2)
                    ),
                    new Resource(
                            "resource-" + i,
                            Resource.ResourceType.DOCUMENT,
                            "Document " + i,
                            null,
                            null
                    ),
                    new EventMetadata(
                            "web-app",
                            "tenant-001",
                            "corr-" + (i / 5),
                            "session-" + (i % 10),
                            null,
                            null
                    ),
                    i > 0 ? "prevHash" + (i - 1) : null,
                    "hash" + i,
                    null
            );
            events.add(event);
        }
        return events;
    }

    public static Event createSingleEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor(
                        "actor-123",
                        Actor.ActorType.USER,
                        "John Doe",
                        "192.168.1.100",
                        "Mozilla/5.0",
                        null
                ),
                new Action(
                        Action.ActionType.CREATE,
                        "Created new document",
                        "DOCUMENTS"
                ),
                new Resource(
                        "doc-456",
                        Resource.ResourceType.DOCUMENT,
                        "Annual Report 2024",
                        null,
                        null
                ),
                new EventMetadata(
                        "web-app",
                        "tenant-001",
                        "corr-789",
                        "session-abc",
                        null,
                        null
                ),
                null,
                "hash123",
                null
        );
    }
}
