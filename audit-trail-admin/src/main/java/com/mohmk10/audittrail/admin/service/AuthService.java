package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.adapter.in.rest.dto.AuthResponse;
import com.mohmk10.audittrail.admin.adapter.in.rest.dto.LoginRequest;
import com.mohmk10.audittrail.admin.adapter.in.rest.dto.RegisterRequest;
import com.mohmk10.audittrail.admin.adapter.in.rest.dto.UserResponse;
import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.TenantEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.UserEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.mapper.UserMapper;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.TenantRepository;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.UserRepository;
import com.mohmk10.audittrail.admin.domain.Role;
import com.mohmk10.audittrail.admin.domain.TenantPlan;
import com.mohmk10.audittrail.admin.domain.TenantStatus;
import com.mohmk10.audittrail.admin.domain.User;
import com.mohmk10.audittrail.admin.domain.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, TenantRepository tenantRepository,
                       UserMapper userMapper, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String tenantId = request.tenantId();
        Role role = Role.ADMIN;

        if (tenantId == null || tenantId.isBlank()) {
            tenantId = "tenant-" + UUID.randomUUID().toString().substring(0, 8);
        } else {
            long existingUsers = userRepository.countByTenantId(tenantId);
            if (existingUsers > 0) {
                role = Role.VIEWER;
            }
        }

        String[] nameParts = request.name().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        User user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        UserEntity entity = userMapper.toEntity(user);
        userRepository.save(entity);

        String token = jwtService.generateToken(user);

        return AuthResponse.of(
                UserResponse.from(user),
                token,
                jwtService.getExpiration()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserEntity entity = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        User user = userMapper.toDomain(entity);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }

        entity.setLastLoginAt(Instant.now());
        userRepository.save(entity);

        String token = jwtService.generateToken(user);

        user.setLastLoginAt(Instant.now());

        return AuthResponse.of(
                UserResponse.from(user),
                token,
                jwtService.getExpiration()
        );
    }

    @Transactional
    public AuthResponse refresh(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");

        if (!jwtService.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        String email = jwtService.extractEmail(token);
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User user = userMapper.toDomain(entity);
        String newToken = jwtService.generateToken(user);

        return AuthResponse.of(
                UserResponse.from(user),
                newToken,
                jwtService.getExpiration()
        );
    }

    public User getCurrentUser(String email) {
        UserEntity entity = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toDomain(entity);
    }

    @Transactional
    public User findOrCreateOAuthUser(String email, String name, String provider, String providerId) {
        return userRepository.findByEmail(email)
                .map(existingEntity -> {
                    if (existingEntity.getOauthProvider() == null) {
                        existingEntity.setOauthProvider(provider);
                        existingEntity.setOauthId(providerId);
                        existingEntity.setLastLoginAt(Instant.now());
                        return userMapper.toDomain(userRepository.save(existingEntity));
                    }
                    existingEntity.setLastLoginAt(Instant.now());
                    return userMapper.toDomain(userRepository.save(existingEntity));
                })
                .orElseGet(() -> {
                    TenantEntity tenant = createDefaultTenant(name);

                    String[] nameParts = name != null ? name.trim().split("\\s+", 2) : new String[]{email.split("@")[0]};
                    String firstName = nameParts[0];
                    String lastName = nameParts.length > 1 ? nameParts[1] : "";

                    User user = User.builder()
                            .id(UUID.randomUUID())
                            .tenantId(tenant.getSlug())
                            .email(email)
                            .firstName(firstName)
                            .lastName(lastName)
                            .oauthProvider(provider)
                            .oauthId(providerId)
                            .role(Role.ADMIN)
                            .status(UserStatus.ACTIVE)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .lastLoginAt(Instant.now())
                            .build();

                    UserEntity entity = userMapper.toEntity(user);
                    return userMapper.toDomain(userRepository.save(entity));
                });
    }

    private TenantEntity createDefaultTenant(String userName) {
        String safeName = userName != null ? userName : "User";
        String slug = UUID.randomUUID().toString().substring(0, 8);

        TenantEntity tenant = new TenantEntity();
        tenant.setId(UUID.randomUUID());
        tenant.setName(safeName + "'s Organization");
        tenant.setSlug(slug);
        tenant.setPlan(TenantPlan.FREE);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setCreatedAt(Instant.now());
        tenant.setUpdatedAt(Instant.now());

        return tenantRepository.save(tenant);
    }
}
