package com.mohmk10.audittrail.admin.service;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.ApiKeyEntity;
import com.mohmk10.audittrail.admin.adapter.out.persistence.mapper.ApiKeyMapper;
import com.mohmk10.audittrail.admin.adapter.out.persistence.repository.JpaApiKeyRepository;
import com.mohmk10.audittrail.admin.domain.ApiKey;
import com.mohmk10.audittrail.admin.domain.ApiKeyCreationResult;
import com.mohmk10.audittrail.admin.domain.ApiKeyScope;
import com.mohmk10.audittrail.admin.domain.ApiKeyStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ApiKeyServiceImpl implements ApiKeyService {

    private static final String KEY_PREFIX = "atk_";
    private static final int KEY_LENGTH = 32;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final JpaApiKeyRepository repository;
    private final ApiKeyMapper mapper;
    private final SecureRandom secureRandom;

    @Value("${admin.api-key.default-expiration-days:365}")
    private int defaultExpirationDays;

    public ApiKeyServiceImpl(JpaApiKeyRepository repository, ApiKeyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public ApiKeyCreationResult create(ApiKey apiKey) {
        String plainTextKey = generateKey();
        String keyHash = hashKey(plainTextKey);
        String keyPrefix = plainTextKey.substring(0, Math.min(8, plainTextKey.length()));

        if (apiKey.getId() == null) {
            apiKey.setId(UUID.randomUUID());
        }
        apiKey.setKeyHash(keyHash);
        apiKey.setKeyPrefix(keyPrefix);
        apiKey.setStatus(ApiKeyStatus.ACTIVE);
        apiKey.setCreatedAt(Instant.now());

        if (apiKey.getExpiresAt() == null) {
            apiKey.setExpiresAt(Instant.now().plus(defaultExpirationDays, ChronoUnit.DAYS));
        }

        ApiKeyEntity entity = mapper.toEntity(apiKey);
        ApiKeyEntity saved = repository.save(entity);
        ApiKey savedApiKey = mapper.toDomain(saved);

        return new ApiKeyCreationResult(savedApiKey, plainTextKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApiKey> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApiKey> findByKey(String key) {
        String keyHash = hashKey(key);
        return repository.findByKeyHash(keyHash).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKey> findByTenantId(String tenantId) {
        return repository.findByTenantId(tenantId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKey> findBySourceId(UUID sourceId) {
        return repository.findBySourceId(sourceId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public ApiKeyCreationResult rotate(UUID id) {
        ApiKeyEntity existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API Key not found: " + id));

        existing.setStatus(ApiKeyStatus.REVOKED);
        repository.save(existing);

        ApiKey newApiKey = ApiKey.builder()
                .tenantId(existing.getTenantId())
                .sourceId(existing.getSourceId())
                .name(existing.getName() + " (rotated)")
                .scopes(mapper.toDomain(existing).getScopes())
                .build();

        return create(newApiKey);
    }

    @Override
    public void revoke(UUID id) {
        ApiKeyEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API Key not found: " + id));
        entity.setStatus(ApiKeyStatus.REVOKED);
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validate(String key, ApiKeyScope requiredScope) {
        Optional<ApiKey> apiKeyOpt = findByKey(key);

        if (apiKeyOpt.isEmpty()) {
            return false;
        }

        ApiKey apiKey = apiKeyOpt.get();

        if (apiKey.getStatus() != ApiKeyStatus.ACTIVE) {
            return false;
        }

        if (apiKey.getExpiresAt() != null && apiKey.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }

        if (apiKey.getScopes().contains(ApiKeyScope.ADMIN)) {
            return true;
        }

        return apiKey.getScopes().contains(requiredScope);
    }

    @Override
    public void updateLastUsed(UUID id, String ip) {
        repository.updateLastUsed(id, Instant.now(), ip);
    }

    private String generateKey() {
        StringBuilder key = new StringBuilder(KEY_PREFIX);
        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            key.append(CHARACTERS.charAt(index));
        }
        return key.toString();
    }

    private String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
