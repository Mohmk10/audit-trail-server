package com.mohmk10.audittrail.detection.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleTypeTest {

    @Test
    void shouldSupportThreshold() {
        assertThat(RuleType.THRESHOLD).isNotNull();
        assertThat(RuleType.THRESHOLD.name()).isEqualTo("THRESHOLD");
    }

    @Test
    void shouldSupportPattern() {
        assertThat(RuleType.PATTERN).isNotNull();
        assertThat(RuleType.PATTERN.name()).isEqualTo("PATTERN");
    }

    @Test
    void shouldSupportAnomaly() {
        assertThat(RuleType.ANOMALY).isNotNull();
        assertThat(RuleType.ANOMALY.name()).isEqualTo("ANOMALY");
    }

    @Test
    void shouldSupportBlacklist() {
        assertThat(RuleType.BLACKLIST).isNotNull();
        assertThat(RuleType.BLACKLIST.name()).isEqualTo("BLACKLIST");
    }

    @Test
    void shouldSupportTimeBased() {
        assertThat(RuleType.TIME_BASED).isNotNull();
        assertThat(RuleType.TIME_BASED.name()).isEqualTo("TIME_BASED");
    }

    @Test
    void shouldHaveFiveTypes() {
        assertThat(RuleType.values()).hasSize(5);
    }

    @Test
    void shouldParseFromString() {
        assertThat(RuleType.valueOf("THRESHOLD")).isEqualTo(RuleType.THRESHOLD);
        assertThat(RuleType.valueOf("PATTERN")).isEqualTo(RuleType.PATTERN);
        assertThat(RuleType.valueOf("ANOMALY")).isEqualTo(RuleType.ANOMALY);
        assertThat(RuleType.valueOf("BLACKLIST")).isEqualTo(RuleType.BLACKLIST);
        assertThat(RuleType.valueOf("TIME_BASED")).isEqualTo(RuleType.TIME_BASED);
    }
}
