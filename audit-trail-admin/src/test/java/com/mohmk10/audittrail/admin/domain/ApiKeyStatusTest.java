package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyStatusTest {

    @Test
    void shouldHaveThreeStatusTypes() {
        assertThat(ApiKeyStatus.values()).hasSize(3);
    }

    @Test
    void shouldContainActiveStatus() {
        assertThat(ApiKeyStatus.ACTIVE).isNotNull();
        assertThat(ApiKeyStatus.ACTIVE.name()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldContainRevokedStatus() {
        assertThat(ApiKeyStatus.REVOKED).isNotNull();
        assertThat(ApiKeyStatus.REVOKED.name()).isEqualTo("REVOKED");
    }

    @Test
    void shouldContainExpiredStatus() {
        assertThat(ApiKeyStatus.EXPIRED).isNotNull();
        assertThat(ApiKeyStatus.EXPIRED.name()).isEqualTo("EXPIRED");
    }

    @ParameterizedTest
    @EnumSource(ApiKeyStatus.class)
    void shouldHaveValidName(ApiKeyStatus status) {
        assertThat(status.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(ApiKeyStatus.valueOf("ACTIVE")).isEqualTo(ApiKeyStatus.ACTIVE);
        assertThat(ApiKeyStatus.valueOf("REVOKED")).isEqualTo(ApiKeyStatus.REVOKED);
        assertThat(ApiKeyStatus.valueOf("EXPIRED")).isEqualTo(ApiKeyStatus.EXPIRED);
    }
}
