package com.mohmk10.audittrail.admin.adapter.out.persistence.mapper;

import com.mohmk10.audittrail.admin.adapter.out.persistence.entity.TenantEntity;
import com.mohmk10.audittrail.admin.domain.Tenant;
import com.mohmk10.audittrail.admin.domain.TenantQuota;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TenantMapper {

    public Tenant toDomain(TenantEntity entity) {
        if (entity == null) {
            return null;
        }

        return Tenant.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .plan(entity.getPlan())
                .quota(mapQuotaFromEntity(entity.getQuota()))
                .settings(entity.getSettings())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TenantEntity toEntity(Tenant domain) {
        if (domain == null) {
            return null;
        }

        TenantEntity entity = new TenantEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setSlug(domain.getSlug());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setPlan(domain.getPlan());
        entity.setQuota(mapQuotaToEntity(domain.getQuota()));
        entity.setSettings(domain.getSettings());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    private TenantQuota mapQuotaFromEntity(Map<String, Object> quotaMap) {
        if (quotaMap == null || quotaMap.isEmpty()) {
            return null;
        }

        TenantQuota quota = new TenantQuota();
        quota.setMaxEventsPerDay(getLong(quotaMap, "maxEventsPerDay"));
        quota.setMaxEventsPerMonth(getLong(quotaMap, "maxEventsPerMonth"));
        quota.setMaxSources(getInt(quotaMap, "maxSources"));
        quota.setMaxApiKeys(getInt(quotaMap, "maxApiKeys"));
        quota.setMaxUsers(getInt(quotaMap, "maxUsers"));
        quota.setRetentionDays(getInt(quotaMap, "retentionDays"));
        return quota;
    }

    private Map<String, Object> mapQuotaToEntity(TenantQuota quota) {
        if (quota == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("maxEventsPerDay", quota.getMaxEventsPerDay());
        map.put("maxEventsPerMonth", quota.getMaxEventsPerMonth());
        map.put("maxSources", quota.getMaxSources());
        map.put("maxApiKeys", quota.getMaxApiKeys());
        map.put("maxUsers", quota.getMaxUsers());
        map.put("retentionDays", quota.getRetentionDays());
        return map;
    }

    private long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    private int getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
