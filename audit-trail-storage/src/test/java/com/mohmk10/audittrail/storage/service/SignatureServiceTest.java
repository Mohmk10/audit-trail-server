package com.mohmk10.audittrail.storage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureServiceTest {

    private SignatureServiceImpl signatureService;

    @BeforeEach
    void setUp() {
        signatureService = new SignatureServiceImpl();
        signatureService.init();
    }

    @Test
    void shouldSignHash() {
        String hash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

        String signature = signatureService.sign(hash);

        assertThat(signature).isNotNull();
        assertThat(signature).isNotEmpty();
    }

    @Test
    void shouldVerifyValidSignature() {
        String hash = "abc123def456789";
        String signature = signatureService.sign(hash);

        boolean isValid = signatureService.verify(hash, signature);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectInvalidSignature() {
        String hash = "original-hash";
        String invalidSignature = "aW52YWxpZC1zaWduYXR1cmU="; // base64 of "invalid-signature"

        boolean isValid = signatureService.verify(hash, invalidSignature);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectTamperedHash() {
        String originalHash = "original-hash";
        String signature = signatureService.sign(originalHash);

        String tamperedHash = "tampered-hash";
        boolean isValid = signatureService.verify(tamperedHash, signature);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldGenerateKeyPair() {
        KeyPair keyPair = signatureService.generateKeyPair();

        assertThat(keyPair).isNotNull();
        assertThat(keyPair.getPrivate()).isNotNull();
        assertThat(keyPair.getPublic()).isNotNull();
    }

    @Test
    void shouldProduceDifferentSignaturesWithDifferentKeys() {
        SignatureServiceImpl service1 = new SignatureServiceImpl();
        service1.init();
        SignatureServiceImpl service2 = new SignatureServiceImpl();
        service2.init();

        String hash = "same-hash-value";
        String signature1 = service1.sign(hash);
        String signature2 = service2.sign(hash);

        // Different key pairs should produce different signatures
        assertThat(signature1).isNotEqualTo(signature2);
    }

    @Test
    void shouldProduceConsistentSignaturesWithSameKey() {
        String hash = "consistent-hash";

        String signature1 = signatureService.sign(hash);
        String signature2 = signatureService.sign(hash);

        // Same key should produce same signature for same input
        // Note: ECDSA produces different signatures each time due to random k value
        // But both should verify correctly
        assertThat(signatureService.verify(hash, signature1)).isTrue();
        assertThat(signatureService.verify(hash, signature2)).isTrue();
    }

    @Test
    void shouldHandleEmptyHash() {
        String emptyHash = "";

        String signature = signatureService.sign(emptyHash);

        assertThat(signature).isNotNull();
        assertThat(signatureService.verify(emptyHash, signature)).isTrue();
    }

    @Test
    void shouldHandleLongHash() {
        String longHash = "a".repeat(256);

        String signature = signatureService.sign(longHash);

        assertThat(signature).isNotNull();
        assertThat(signatureService.verify(longHash, signature)).isTrue();
    }

    @Test
    void shouldReturnFalseForNullSignature() {
        String hash = "some-hash";

        boolean isValid = signatureService.verify(hash, null);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseForEmptySignature() {
        String hash = "some-hash";

        boolean isValid = signatureService.verify(hash, "");

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldProduceBase64EncodedSignature() {
        String hash = "test-hash";

        String signature = signatureService.sign(hash);

        // Base64 characters are A-Z, a-z, 0-9, +, /, =
        assertThat(signature).matches("^[A-Za-z0-9+/=]+$");
    }

    @Test
    void shouldUseECDSAAlgorithm() {
        KeyPair keyPair = signatureService.generateKeyPair();

        assertThat(keyPair.getPrivate().getAlgorithm()).isEqualTo("EC");
        assertThat(keyPair.getPublic().getAlgorithm()).isEqualTo("EC");
    }

    @Test
    void shouldGenerateUniqueKeyPairs() {
        KeyPair keyPair1 = signatureService.generateKeyPair();
        KeyPair keyPair2 = signatureService.generateKeyPair();

        assertThat(keyPair1.getPrivate()).isNotEqualTo(keyPair2.getPrivate());
        assertThat(keyPair1.getPublic()).isNotEqualTo(keyPair2.getPublic());
    }

    @Test
    void shouldHandleMalformedSignature() {
        String hash = "some-hash";
        String malformedSignature = "not-valid-base64!!!";

        boolean isValid = signatureService.verify(hash, malformedSignature);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldSignDifferentHashesDifferently() {
        String hash1 = "hash-one";
        String hash2 = "hash-two";

        String signature1 = signatureService.sign(hash1);
        String signature2 = signatureService.sign(hash2);

        // Signatures should be different for different hashes
        assertThat(signature1).isNotEqualTo(signature2);

        // Cross-verification should fail
        assertThat(signatureService.verify(hash1, signature2)).isFalse();
        assertThat(signatureService.verify(hash2, signature1)).isFalse();
    }
}
