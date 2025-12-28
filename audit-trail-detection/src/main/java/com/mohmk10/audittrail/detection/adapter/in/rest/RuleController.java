package com.mohmk10.audittrail.detection.adapter.in.rest;

import com.mohmk10.audittrail.detection.adapter.in.rest.dto.*;
import com.mohmk10.audittrail.detection.domain.Rule;
import com.mohmk10.audittrail.detection.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleService ruleService;
    private final RuleDtoMapper mapper;

    public RuleController(RuleService ruleService, RuleDtoMapper mapper) {
        this.ruleService = ruleService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<RuleResponse> create(@RequestBody @Valid CreateRuleRequest request) {
        Rule rule = mapper.toDomain(request);
        Rule created = ruleService.create(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<RuleResponse>> list(@RequestParam String tenantId) {
        List<RuleResponse> rules = ruleService.findByTenantId(tenantId)
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleResponse> getById(@PathVariable UUID id) {
        return ruleService.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateRuleRequest request) {
        return ruleService.findById(id)
                .map(existing -> {
                    Rule updated = mapper.applyUpdate(existing, request);
                    Rule saved = ruleService.update(id, updated);
                    return ResponseEntity.ok(mapper.toResponse(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ruleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<RuleResponse> enable(@PathVariable UUID id) {
        ruleService.enable(id);
        return ruleService.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<RuleResponse> disable(@PathVariable UUID id) {
        ruleService.disable(id);
        return ruleService.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
