package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyScopeTest {

    @Test
    void shouldHaveTenScopes() {
        assertThat(ApiKeyScope.values()).hasSize(10);
    }

    @Test
    void shouldContainEventsWriteScope() {
        assertThat(ApiKeyScope.EVENTS_WRITE).isNotNull();
        assertThat(ApiKeyScope.EVENTS_WRITE.name()).isEqualTo("EVENTS_WRITE");
    }

    @Test
    void shouldContainEventsReadScope() {
        assertThat(ApiKeyScope.EVENTS_READ).isNotNull();
        assertThat(ApiKeyScope.EVENTS_READ.name()).isEqualTo("EVENTS_READ");
    }

    @Test
    void shouldContainSearchScope() {
        assertThat(ApiKeyScope.SEARCH).isNotNull();
        assertThat(ApiKeyScope.SEARCH.name()).isEqualTo("SEARCH");
    }

    @Test
    void shouldContainReportsReadScope() {
        assertThat(ApiKeyScope.REPORTS_READ).isNotNull();
        assertThat(ApiKeyScope.REPORTS_READ.name()).isEqualTo("REPORTS_READ");
    }

    @Test
    void shouldContainReportsWriteScope() {
        assertThat(ApiKeyScope.REPORTS_WRITE).isNotNull();
        assertThat(ApiKeyScope.REPORTS_WRITE.name()).isEqualTo("REPORTS_WRITE");
    }

    @Test
    void shouldContainRulesReadScope() {
        assertThat(ApiKeyScope.RULES_READ).isNotNull();
        assertThat(ApiKeyScope.RULES_READ.name()).isEqualTo("RULES_READ");
    }

    @Test
    void shouldContainRulesWriteScope() {
        assertThat(ApiKeyScope.RULES_WRITE).isNotNull();
        assertThat(ApiKeyScope.RULES_WRITE.name()).isEqualTo("RULES_WRITE");
    }

    @Test
    void shouldContainAlertsReadScope() {
        assertThat(ApiKeyScope.ALERTS_READ).isNotNull();
        assertThat(ApiKeyScope.ALERTS_READ.name()).isEqualTo("ALERTS_READ");
    }

    @Test
    void shouldContainAlertsWriteScope() {
        assertThat(ApiKeyScope.ALERTS_WRITE).isNotNull();
        assertThat(ApiKeyScope.ALERTS_WRITE.name()).isEqualTo("ALERTS_WRITE");
    }

    @Test
    void shouldContainAdminScope() {
        assertThat(ApiKeyScope.ADMIN).isNotNull();
        assertThat(ApiKeyScope.ADMIN.name()).isEqualTo("ADMIN");
    }

    @ParameterizedTest
    @EnumSource(ApiKeyScope.class)
    void shouldHaveValidName(ApiKeyScope scope) {
        assertThat(scope.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(ApiKeyScope.valueOf("EVENTS_WRITE")).isEqualTo(ApiKeyScope.EVENTS_WRITE);
        assertThat(ApiKeyScope.valueOf("EVENTS_READ")).isEqualTo(ApiKeyScope.EVENTS_READ);
        assertThat(ApiKeyScope.valueOf("SEARCH")).isEqualTo(ApiKeyScope.SEARCH);
        assertThat(ApiKeyScope.valueOf("ADMIN")).isEqualTo(ApiKeyScope.ADMIN);
    }
}
