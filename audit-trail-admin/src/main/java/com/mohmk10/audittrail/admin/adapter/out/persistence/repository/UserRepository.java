package com.mohmk10.audittrail.admin.adapter.out.persistence.repository;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.UserEntity;
import com.mohmk10.audittrail.admin.domain.Role;
import com.mohmk10.audittrail.admin.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

    boolean existsByEmail(String email);

    Page<UserEntity> findByTenantId(String tenantId, Pageable pageable);

    List<UserEntity> findByTenantIdAndStatus(String tenantId, UserStatus status);

    List<UserEntity> findByTenantIdAndRole(String tenantId, Role role);

    @Query("SELECT u FROM UserEntity u WHERE u.tenantId = :tenantId " +
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:role IS NULL OR u.role = :role)")
    Page<UserEntity> findByFilters(
            @Param("tenantId") String tenantId,
            @Param("status") UserStatus status,
            @Param("role") Role role,
            Pageable pageable
    );

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.tenantId = :tenantId AND u.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.tenantId = :tenantId")
    long countByTenantId(@Param("tenantId") String tenantId);
}
