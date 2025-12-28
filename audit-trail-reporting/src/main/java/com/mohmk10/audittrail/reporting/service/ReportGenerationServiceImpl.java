package com.mohmk10.audittrail.reporting.service;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.reporting.adapter.out.persistence.ReportRepository;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.domain.ReportStatus;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import com.mohmk10.audittrail.reporting.generator.ReportGenerator;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import com.mohmk10.audittrail.search.service.EventSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationServiceImpl.class);

    private final EventSearchService eventSearchService;
    private final ReportRepository reportRepository;
    private final ReportStorageService storageService;
    private final ReportCertificationService certificationService;
    private final Map<ReportFormat, ReportGenerator> generators;
    private final Map<ReportType, ReportTemplate> templates;
    private final int expirationDays;

    public ReportGenerationServiceImpl(
            EventSearchService eventSearchService,
            ReportRepository reportRepository,
            ReportStorageService storageService,
            ReportCertificationService certificationService,
            List<ReportGenerator> generatorList,
            List<ReportTemplate> templateList,
            @Value("${reporting.expiration.days:30}") int expirationDays) {
        this.eventSearchService = eventSearchService;
        this.reportRepository = reportRepository;
        this.storageService = storageService;
        this.certificationService = certificationService;
        this.generators = generatorList.stream()
                .collect(Collectors.toMap(ReportGenerator::getFormat, Function.identity()));
        this.templates = templateList.stream()
                .collect(Collectors.toMap(ReportTemplate::getType, Function.identity()));
        this.expirationDays = expirationDays;
    }

    @Override
    public Report generate(ReportRequest request) {
        validateRequest(request);

        Report report = createPendingReport(request);
        report = reportRepository.save(report);

        try {
            report = generateReportContent(report, request);
            return reportRepository.save(report);
        } catch (Exception e) {
            log.error("Failed to generate report: {}", report.id(), e);
            Report failedReport = report.toBuilder()
                    .status(ReportStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .build();
            return reportRepository.save(failedReport);
        }
    }

    @Override
    @Async
    public CompletableFuture<Report> generateAsync(ReportRequest request) {
        return CompletableFuture.supplyAsync(() -> generate(request));
    }

    @Override
    public byte[] download(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        if (report.status() != ReportStatus.COMPLETED) {
            throw new RuntimeException("Report is not ready for download. Status: " + report.status());
        }

        if (report.expiresAt() != null && report.expiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Report has expired");
        }

        return storageService.retrieve(report.filePath());
    }

    @Override
    public void delete(UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));

        if (report.filePath() != null) {
            storageService.delete(report.filePath());
        }

        reportRepository.delete(report);
        log.info("Deleted report: {}", reportId);
    }

    @Override
    public Report getStatus(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found: " + reportId));
    }

    private void validateRequest(ReportRequest request) {
        if (request.tenantId() == null || request.tenantId().isBlank()) {
            throw new IllegalArgumentException("Tenant ID is required");
        }
        if (request.type() == null) {
            throw new IllegalArgumentException("Report type is required");
        }
        if (request.format() == null) {
            throw new IllegalArgumentException("Report format is required");
        }
        if (!generators.containsKey(request.format())) {
            throw new IllegalArgumentException("Unsupported report format: " + request.format());
        }
    }

    private Report createPendingReport(ReportRequest request) {
        return Report.builder()
                .id(UUID.randomUUID())
                .name(request.name() != null ? request.name() : generateDefaultName(request))
                .type(request.type())
                .format(request.format())
                .status(ReportStatus.PENDING)
                .tenantId(request.tenantId())
                .parameters(request.parameters())
                .createdAt(Instant.now())
                .build();
    }

    private String generateDefaultName(ReportRequest request) {
        return request.type().name() + " Report - " + Instant.now().toString().substring(0, 10);
    }

    private Report generateReportContent(Report report, ReportRequest request) {
        Report updatedReport = report.toBuilder()
                .status(ReportStatus.GENERATING)
                .build();
        reportRepository.save(updatedReport);

        ReportTemplate template = templates.getOrDefault(request.type(), templates.get(ReportType.AUDIT));
        SearchCriteria criteria = buildSearchCriteria(request, template);

        List<Event> events = fetchEvents(criteria);
        log.info("Fetched {} events for report {}", events.size(), report.id());

        updatedReport = updatedReport.toBuilder()
                .criteria(criteria)
                .generatedAt(Instant.now())
                .build();

        ReportGenerator generator = generators.get(request.format());
        byte[] content = generator.generate(updatedReport, events, template);

        String checksum = certificationService.calculateChecksum(content);
        String signature = certificationService.sign(content, report.id().toString());

        String extension = request.format().name().toLowerCase();
        String filePath = storageService.store(report.id(), extension, content);
        long fileSize = storageService.getFileSize(filePath);

        return updatedReport.toBuilder()
                .status(ReportStatus.COMPLETED)
                .filePath(filePath)
                .fileSize(fileSize)
                .checksum(checksum)
                .signature(signature)
                .expiresAt(Instant.now().plus(expirationDays, ChronoUnit.DAYS))
                .build();
    }

    private SearchCriteria buildSearchCriteria(ReportRequest request, ReportTemplate template) {
        SearchCriteria.Builder builder = SearchCriteria.builder()
                .tenantId(request.tenantId())
                .page(0)
                .size(10000)
                .sortBy("timestamp")
                .sortDirection(SearchCriteria.SortDirection.DESC);

        if (request.dateRange() != null) {
            builder.dateRange(request.dateRange());
        }

        if (request.actorId() != null && !request.actorId().isBlank()) {
            builder.actorId(request.actorId());
        }

        if (request.actionType() != null && !request.actionType().isBlank()) {
            try {
                builder.actionTypes(List.of(Action.ActionType.valueOf(request.actionType())));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (request.resourceType() != null && !request.resourceType().isBlank()) {
            try {
                builder.resourceTypes(List.of(Resource.ResourceType.valueOf(request.resourceType())));
            } catch (IllegalArgumentException ignored) {
            }
        }

        return builder.build();
    }

    private List<Event> fetchEvents(SearchCriteria criteria) {
        List<Event> allEvents = new ArrayList<>();
        int page = 0;
        int pageSize = 1000;

        while (true) {
            SearchCriteria pagedCriteria = SearchCriteria.builder()
                    .tenantId(criteria.tenantId())
                    .actorId(criteria.actorId())
                    .actionTypes(criteria.actionTypes())
                    .resourceTypes(criteria.resourceTypes())
                    .dateRange(criteria.dateRange())
                    .correlationId(criteria.correlationId())
                    .sessionId(criteria.sessionId())
                    .query(criteria.query())
                    .tags(criteria.tags())
                    .page(page)
                    .size(pageSize)
                    .sortBy(criteria.sortBy())
                    .sortDirection(criteria.sortDirection())
                    .build();

            SearchResult<Event> result = eventSearchService.search(pagedCriteria);
            allEvents.addAll(result.items());

            if (result.items().size() < pageSize || allEvents.size() >= 10000) {
                break;
            }
            page++;
        }

        return allEvents;
    }
}
