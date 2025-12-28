package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.detection.domain.Rule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleService {

    Rule create(Rule rule);

    Rule update(UUID id, Rule rule);

    void delete(UUID id);

    Optional<Rule> findById(UUID id);

    List<Rule> findByTenantId(String tenantId);

    List<Rule> findEnabledByTenantId(String tenantId);

    void enable(UUID id);

    void disable(UUID id);
}
