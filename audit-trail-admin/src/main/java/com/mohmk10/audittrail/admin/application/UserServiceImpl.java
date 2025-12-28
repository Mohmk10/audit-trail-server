package com.mohmk10.audittrail.admin.application;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.UserEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.mapper.UserMapper;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.UserRepository;
import com.mohmk10.audittrail.admin.domain.Role;
import com.mohmk10.audittrail.admin.domain.User;
import com.mohmk10.audittrail.admin.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(String tenantId, String email, String password, String firstName, String lastName, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .email(email.toLowerCase())
                .passwordHash(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase()).map(userMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findByTenantId(String tenantId, Pageable pageable) {
        return userRepository.findByTenantId(tenantId, pageable).map(userMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findByFilters(String tenantId, UserStatus status, Role role, Pageable pageable) {
        return userRepository.findByFilters(tenantId, status, role, pageable).map(userMapper::toDomain);
    }

    @Override
    public User updateUser(UUID id, String firstName, String lastName, Role role) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        entity.setFirstName(firstName);
        entity.setLastName(lastName);
        entity.setRole(role);

        UserEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public User changePassword(UUID id, String newPassword) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        entity.setPasswordHash(passwordEncoder.encode(newPassword));

        UserEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public User updateStatus(UUID id, UserStatus status) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        entity.setStatus(status);

        UserEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public User recordLogin(UUID id, String ipAddress) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        entity.setLastLoginAt(Instant.now());
        entity.setLastLoginIp(ipAddress);

        UserEntity saved = userRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTenantId(String tenantId) {
        return userRepository.countByTenantId(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByTenantId(String tenantId) {
        return userRepository.countByTenantIdAndStatus(tenantId, UserStatus.ACTIVE);
    }
}
