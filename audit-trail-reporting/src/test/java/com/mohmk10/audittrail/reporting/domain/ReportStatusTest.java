package com.mohmk10.audittrail.reporting.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportStatusTest {

    @Test
    void shouldHavePendingStatus() {
        assertThat(ReportStatus.PENDING).isNotNull();
        assertThat(ReportStatus.PENDING.name()).isEqualTo("PENDING");
    }

    @Test
    void shouldHaveGeneratingStatus() {
        assertThat(ReportStatus.GENERATING).isNotNull();
        assertThat(ReportStatus.GENERATING.name()).isEqualTo("GENERATING");
    }

    @Test
    void shouldHaveCompletedStatus() {
        assertThat(ReportStatus.COMPLETED).isNotNull();
        assertThat(ReportStatus.COMPLETED.name()).isEqualTo("COMPLETED");
    }

    @Test
    void shouldHaveFailedStatus() {
        assertThat(ReportStatus.FAILED).isNotNull();
        assertThat(ReportStatus.FAILED.name()).isEqualTo("FAILED");
    }

    @Test
    void shouldHaveExpiredStatus() {
        assertThat(ReportStatus.EXPIRED).isNotNull();
        assertThat(ReportStatus.EXPIRED.name()).isEqualTo("EXPIRED");
    }

    @Test
    void shouldHaveFiveStatuses() {
        assertThat(ReportStatus.values()).hasSize(5);
    }

    @Test
    void shouldParseFromString() {
        assertThat(ReportStatus.valueOf("PENDING")).isEqualTo(ReportStatus.PENDING);
        assertThat(ReportStatus.valueOf("GENERATING")).isEqualTo(ReportStatus.GENERATING);
        assertThat(ReportStatus.valueOf("COMPLETED")).isEqualTo(ReportStatus.COMPLETED);
        assertThat(ReportStatus.valueOf("FAILED")).isEqualTo(ReportStatus.FAILED);
        assertThat(ReportStatus.valueOf("EXPIRED")).isEqualTo(ReportStatus.EXPIRED);
    }
}
