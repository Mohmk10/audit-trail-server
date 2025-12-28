package com.mohmk10.audittrail.admin.application;

import com.mohmk10.audittrail.admin.domain.Role;
import com.mohmk10.audittrail.admin.domain.User;
import com.mohmk10.audittrail.admin.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User createUser(String tenantId, String email, String password, String firstName, String lastName, Role role);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Page<User> findByTenantId(String tenantId, Pageable pageable);

    Page<User> findByFilters(String tenantId, UserStatus status, Role role, Pageable pageable);

    User updateUser(UUID id, String firstName, String lastName, Role role);

    User changePassword(UUID id, String newPassword);

    User updateStatus(UUID id, UserStatus status);

    User recordLogin(UUID id, String ipAddress);

    void deleteUser(UUID id);

    boolean existsByEmail(String email);

    long countByTenantId(String tenantId);

    long countActiveByTenantId(String tenantId);
}
