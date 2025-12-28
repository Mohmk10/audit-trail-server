package com.mohmk10.audittrail.detection.adapter.out.persistence.repository;

import com.mohmk10.audittrail.detection.adapter.out.persistence.entity.AlertEntity;
import com.mohmk10.audittrail.detection.domain.AlertStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAlertRepository extends JpaRepository<AlertEntity, UUID> {

    Page<AlertEntity> findByTenantId(String tenantId, Pageable pageable);

    List<AlertEntity> findByTenantIdAndStatus(String tenantId, AlertStatus status);

    Page<AlertEntity> findByTenantIdAndStatus(String tenantId, AlertStatus status, Pageable pageable);

    long countByTenantIdAndStatus(String tenantId, AlertStatus status);

    List<AlertEntity> findByTenantIdOrderByTriggeredAtDesc(String tenantId);

    void deleteByTenantId(String tenantId);
}
