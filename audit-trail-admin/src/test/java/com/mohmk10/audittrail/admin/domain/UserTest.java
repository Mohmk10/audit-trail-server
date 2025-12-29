package com.mohmk10.audittrail.admin.domain;

import com.mohmk10.audittrail.admin.fixtures.AdminTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldBuildUserWithAllFields() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        User user = User.builder()
                .id(id)
                .tenantId("tenant-001")
                .email("user@example.com")
                .passwordHash("hashed_password")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .lastLoginAt(now)
                .lastLoginIp("192.168.1.1")
                .build();

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getTenantId()).isEqualTo("tenant-001");
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashed_password");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
        assertThat(user.getLastLoginAt()).isEqualTo(now);
        assertThat(user.getLastLoginIp()).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldCreateEmptyUser() {
        User user = new User();

        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getRole()).isNull();
    }

    @Test
    void shouldSetAndGetId() {
        User user = new User();
        UUID id = UUID.randomUUID();

        user.setId(id);

        assertThat(user.getId()).isEqualTo(id);
    }

    @Test
    void shouldSetAndGetTenantId() {
        User user = new User();

        user.setTenantId("tenant-002");

        assertThat(user.getTenantId()).isEqualTo("tenant-002");
    }

    @Test
    void shouldSetAndGetEmail() {
        User user = new User();

        user.setEmail("test@example.com");

        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldSetAndGetPasswordHash() {
        User user = new User();

        user.setPasswordHash("new_hash");

        assertThat(user.getPasswordHash()).isEqualTo("new_hash");
    }

    @Test
    void shouldSetAndGetFirstName() {
        User user = new User();

        user.setFirstName("Jane");

        assertThat(user.getFirstName()).isEqualTo("Jane");
    }

    @Test
    void shouldSetAndGetLastName() {
        User user = new User();

        user.setLastName("Smith");

        assertThat(user.getLastName()).isEqualTo("Smith");
    }

    @Test
    void shouldSetAndGetRole() {
        User user = new User();

        user.setRole(Role.AUDITOR);

        assertThat(user.getRole()).isEqualTo(Role.AUDITOR);
    }

    @Test
    void shouldSetAndGetStatus() {
        User user = new User();

        user.setStatus(UserStatus.LOCKED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
    }

    @Test
    void shouldSetAndGetCreatedAt() {
        User user = new User();
        Instant now = Instant.now();

        user.setCreatedAt(now);

        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetUpdatedAt() {
        User user = new User();
        Instant now = Instant.now();

        user.setUpdatedAt(now);

        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetLastLoginAt() {
        User user = new User();
        Instant now = Instant.now();

        user.setLastLoginAt(now);

        assertThat(user.getLastLoginAt()).isEqualTo(now);
    }

    @Test
    void shouldSetAndGetLastLoginIp() {
        User user = new User();

        user.setLastLoginIp("10.0.0.1");

        assertThat(user.getLastLoginIp()).isEqualTo("10.0.0.1");
    }

    @Test
    void shouldGetFullNameWithBothNames() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        assertThat(user.getFullName()).isEqualTo("John Doe");
    }

    @Test
    void shouldGetFullNameWithOnlyFirstName() {
        User user = User.builder()
                .firstName("John")
                .email("john@example.com")
                .build();

        assertThat(user.getFullName()).isEqualTo("John ");
    }

    @Test
    void shouldGetFullNameWithOnlyLastName() {
        User user = User.builder()
                .lastName("Doe")
                .email("john@example.com")
                .build();

        assertThat(user.getFullName()).isEqualTo(" Doe");
    }

    @Test
    void shouldGetEmailAsFullNameWhenNoBothNames() {
        User user = User.builder()
                .email("john@example.com")
                .build();

        assertThat(user.getFullName()).isEqualTo("john@example.com");
    }

    @Test
    void shouldCreateUserFromFixtures() {
        User user = AdminTestFixtures.createUser();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isNotNull();
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldCreateUserWithDifferentRoles() {
        for (Role role : Role.values()) {
            User user = AdminTestFixtures.createUserWithRole(role);

            assertThat(user.getRole()).isEqualTo(role);
        }
    }

    @Test
    void shouldCreateUserWithDifferentStatuses() {
        for (UserStatus status : UserStatus.values()) {
            User user = AdminTestFixtures.createUserWithStatus(status);

            assertThat(user.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void shouldCreateUserWithoutNames() {
        User user = AdminTestFixtures.createUserWithoutNames();

        assertThat(user.getFirstName()).isNull();
        assertThat(user.getLastName()).isNull();
        assertThat(user.getFullName()).isEqualTo(user.getEmail());
    }
}
