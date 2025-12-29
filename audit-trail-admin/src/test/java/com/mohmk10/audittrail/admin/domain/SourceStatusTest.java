package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class SourceStatusTest {

    @Test
    void shouldHaveThreeStatusTypes() {
        assertThat(SourceStatus.values()).hasSize(3);
    }

    @Test
    void shouldContainActiveStatus() {
        assertThat(SourceStatus.ACTIVE).isNotNull();
        assertThat(SourceStatus.ACTIVE.name()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldContainInactiveStatus() {
        assertThat(SourceStatus.INACTIVE).isNotNull();
        assertThat(SourceStatus.INACTIVE.name()).isEqualTo("INACTIVE");
    }

    @Test
    void shouldContainSuspendedStatus() {
        assertThat(SourceStatus.SUSPENDED).isNotNull();
        assertThat(SourceStatus.SUSPENDED.name()).isEqualTo("SUSPENDED");
    }

    @ParameterizedTest
    @EnumSource(SourceStatus.class)
    void shouldHaveValidName(SourceStatus status) {
        assertThat(status.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(SourceStatus.valueOf("ACTIVE")).isEqualTo(SourceStatus.ACTIVE);
        assertThat(SourceStatus.valueOf("INACTIVE")).isEqualTo(SourceStatus.INACTIVE);
        assertThat(SourceStatus.valueOf("SUSPENDED")).isEqualTo(SourceStatus.SUSPENDED);
    }
}
