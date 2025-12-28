package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.detection.adapter.out.persistence.entity.RuleEntity;
import com.mohmk10.audittrail.detection.adapter.out.persistence.mapper.RuleMapper;
import com.mohmk10.audittrail.detection.adapter.out.persistence.repository.JpaRuleRepository;
import com.mohmk10.audittrail.detection.domain.Rule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RuleServiceImpl implements RuleService {

    private final JpaRuleRepository ruleRepository;
    private final RuleMapper ruleMapper;

    public RuleServiceImpl(JpaRuleRepository ruleRepository, RuleMapper ruleMapper) {
        this.ruleRepository = ruleRepository;
        this.ruleMapper = ruleMapper;
    }

    @Override
    public Rule create(Rule rule) {
        Rule newRule = rule.toBuilder()
                .id(UUID.randomUUID())
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        RuleEntity entity = ruleMapper.toEntity(newRule);
        RuleEntity saved = ruleRepository.save(entity);
        return ruleMapper.toDomain(saved);
    }

    @Override
    public Rule update(UUID id, Rule rule) {
        RuleEntity existing = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));

        Rule updatedRule = rule.toBuilder()
                .id(id)
                .tenantId(existing.getTenantId())
                .createdAt(existing.getCreatedAt())
                .updatedAt(Instant.now())
                .build();

        RuleEntity entity = ruleMapper.toEntity(updatedRule);
        RuleEntity saved = ruleRepository.save(entity);
        return ruleMapper.toDomain(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!ruleRepository.existsById(id)) {
            throw new RuntimeException("Rule not found: " + id);
        }
        ruleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rule> findById(UUID id) {
        return ruleRepository.findById(id)
                .map(ruleMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rule> findByTenantId(String tenantId) {
        return ruleRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(ruleMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Rule> findEnabledByTenantId(String tenantId) {
        return ruleRepository.findByTenantIdAndEnabled(tenantId, true)
                .stream()
                .map(ruleMapper::toDomain)
                .toList();
    }

    @Override
    public void enable(UUID id) {
        RuleEntity entity = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
        entity.setEnabled(true);
        entity.setUpdatedAt(Instant.now());
        ruleRepository.save(entity);
    }

    @Override
    public void disable(UUID id) {
        RuleEntity entity = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
        entity.setEnabled(false);
        entity.setUpdatedAt(Instant.now());
        ruleRepository.save(entity);
    }
}
