package com.mohmk10.audittrail.search.service;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QueryBuilderServiceTest {

    private QueryBuilderService queryBuilderService;

    @BeforeEach
    void setUp() {
        queryBuilderService = new QueryBuilderService();
    }

    @Test
    void shouldBuildQueryWithTenantId() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
        assertThat(query.getQuery()).isNotNull();
    }

    @Test
    void shouldBuildQueryWithActorId() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .actorId("actor-123")
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQueryWithActionTypes() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .actionTypes(List.of(Action.ActionType.CREATE, Action.ActionType.UPDATE))
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQueryWithResourceTypes() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .resourceTypes(List.of(Resource.ResourceType.DOCUMENT, Resource.ResourceType.FILE))
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQueryWithDateRange() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        DateRange dateRange = new DateRange(from, to);

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .dateRange(dateRange)
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQueryWithCorrelationId() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .correlationId("corr-123")
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQueryWithSessionId() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .sessionId("session-abc")
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQueryWithFullTextSearch() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .query("annual report")
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQueryWithTags() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .tags(Map.of("env", "prod", "region", "us-east-1"))
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQueryWithAllFilters() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .actorId("actor-123")
                .actionTypes(List.of(Action.ActionType.CREATE))
                .resourceTypes(List.of(Resource.ResourceType.DOCUMENT))
                .dateRange(new DateRange(from, to))
                .correlationId("corr-123")
                .sessionId("session-abc")
                .query("annual report")
                .tags(Map.of("env", "prod"))
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQuickSearchQuery() {
        NativeQuery query = queryBuilderService.buildQuickSearchQuery("annual report", "tenant-001");

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQuickSearchQueryWithNullQuery() {
        NativeQuery query = queryBuilderService.buildQuickSearchQuery(null, "tenant-001");

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildQuickSearchQueryWithBlankQuery() {
        NativeQuery query = queryBuilderService.buildQuickSearchQuery("  ", "tenant-001");

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildCorrelationQuery() {
        NativeQuery query = queryBuilderService.buildCorrelationQuery("corr-123");

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildTimelineQuery() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        DateRange range = new DateRange(from, to);

        NativeQuery query = queryBuilderService.buildTimelineQuery("tenant-001", range);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldBuildTimelineQueryWithNullRange() {
        NativeQuery query = queryBuilderService.buildTimelineQuery("tenant-001", null);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldHandleNullTenantId() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId(null)
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldHandleBlankTenantId() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("  ")
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldHandleEmptyActionTypes() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .actionTypes(List.of())
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldHandleEmptyResourceTypes() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .resourceTypes(List.of())
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldHandleEmptyTags() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .tags(Map.of())
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldHandleDateRangeWithOnlyFrom() {
        Instant from = Instant.now().minusSeconds(3600);
        DateRange dateRange = new DateRange(from, null);

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .dateRange(dateRange)
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }

    @Test
    void shouldHandleDateRangeWithOnlyTo() {
        Instant to = Instant.now();
        DateRange dateRange = new DateRange(null, to);

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .dateRange(dateRange)
                .page(0)
                .size(20)
                .build();

        NativeQuery query = queryBuilderService.buildQuery(criteria);

        assertThat(query).isNotNull();
    }
}
