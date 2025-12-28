package com.mohmk10.audittrail.reporting.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class ReportCertificationService {

    public String calculateChecksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public String sign(byte[] content, String reportId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(content);
            digest.update(reportId.getBytes(StandardCharsets.UTF_8));
            digest.update(getSigningKey());
            byte[] signature = digest.digest();
            return Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public boolean verify(byte[] content, String reportId, String signature) {
        String expectedSignature = sign(content, reportId);
        return expectedSignature.equals(signature);
    }

    private byte[] getSigningKey() {
        return "audit-trail-report-signing-key-v1".getBytes(StandardCharsets.UTF_8);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
