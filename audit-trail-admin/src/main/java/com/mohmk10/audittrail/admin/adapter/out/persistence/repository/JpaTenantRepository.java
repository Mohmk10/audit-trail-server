package com.mohmk10.audittrail.admin.adapter.out.persistence.repository;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.TenantEntity;
import com.mohmk10.audittrail.admin.domain.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTenantRepository extends JpaRepository<TenantEntity, UUID> {
    Optional<TenantEntity> findBySlug(String slug);
    List<TenantEntity> findByStatus(TenantStatus status);
}
