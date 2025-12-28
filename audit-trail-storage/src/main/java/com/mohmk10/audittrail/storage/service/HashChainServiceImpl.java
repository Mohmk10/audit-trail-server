package com.mohmk10.audittrail.storage.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.storage.adapter.out.persistence.mapper.EventMapper;
import com.mohmk10.audittrail.storage.adapter.out.persistence.repository.JpaEventRepository;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
public class HashChainServiceImpl implements HashChainService {

    public static final String GENESIS_HASH = "GENESIS";
    private static final String HASH_ALGORITHM = "SHA-256";

    private final JpaEventRepository jpaEventRepository;

    public HashChainServiceImpl(JpaEventRepository jpaEventRepository) {
        this.jpaEventRepository = jpaEventRepository;
    }

    @Override
    public String calculateHash(Event event, String previousHash) {
        String dataToHash = buildHashData(event, previousHash);
        return computeSha256(dataToHash);
    }

    @Override
    public boolean verifyChain(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return true;
        }

        String expectedPreviousHash = GENESIS_HASH;

        for (Event event : events) {
            if (!expectedPreviousHash.equals(event.previousHash())) {
                return false;
            }

            String calculatedHash = calculateHash(event, event.previousHash());
            if (!calculatedHash.equals(event.hash())) {
                return false;
            }

            expectedPreviousHash = event.hash();
        }

        return true;
    }

    @Override
    public String getLastHash(String tenantId) {
        return jpaEventRepository.findTopByTenantIdOrderByCreatedAtDesc(tenantId)
                .map(entity -> entity.getHash())
                .orElse(GENESIS_HASH);
    }

    private String buildHashData(Event event, String previousHash) {
        StringBuilder sb = new StringBuilder();
        sb.append(event.timestamp().toString());
        sb.append(event.actor().id());
        sb.append(event.action().type().name());
        sb.append(event.resource().id());
        sb.append(previousHash != null ? previousHash : GENESIS_HASH);
        return sb.toString();
    }

    private String computeSha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
