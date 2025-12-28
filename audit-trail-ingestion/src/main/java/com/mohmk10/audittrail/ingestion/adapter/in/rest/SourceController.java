package com.mohmk10.audittrail.ingestion.adapter.in.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sources")
public class SourceController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listSources() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "default", "name", "Default Source", "status", "ACTIVE")
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerSource(@RequestBody Map<String, String> request) {
        String sourceId = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of(
                "id", sourceId,
                "name", request.getOrDefault("name", "New Source"),
                "status", "ACTIVE"
        ));
    }

    @PostMapping("/{id}/apikey")
    public ResponseEntity<Map<String, String>> generateApiKey(@PathVariable String id) {
        String apiKey = "atk_" + UUID.randomUUID().toString().replace("-", "");
        return ResponseEntity.ok(Map.of(
                "sourceId", id,
                "apiKey", apiKey,
                "message", "Store this API key securely. It will not be shown again."
        ));
    }
}
