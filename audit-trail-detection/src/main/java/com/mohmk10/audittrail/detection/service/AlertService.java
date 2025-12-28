package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.detection.domain.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertService {

    Alert create(Alert alert);

    Optional<Alert> findById(UUID id);

    Page<Alert> findByTenantId(String tenantId, Pageable pageable);

    List<Alert> findOpenAlerts(String tenantId);

    Alert acknowledge(UUID id, String acknowledgedBy);

    Alert resolve(UUID id, String resolution);

    Alert dismiss(UUID id);

    long countOpenAlerts(String tenantId);
}
