package com.mohmk10.audittrail.reporting.template;

import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.reporting.domain.ReportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditReportTemplateTest {

    private AuditReportTemplate template;

    @BeforeEach
    void setUp() {
        template = new AuditReportTemplate();
    }

    @Test
    void shouldReturnAuditReportType() {
        assertThat(template.getType()).isEqualTo(ReportType.AUDIT);
    }

    @Test
    void shouldReturnTemplateName() {
        assertThat(template.getName()).isEqualTo("Audit Report");
    }

    @Test
    void shouldReturnDescription() {
        String description = template.getDescription();

        assertThat(description).isNotNull();
        assertThat(description).contains("audit");
    }

    @Test
    void shouldReturnRequiredFields() {
        assertThat(template.getRequiredFields())
                .contains("tenantId", "dateRange");
    }

    @Test
    void shouldReturnOptionalFields() {
        assertThat(template.getOptionalFields())
                .contains("actorId", "resourceType", "actionType");
    }

    @Test
    void shouldProvideDefaultCriteria() {
        SearchCriteria criteria = template.getDefaultCriteria("tenant-001");

        assertThat(criteria).isNotNull();
        assertThat(criteria.tenantId()).isEqualTo("tenant-001");
        assertThat(criteria.sortBy()).isEqualTo("timestamp");
        assertThat(criteria.sortDirection()).isEqualTo(SearchCriteria.SortDirection.DESC);
    }

    @Test
    void shouldSetDefaultPageSize() {
        SearchCriteria criteria = template.getDefaultCriteria("tenant-001");

        assertThat(criteria.size()).isEqualTo(10000);
        assertThat(criteria.page()).isEqualTo(0);
    }
}
