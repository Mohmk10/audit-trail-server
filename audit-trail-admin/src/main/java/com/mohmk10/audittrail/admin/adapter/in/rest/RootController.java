package com.mohmk10.audittrail.admin.adapter.in.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> simpleHealth() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "service", "Audit Trail Server",
                "version", "1.0.0",
                "status", "running"
        ));
    }
}
