package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.detection.adapter.out.persistence.entity.AlertEntity;
import com.mohmk10.audittrail.detection.adapter.out.persistence.mapper.AlertMapper;
import com.mohmk10.audittrail.detection.adapter.out.persistence.repository.JpaAlertRepository;
import com.mohmk10.audittrail.detection.domain.Alert;
import com.mohmk10.audittrail.detection.domain.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final JpaAlertRepository alertRepository;
    private final AlertMapper alertMapper;

    public AlertServiceImpl(JpaAlertRepository alertRepository, AlertMapper alertMapper) {
        this.alertRepository = alertRepository;
        this.alertMapper = alertMapper;
    }

    @Override
    public Alert create(Alert alert) {
        Alert newAlert = alert.toBuilder()
                .id(alert.getId() != null ? alert.getId() : UUID.randomUUID())
                .status(AlertStatus.OPEN)
                .triggeredAt(alert.getTriggeredAt() != null ? alert.getTriggeredAt() : Instant.now())
                .createdAt(Instant.now())
                .build();

        AlertEntity entity = alertMapper.toEntity(newAlert);
        AlertEntity saved = alertRepository.save(entity);
        return alertMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Alert> findById(UUID id) {
        return alertRepository.findById(id)
                .map(alertMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Alert> findByTenantId(String tenantId, Pageable pageable) {
        return alertRepository.findByTenantId(tenantId, pageable)
                .map(alertMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> findOpenAlerts(String tenantId) {
        return alertRepository.findByTenantIdAndStatus(tenantId, AlertStatus.OPEN)
                .stream()
                .map(alertMapper::toDomain)
                .toList();
    }

    @Override
    public Alert acknowledge(UUID id, String acknowledgedBy) {
        AlertEntity entity = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));

        if (entity.getStatus() != AlertStatus.OPEN) {
            throw new RuntimeException("Alert is not in OPEN status");
        }

        entity.setStatus(AlertStatus.ACKNOWLEDGED);
        entity.setAcknowledgedAt(Instant.now());
        entity.setAcknowledgedBy(acknowledgedBy);

        AlertEntity saved = alertRepository.save(entity);
        return alertMapper.toDomain(saved);
    }

    @Override
    public Alert resolve(UUID id, String resolution) {
        AlertEntity entity = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));

        if (entity.getStatus() == AlertStatus.RESOLVED || entity.getStatus() == AlertStatus.DISMISSED) {
            throw new RuntimeException("Alert is already resolved or dismissed");
        }

        entity.setStatus(AlertStatus.RESOLVED);
        entity.setResolution(resolution);
        entity.setResolvedAt(Instant.now());

        AlertEntity saved = alertRepository.save(entity);
        return alertMapper.toDomain(saved);
    }

    @Override
    public Alert dismiss(UUID id) {
        AlertEntity entity = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));

        if (entity.getStatus() == AlertStatus.RESOLVED || entity.getStatus() == AlertStatus.DISMISSED) {
            throw new RuntimeException("Alert is already resolved or dismissed");
        }

        entity.setStatus(AlertStatus.DISMISSED);
        entity.setResolvedAt(Instant.now());

        AlertEntity saved = alertRepository.save(entity);
        return alertMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOpenAlerts(String tenantId) {
        return alertRepository.countByTenantIdAndStatus(tenantId, AlertStatus.OPEN);
    }
}
