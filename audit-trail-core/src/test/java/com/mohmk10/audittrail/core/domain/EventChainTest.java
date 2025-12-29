package com.mohmk10.audittrail.core.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EventChainTest {

    @Test
    void shouldCreateChainWithPreviousHash() {
        String previousHash = "abc123def456";
        EventChain chain = new EventChain(
                previousHash,
                "currentHash789",
                "signature",
                Instant.now()
        );

        assertThat(chain.previousHash()).isEqualTo(previousHash);
    }

    @Test
    void shouldCreateChainWithHash() {
        String hash = "sha256hashvalue";
        EventChain chain = new EventChain(
                "previousHash",
                hash,
                "signature",
                Instant.now()
        );

        assertThat(chain.hash()).isEqualTo(hash);
    }

    @Test
    void shouldCreateChainWithSignature() {
        String signature = "base64encodedSignature==";
        EventChain chain = new EventChain(
                "previousHash",
                "hash",
                signature,
                Instant.now()
        );

        assertThat(chain.signature()).isEqualTo(signature);
    }

    @Test
    void shouldRecordChainedAtTimestamp() {
        Instant chainedAt = Instant.parse("2024-01-15T10:30:00Z");
        EventChain chain = new EventChain(
                "previousHash",
                "hash",
                "signature",
                chainedAt
        );

        assertThat(chain.chainedAt()).isEqualTo(chainedAt);
    }

    @Test
    void shouldHandleNullPreviousHashForGenesisEvent() {
        EventChain chain = new EventChain(
                null,
                "genesisHash",
                "signature",
                Instant.now()
        );

        assertThat(chain.previousHash()).isNull();
        assertThat(chain.hash()).isEqualTo("genesisHash");
    }

    @Test
    void shouldHandleNullSignature() {
        EventChain chain = new EventChain(
                "previousHash",
                "hash",
                null,
                Instant.now()
        );

        assertThat(chain.signature()).isNull();
    }

    @Test
    void shouldCreateChainWithAllFields() {
        Instant chainedAt = Instant.now();
        EventChain chain = new EventChain(
                "prevHash123",
                "currHash456",
                "sig789",
                chainedAt
        );

        assertThat(chain.previousHash()).isEqualTo("prevHash123");
        assertThat(chain.hash()).isEqualTo("currHash456");
        assertThat(chain.signature()).isEqualTo("sig789");
        assertThat(chain.chainedAt()).isEqualTo(chainedAt);
    }

    @Test
    void shouldSupportRecordEquality() {
        Instant timestamp = Instant.now();
        EventChain chain1 = new EventChain("prev", "hash", "sig", timestamp);
        EventChain chain2 = new EventChain("prev", "hash", "sig", timestamp);

        assertThat(chain1).isEqualTo(chain2);
        assertThat(chain1.hashCode()).isEqualTo(chain2.hashCode());
    }

    @Test
    void shouldDifferentiateNonEqualChains() {
        Instant timestamp = Instant.now();
        EventChain chain1 = new EventChain("prev", "hash1", "sig", timestamp);
        EventChain chain2 = new EventChain("prev", "hash2", "sig", timestamp);

        assertThat(chain1).isNotEqualTo(chain2);
    }

    @Test
    void shouldPreserveHashIntegrity() {
        String expectedHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        EventChain chain = new EventChain(null, expectedHash, null, Instant.now());

        assertThat(chain.hash()).isEqualTo(expectedHash);
        assertThat(chain.hash()).hasSize(64);
    }
}
