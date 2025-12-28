package com.mohmk10.audittrail.admin.adapter.in.rest.dto;

import com.mohmk10.audittrail.admin.domain.*;
import org.springframework.stereotype.Component;

@Component
public class AdminDtoMapper {

    public Tenant toTenantDomain(CreateTenantRequest request) {
        return Tenant.builder()
                .name(request.name())
                .slug(request.slug())
                .description(request.description())
                .plan(request.plan())
                .quota(toQuotaDomain(request.quota()))
                .settings(request.settings())
                .build();
    }

    public Tenant toTenantDomain(UpdateTenantRequest request) {
        return Tenant.builder()
                .name(request.name())
                .description(request.description())
                .plan(request.plan())
                .quota(toQuotaDomain(request.quota()))
                .settings(request.settings())
                .build();
    }

    public TenantResponse toTenantResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getDescription(),
                tenant.getStatus(),
                tenant.getPlan(),
                toQuotaDto(tenant.getQuota()),
                tenant.getSettings(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }

    public Source toSourceDomain(CreateSourceRequest request) {
        return Source.builder()
                .tenantId(request.tenantId())
                .name(request.name())
                .description(request.description())
                .type(request.type())
                .config(request.config())
                .build();
    }

    public Source toSourceDomain(UpdateSourceRequest request) {
        return Source.builder()
                .name(request.name())
                .description(request.description())
                .type(request.type())
                .config(request.config())
                .build();
    }

    public SourceResponse toSourceResponse(Source source) {
        return new SourceResponse(
                source.getId(),
                source.getTenantId(),
                source.getName(),
                source.getDescription(),
                source.getType(),
                source.getStatus(),
                source.getConfig(),
                source.getCreatedAt(),
                source.getLastEventAt(),
                source.getEventCount()
        );
    }

    public ApiKey toApiKeyDomain(CreateApiKeyRequest request) {
        return ApiKey.builder()
                .tenantId(request.tenantId())
                .sourceId(request.sourceId())
                .name(request.name())
                .scopes(request.scopes())
                .expiresAt(request.expiresAt())
                .build();
    }

    public ApiKeyResponse toApiKeyResponse(ApiKey apiKey) {
        return new ApiKeyResponse(
                apiKey.getId(),
                apiKey.getTenantId(),
                apiKey.getSourceId(),
                apiKey.getName(),
                apiKey.getKeyPrefix(),
                apiKey.getScopes(),
                apiKey.getStatus(),
                apiKey.getCreatedAt(),
                apiKey.getExpiresAt(),
                apiKey.getLastUsedAt(),
                apiKey.getLastUsedIp()
        );
    }

    public ApiKeyCreatedResponse toApiKeyCreatedResponse(ApiKeyCreationResult result) {
        ApiKey apiKey = result.apiKey();
        return ApiKeyCreatedResponse.from(
                apiKey.getId(),
                apiKey.getTenantId(),
                apiKey.getSourceId(),
                apiKey.getName(),
                result.plainTextKey(),
                apiKey.getKeyPrefix(),
                apiKey.getScopes(),
                apiKey.getStatus(),
                apiKey.getCreatedAt(),
                apiKey.getExpiresAt()
        );
    }

    private TenantQuota toQuotaDomain(TenantQuotaDto dto) {
        if (dto == null) {
            return null;
        }
        return new TenantQuota(
                dto.maxEventsPerDay() != null ? dto.maxEventsPerDay() : 0,
                dto.maxEventsPerMonth() != null ? dto.maxEventsPerMonth() : 0,
                dto.maxSources() != null ? dto.maxSources() : 0,
                dto.maxApiKeys() != null ? dto.maxApiKeys() : 0,
                dto.maxUsers() != null ? dto.maxUsers() : 0,
                dto.retentionDays() != null ? dto.retentionDays() : 0
        );
    }

    private TenantQuotaDto toQuotaDto(TenantQuota quota) {
        if (quota == null) {
            return null;
        }
        return new TenantQuotaDto(
                quota.getMaxEventsPerDay(),
                quota.getMaxEventsPerMonth(),
                quota.getMaxSources(),
                quota.getMaxApiKeys(),
                quota.getMaxUsers(),
                quota.getRetentionDays()
        );
    }
}
