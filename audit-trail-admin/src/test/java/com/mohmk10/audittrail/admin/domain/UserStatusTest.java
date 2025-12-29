package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class UserStatusTest {

    @Test
    void shouldHaveFourStatusTypes() {
        assertThat(UserStatus.values()).hasSize(4);
    }

    @Test
    void shouldContainActiveStatus() {
        assertThat(UserStatus.ACTIVE).isNotNull();
        assertThat(UserStatus.ACTIVE.name()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldContainInactiveStatus() {
        assertThat(UserStatus.INACTIVE).isNotNull();
        assertThat(UserStatus.INACTIVE.name()).isEqualTo("INACTIVE");
    }

    @Test
    void shouldContainLockedStatus() {
        assertThat(UserStatus.LOCKED).isNotNull();
        assertThat(UserStatus.LOCKED.name()).isEqualTo("LOCKED");
    }

    @Test
    void shouldContainPendingVerificationStatus() {
        assertThat(UserStatus.PENDING_VERIFICATION).isNotNull();
        assertThat(UserStatus.PENDING_VERIFICATION.name()).isEqualTo("PENDING_VERIFICATION");
    }

    @ParameterizedTest
    @EnumSource(UserStatus.class)
    void shouldHaveValidName(UserStatus status) {
        assertThat(status.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(UserStatus.valueOf("ACTIVE")).isEqualTo(UserStatus.ACTIVE);
        assertThat(UserStatus.valueOf("INACTIVE")).isEqualTo(UserStatus.INACTIVE);
        assertThat(UserStatus.valueOf("LOCKED")).isEqualTo(UserStatus.LOCKED);
        assertThat(UserStatus.valueOf("PENDING_VERIFICATION")).isEqualTo(UserStatus.PENDING_VERIFICATION);
    }
}
