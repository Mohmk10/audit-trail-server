package com.mohmk10.audittrail.reporting.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTypeTest {

    @Test
    void shouldSupportAuditType() {
        assertThat(ReportType.AUDIT).isNotNull();
        assertThat(ReportType.AUDIT.name()).isEqualTo("AUDIT");
    }

    @Test
    void shouldSupportComplianceType() {
        assertThat(ReportType.COMPLIANCE).isNotNull();
        assertThat(ReportType.COMPLIANCE.name()).isEqualTo("COMPLIANCE");
    }

    @Test
    void shouldSupportSecurityType() {
        assertThat(ReportType.SECURITY).isNotNull();
        assertThat(ReportType.SECURITY.name()).isEqualTo("SECURITY");
    }

    @Test
    void shouldSupportActivityType() {
        assertThat(ReportType.ACTIVITY).isNotNull();
        assertThat(ReportType.ACTIVITY.name()).isEqualTo("ACTIVITY");
    }

    @Test
    void shouldSupportCustomType() {
        assertThat(ReportType.CUSTOM).isNotNull();
        assertThat(ReportType.CUSTOM.name()).isEqualTo("CUSTOM");
    }

    @Test
    void shouldHaveFiveTypes() {
        assertThat(ReportType.values()).hasSize(5);
    }

    @Test
    void shouldParseFromString() {
        assertThat(ReportType.valueOf("AUDIT")).isEqualTo(ReportType.AUDIT);
        assertThat(ReportType.valueOf("COMPLIANCE")).isEqualTo(ReportType.COMPLIANCE);
        assertThat(ReportType.valueOf("SECURITY")).isEqualTo(ReportType.SECURITY);
        assertThat(ReportType.valueOf("ACTIVITY")).isEqualTo(ReportType.ACTIVITY);
        assertThat(ReportType.valueOf("CUSTOM")).isEqualTo(ReportType.CUSTOM);
    }
}
