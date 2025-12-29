package com.mohmk10.audittrail.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AdminActionTest {

    @Test
    void shouldHaveUserActions() {
        List<AdminAction> userActions = Arrays.stream(AdminAction.values())
                .filter(a -> a.name().startsWith("USER_"))
                .collect(Collectors.toList());

        assertThat(userActions).contains(
                AdminAction.USER_CREATED,
                AdminAction.USER_UPDATED,
                AdminAction.USER_DELETED,
                AdminAction.USER_ACTIVATED,
                AdminAction.USER_DEACTIVATED,
                AdminAction.USER_LOCKED,
                AdminAction.USER_UNLOCKED,
                AdminAction.USER_PASSWORD_CHANGED,
                AdminAction.USER_ROLE_CHANGED,
                AdminAction.USER_LOGIN,
                AdminAction.USER_LOGIN_FAILED,
                AdminAction.USER_LOGOUT
        );
    }

    @Test
    void shouldHaveTenantActions() {
        List<AdminAction> tenantActions = Arrays.stream(AdminAction.values())
                .filter(a -> a.name().startsWith("TENANT_"))
                .collect(Collectors.toList());

        assertThat(tenantActions).contains(
                AdminAction.TENANT_CREATED,
                AdminAction.TENANT_UPDATED,
                AdminAction.TENANT_SUSPENDED,
                AdminAction.TENANT_ACTIVATED,
                AdminAction.TENANT_PLAN_CHANGED,
                AdminAction.TENANT_SETTINGS_CHANGED
        );
    }

    @Test
    void shouldHaveSourceActions() {
        List<AdminAction> sourceActions = Arrays.stream(AdminAction.values())
                .filter(a -> a.name().startsWith("SOURCE_"))
                .collect(Collectors.toList());

        assertThat(sourceActions).contains(
                AdminAction.SOURCE_CREATED,
                AdminAction.SOURCE_UPDATED,
                AdminAction.SOURCE_DELETED,
                AdminAction.SOURCE_ACTIVATED,
                AdminAction.SOURCE_DEACTIVATED
        );
    }

    @Test
    void shouldHaveApiKeyActions() {
        List<AdminAction> apiKeyActions = Arrays.stream(AdminAction.values())
                .filter(a -> a.name().startsWith("API_KEY_"))
                .collect(Collectors.toList());

        assertThat(apiKeyActions).contains(
                AdminAction.API_KEY_CREATED,
                AdminAction.API_KEY_REVOKED,
                AdminAction.API_KEY_ROTATED
        );
    }

    @Test
    void shouldHaveRuleActions() {
        List<AdminAction> ruleActions = Arrays.stream(AdminAction.values())
                .filter(a -> a.name().startsWith("RULE_"))
                .collect(Collectors.toList());

        assertThat(ruleActions).contains(
                AdminAction.RULE_CREATED,
                AdminAction.RULE_UPDATED,
                AdminAction.RULE_DELETED,
                AdminAction.RULE_ENABLED,
                AdminAction.RULE_DISABLED
        );
    }

    @Test
    void shouldHaveReportActions() {
        List<AdminAction> reportActions = Arrays.stream(AdminAction.values())
                .filter(a -> a.name().startsWith("REPORT_"))
                .collect(Collectors.toList());

        assertThat(reportActions).contains(
                AdminAction.REPORT_GENERATED,
                AdminAction.REPORT_SCHEDULED,
                AdminAction.REPORT_EXPORTED
        );
    }

    @Test
    void shouldHaveSystemActions() {
        assertThat(AdminAction.SYSTEM_SETTINGS_CHANGED).isNotNull();
        assertThat(AdminAction.MAINTENANCE_MODE_ENABLED).isNotNull();
        assertThat(AdminAction.MAINTENANCE_MODE_DISABLED).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(AdminAction.class)
    void shouldHaveValidName(AdminAction action) {
        assertThat(action.name()).isNotBlank();
    }

    @Test
    void shouldParseFromString() {
        assertThat(AdminAction.valueOf("USER_CREATED")).isEqualTo(AdminAction.USER_CREATED);
        assertThat(AdminAction.valueOf("TENANT_CREATED")).isEqualTo(AdminAction.TENANT_CREATED);
        assertThat(AdminAction.valueOf("API_KEY_CREATED")).isEqualTo(AdminAction.API_KEY_CREATED);
    }
}
