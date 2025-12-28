package com.mohmk10.audittrail.detection.adapter.out.persistence.repository;

import com.mohmk10.audittrail.detection.adapter.out.persistence.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaRuleRepository extends JpaRepository<RuleEntity, UUID> {

    List<RuleEntity> findByTenantId(String tenantId);

    List<RuleEntity> findByTenantIdAndEnabled(String tenantId, boolean enabled);

    List<RuleEntity> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    void deleteByTenantId(String tenantId);
}
