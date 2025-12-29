package com.mohmk10.audittrail.reporting.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportFormatTest {

    @Test
    void shouldSupportPdfFormat() {
        assertThat(ReportFormat.PDF).isNotNull();
        assertThat(ReportFormat.PDF.name()).isEqualTo("PDF");
    }

    @Test
    void shouldSupportCsvFormat() {
        assertThat(ReportFormat.CSV).isNotNull();
        assertThat(ReportFormat.CSV.name()).isEqualTo("CSV");
    }

    @Test
    void shouldSupportXlsxFormat() {
        assertThat(ReportFormat.XLSX).isNotNull();
        assertThat(ReportFormat.XLSX.name()).isEqualTo("XLSX");
    }

    @Test
    void shouldSupportJsonFormat() {
        assertThat(ReportFormat.JSON).isNotNull();
        assertThat(ReportFormat.JSON.name()).isEqualTo("JSON");
    }

    @Test
    void shouldHaveFourFormats() {
        assertThat(ReportFormat.values()).hasSize(4);
    }

    @Test
    void shouldParseFromString() {
        assertThat(ReportFormat.valueOf("PDF")).isEqualTo(ReportFormat.PDF);
        assertThat(ReportFormat.valueOf("CSV")).isEqualTo(ReportFormat.CSV);
        assertThat(ReportFormat.valueOf("XLSX")).isEqualTo(ReportFormat.XLSX);
        assertThat(ReportFormat.valueOf("JSON")).isEqualTo(ReportFormat.JSON);
    }
}
