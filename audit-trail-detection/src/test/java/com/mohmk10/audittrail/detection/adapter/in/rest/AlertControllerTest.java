package com.mohmk10.audittrail.detection.adapter.in.rest;

import com.mohmk10.audittrail.detection.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.detection.adapter.out.persistence.entity.AlertEntity;
import com.mohmk10.audittrail.detection.adapter.out.persistence.repository.JpaAlertRepository;
import com.mohmk10.audittrail.detection.domain.*;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import com.mohmk10.audittrail.detection.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

    @Mock
    private AlertService alertService;

    @Mock
    private JpaAlertRepository alertRepository;

    @Mock
    private RuleDtoMapper mapper;

    private AlertController controller;

    @BeforeEach
    void setUp() {
        controller = new AlertController(alertService, alertRepository, mapper);
    }

    @Test
    void shouldListAlerts() {
        Alert alert1 = DetectionTestFixtures.createTestAlert();
        Alert alert2 = DetectionTestFixtures.createTestAlert();
        Page<Alert> alertPage = new PageImpl<>(List.of(alert1, alert2), PageRequest.of(0, 20), 2);
        AlertResponse response1 = createAlertResponse(alert1);
        AlertResponse response2 = createAlertResponse(alert2);

        when(alertService.findByTenantId(eq("tenant-001"), any(PageRequest.class))).thenReturn(alertPage);
        when(mapper.toAlertResponse(alert1)).thenReturn(response1);
        when(mapper.toAlertResponse(alert2)).thenReturn(response2);

        ResponseEntity<List<AlertResponse>> result = controller.list("tenant-001", null, 0, 20);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(2);
    }

    @Test
    void shouldListAlertsWithStatusFilter() {
        Alert alert = DetectionTestFixtures.createTestAlert();
        Page<Alert> alertPage = new PageImpl<>(List.of(alert), PageRequest.of(0, 20), 1);
        AlertResponse response = createAlertResponse(alert);

        when(alertService.findByTenantId(eq("tenant-001"), any(PageRequest.class))).thenReturn(alertPage);
        when(mapper.toAlertResponse(alert)).thenReturn(response);

        ResponseEntity<List<AlertResponse>> result = controller.list("tenant-001", AlertStatus.OPEN, 0, 20);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyListWhenNoAlerts() {
        Page<Alert> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(alertService.findByTenantId(eq("tenant-001"), any(PageRequest.class))).thenReturn(emptyPage);

        ResponseEntity<List<AlertResponse>> result = controller.list("tenant-001", null, 0, 20);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    void shouldGetAlertStats() {
        AlertEntity openAlert1 = createAlertEntity(Severity.CRITICAL, AlertStatus.OPEN);
        AlertEntity openAlert2 = createAlertEntity(Severity.HIGH, AlertStatus.OPEN);

        when(alertRepository.countByTenantIdAndStatus("tenant-001", AlertStatus.OPEN)).thenReturn(5L);
        when(alertRepository.countByTenantIdAndStatus("tenant-001", AlertStatus.ACKNOWLEDGED)).thenReturn(3L);
        when(alertRepository.countByTenantIdAndStatus("tenant-001", AlertStatus.RESOLVED)).thenReturn(10L);
        when(alertRepository.countByTenantIdAndStatus("tenant-001", AlertStatus.DISMISSED)).thenReturn(2L);
        when(alertRepository.findByTenantIdAndStatus("tenant-001", AlertStatus.OPEN))
                .thenReturn(List.of(openAlert1, openAlert2));

        ResponseEntity<AlertStatsResponse> result = controller.stats("tenant-001");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().totalOpen()).isEqualTo(5L);
        assertThat(result.getBody().totalAcknowledged()).isEqualTo(3L);
        assertThat(result.getBody().totalResolved()).isEqualTo(10L);
        assertThat(result.getBody().totalDismissed()).isEqualTo(2L);
    }

    @Test
    void shouldGetAlertById() {
        UUID alertId = UUID.randomUUID();
        Alert alert = DetectionTestFixtures.createTestAlert();
        AlertResponse response = createAlertResponse(alert);

        when(alertService.findById(alertId)).thenReturn(Optional.of(alert));
        when(mapper.toAlertResponse(alert)).thenReturn(response);

        ResponseEntity<AlertResponse> result = controller.getById(alertId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
    }

    @Test
    void shouldReturnNotFoundWhenAlertDoesNotExist() {
        UUID alertId = UUID.randomUUID();

        when(alertService.findById(alertId)).thenReturn(Optional.empty());

        ResponseEntity<AlertResponse> result = controller.getById(alertId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldAcknowledgeAlert() {
        UUID alertId = UUID.randomUUID();
        Alert acknowledgedAlert = DetectionTestFixtures.createAcknowledgedAlert();
        AlertResponse response = createAlertResponse(acknowledgedAlert);
        AcknowledgeAlertRequest request = new AcknowledgeAlertRequest("admin@example.com");

        when(alertService.acknowledge(alertId, "admin@example.com")).thenReturn(acknowledgedAlert);
        when(mapper.toAlertResponse(acknowledgedAlert)).thenReturn(response);

        ResponseEntity<AlertResponse> result = controller.acknowledge(alertId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().status()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        verify(alertService).acknowledge(alertId, "admin@example.com");
    }

    @Test
    void shouldResolveAlert() {
        UUID alertId = UUID.randomUUID();
        Alert resolvedAlert = DetectionTestFixtures.createResolvedAlert();
        AlertResponse response = createAlertResponse(resolvedAlert);
        ResolveAlertRequest request = new ResolveAlertRequest("Issue has been fixed");

        when(alertService.resolve(alertId, "Issue has been fixed")).thenReturn(resolvedAlert);
        when(mapper.toAlertResponse(resolvedAlert)).thenReturn(response);

        ResponseEntity<AlertResponse> result = controller.resolve(alertId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().status()).isEqualTo(AlertStatus.RESOLVED);
        verify(alertService).resolve(alertId, "Issue has been fixed");
    }

    @Test
    void shouldDismissAlert() {
        UUID alertId = UUID.randomUUID();
        Alert dismissedAlert = DetectionTestFixtures.createTestAlert().toBuilder()
                .status(AlertStatus.DISMISSED)
                .build();
        AlertResponse response = createAlertResponse(dismissedAlert);

        when(alertService.dismiss(alertId)).thenReturn(dismissedAlert);
        when(mapper.toAlertResponse(dismissedAlert)).thenReturn(response);

        ResponseEntity<AlertResponse> result = controller.dismiss(alertId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().status()).isEqualTo(AlertStatus.DISMISSED);
        verify(alertService).dismiss(alertId);
    }

    @Test
    void shouldUsePagination() {
        Page<Alert> alertPage = new PageImpl<>(List.of(), PageRequest.of(2, 10), 0);

        when(alertService.findByTenantId(eq("tenant-001"), any(PageRequest.class))).thenReturn(alertPage);

        controller.list("tenant-001", null, 2, 10);

        verify(alertService).findByTenantId(eq("tenant-001"), argThat(pageable ->
                pageable.getPageNumber() == 2 && pageable.getPageSize() == 10
        ));
    }

    @Test
    void shouldSortByTriggeredAtDesc() {
        Page<Alert> alertPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(alertService.findByTenantId(eq("tenant-001"), any(PageRequest.class))).thenReturn(alertPage);

        controller.list("tenant-001", null, 0, 20);

        verify(alertService).findByTenantId(eq("tenant-001"), argThat(pageable ->
                pageable.getSort().getOrderFor("triggeredAt") != null &&
                pageable.getSort().getOrderFor("triggeredAt").getDirection().isDescending()
        ));
    }

    private AlertResponse createAlertResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getTenantId(),
                alert.getRuleId(),
                alert.getRule() != null ? alert.getRule().getName() : null,
                alert.getSeverity(),
                alert.getStatus(),
                alert.getMessage(),
                alert.getTriggeringEventIds(),
                alert.getTriggeredAt(),
                alert.getAcknowledgedAt(),
                alert.getAcknowledgedBy(),
                alert.getResolution(),
                alert.getResolvedAt()
        );
    }

    private AlertEntity createAlertEntity(Severity severity, AlertStatus status) {
        AlertEntity entity = new AlertEntity();
        entity.setId(UUID.randomUUID());
        entity.setTenantId("tenant-001");
        entity.setSeverity(severity);
        entity.setStatus(status);
        entity.setMessage("Test alert");
        entity.setTriggeredAt(Instant.now());
        return entity;
    }
}
