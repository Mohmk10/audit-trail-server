package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.detection.adapter.out.persistence.entity.AlertEntity;
import com.mohmk10.audittrail.detection.adapter.out.persistence.mapper.AlertMapper;
import com.mohmk10.audittrail.detection.adapter.out.persistence.repository.JpaAlertRepository;
import com.mohmk10.audittrail.detection.domain.Alert;
import com.mohmk10.audittrail.detection.domain.AlertStatus;
import com.mohmk10.audittrail.detection.domain.Severity;
import com.mohmk10.audittrail.detection.fixtures.DetectionTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock
    private JpaAlertRepository alertRepository;

    @Mock
    private AlertMapper alertMapper;

    private AlertServiceImpl alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertServiceImpl(alertRepository, alertMapper);
    }

    @Test
    void shouldCreateAlert() {
        Alert alert = DetectionTestFixtures.createTestAlert();
        AlertEntity entity = createAlertEntity(alert);
        Alert savedAlert = alert.toBuilder().build();

        when(alertMapper.toEntity(any(Alert.class))).thenReturn(entity);
        when(alertRepository.save(entity)).thenReturn(entity);
        when(alertMapper.toDomain(entity)).thenReturn(savedAlert);

        Alert result = alertService.create(alert);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AlertStatus.OPEN);
        verify(alertRepository).save(any(AlertEntity.class));
    }

    @Test
    void shouldGenerateIdWhenCreatingAlertWithoutId() {
        Alert alertWithoutId = Alert.builder()
                .tenantId("tenant-001")
                .severity(Severity.HIGH)
                .message("Test alert")
                .build();

        AlertEntity entity = new AlertEntity();
        entity.setId(UUID.randomUUID());
        Alert savedAlert = alertWithoutId.toBuilder().id(entity.getId()).build();

        when(alertMapper.toEntity(any(Alert.class))).thenReturn(entity);
        when(alertRepository.save(entity)).thenReturn(entity);
        when(alertMapper.toDomain(entity)).thenReturn(savedAlert);

        Alert result = alertService.create(alertWithoutId);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertMapper).toEntity(alertCaptor.capture());

        assertThat(alertCaptor.getValue().getId()).isNotNull();
    }

    @Test
    void shouldFindAlertById() {
        UUID alertId = UUID.randomUUID();
        AlertEntity entity = new AlertEntity();
        entity.setId(alertId);
        Alert alert = DetectionTestFixtures.createTestAlert();

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(entity));
        when(alertMapper.toDomain(entity)).thenReturn(alert);

        Optional<Alert> result = alertService.findById(alertId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(alert);
    }

    @Test
    void shouldReturnEmptyWhenAlertNotFound() {
        UUID alertId = UUID.randomUUID();

        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        Optional<Alert> result = alertService.findById(alertId);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAlertsByTenantId() {
        Pageable pageable = PageRequest.of(0, 10);
        AlertEntity entity1 = new AlertEntity();
        AlertEntity entity2 = new AlertEntity();
        Page<AlertEntity> entityPage = new PageImpl<>(List.of(entity1, entity2), pageable, 2);
        Alert alert1 = DetectionTestFixtures.createTestAlert();
        Alert alert2 = DetectionTestFixtures.createTestAlert();

        when(alertRepository.findByTenantId("tenant-001", pageable)).thenReturn(entityPage);
        when(alertMapper.toDomain(entity1)).thenReturn(alert1);
        when(alertMapper.toDomain(entity2)).thenReturn(alert2);

        Page<Alert> result = alertService.findByTenantId("tenant-001", pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldFindOpenAlerts() {
        AlertEntity entity1 = new AlertEntity();
        AlertEntity entity2 = new AlertEntity();
        Alert alert1 = DetectionTestFixtures.createTestAlert();
        Alert alert2 = DetectionTestFixtures.createTestAlert();

        when(alertRepository.findByTenantIdAndStatus("tenant-001", AlertStatus.OPEN))
                .thenReturn(List.of(entity1, entity2));
        when(alertMapper.toDomain(entity1)).thenReturn(alert1);
        when(alertMapper.toDomain(entity2)).thenReturn(alert2);

        List<Alert> result = alertService.findOpenAlerts("tenant-001");

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldAcknowledgeAlert() {
        UUID alertId = UUID.randomUUID();
        AlertEntity entity = createOpenAlertEntity(alertId);
        Alert acknowledgedAlert = DetectionTestFixtures.createAcknowledgedAlert();

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(entity));
        when(alertRepository.save(entity)).thenReturn(entity);
        when(alertMapper.toDomain(entity)).thenReturn(acknowledgedAlert);

        Alert result = alertService.acknowledge(alertId, "admin@example.com");

        assertThat(result.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(entity.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(entity.getAcknowledgedBy()).isEqualTo("admin@example.com");
        assertThat(entity.getAcknowledgedAt()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenAcknowledgingNonOpenAlert() {
        UUID alertId = UUID.randomUUID();
        AlertEntity entity = new AlertEntity();
        entity.setId(alertId);
        entity.setStatus(AlertStatus.ACKNOWLEDGED);

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> alertService.acknowledge(alertId, "admin@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not in OPEN status");
    }

    @Test
    void shouldThrowExceptionWhenAcknowledgingNonExistentAlert() {
        UUID alertId = UUID.randomUUID();

        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.acknowledge(alertId, "admin@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Alert not found");
    }

    @Test
    void shouldResolveAlert() {
        UUID alertId = UUID.randomUUID();
        AlertEntity entity = createOpenAlertEntity(alertId);
        Alert resolvedAlert = DetectionTestFixtures.createResolvedAlert();

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(entity));
        when(alertRepository.save(entity)).thenReturn(entity);
        when(alertMapper.toDomain(entity)).thenReturn(resolvedAlert);

        Alert result = alertService.resolve(alertId, "Issue fixed");

        assertThat(result.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(entity.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(entity.getResolution()).isEqualTo("Issue fixed");
        assertThat(entity.getResolvedAt()).isNotNull();
    }

    @Test
    void shouldResolveAcknowledgedAlert() {
        UUID alertId = UUID.randomUUID();
        AlertEntity entity = new AlertEntity();
        entity.setId(alertId);
        entity.setStatus(AlertStatus.ACKNOWLEDGED);
        Alert resolvedAlert = DetectionTestFixtures.createResolvedAlert();

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(entity));
        when(alertRepository.save(entity)).thenReturn(entity);
        when(alertMapper.toDomain(entity)).thenReturn(resolvedAlert);

        Alert result = alertService.resolve(alertId, "Issue fixed");

        assertThat(result.getStatus()).isEqualTo(AlertStatus.RESOLVED);
    }

    @Test
    void shouldThrowExceptionWhenResolvingAlreadyResolvedAlert() {
        UUID alertId = UUID.randomUUID();
        AlertEntity entity = new AlertEntity();
        entity.setId(alertId);
        entity.setStatus(AlertStatus.RESOLVED);

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> alertService.resolve(alertId, "Issue fixed"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already resolved or dismissed");
    }

    @Test
    void shouldDismissAlert() {
        UUID alertId = UUID.randomUUID();
        AlertEntity entity = createOpenAlertEntity(alertId);
        Alert dismissedAlert = Alert.builder()
                .id(alertId)
                .status(AlertStatus.DISMISSED)
                .build();

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(entity));
        when(alertRepository.save(entity)).thenReturn(entity);
        when(alertMapper.toDomain(entity)).thenReturn(dismissedAlert);

        Alert result = alertService.dismiss(alertId);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.DISMISSED);
        assertThat(entity.getStatus()).isEqualTo(AlertStatus.DISMISSED);
        assertThat(entity.getResolvedAt()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenDismissingAlreadyDismissedAlert() {
        UUID alertId = UUID.randomUUID();
        AlertEntity entity = new AlertEntity();
        entity.setId(alertId);
        entity.setStatus(AlertStatus.DISMISSED);

        when(alertRepository.findById(alertId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> alertService.dismiss(alertId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already resolved or dismissed");
    }

    @Test
    void shouldCountOpenAlerts() {
        when(alertRepository.countByTenantIdAndStatus("tenant-001", AlertStatus.OPEN))
                .thenReturn(5L);

        long count = alertService.countOpenAlerts("tenant-001");

        assertThat(count).isEqualTo(5L);
    }

    private AlertEntity createAlertEntity(Alert alert) {
        AlertEntity entity = new AlertEntity();
        entity.setId(alert.getId());
        entity.setTenantId(alert.getTenantId());
        entity.setSeverity(alert.getSeverity());
        entity.setStatus(alert.getStatus());
        entity.setMessage(alert.getMessage());
        entity.setTriggeredAt(Instant.now());
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private AlertEntity createOpenAlertEntity(UUID id) {
        AlertEntity entity = new AlertEntity();
        entity.setId(id);
        entity.setTenantId("tenant-001");
        entity.setSeverity(Severity.HIGH);
        entity.setStatus(AlertStatus.OPEN);
        entity.setMessage("Test alert");
        entity.setTriggeredAt(Instant.now());
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
