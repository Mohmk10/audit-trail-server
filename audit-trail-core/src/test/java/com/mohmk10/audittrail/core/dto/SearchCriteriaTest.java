package com.mohmk10.audittrail.core.dto;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Resource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SearchCriteriaTest {

    @Test
    void shouldCreateWithTenantId() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .build();

        assertThat(criteria.tenantId()).isEqualTo("tenant-001");
    }

    @Test
    void shouldHandleDateRange() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");
        DateRange dateRange = new DateRange(from, to);

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .dateRange(dateRange)
                .build();

        assertThat(criteria.dateRange()).isNotNull();
        assertThat(criteria.dateRange().from()).isEqualTo(from);
        assertThat(criteria.dateRange().to()).isEqualTo(to);
    }

    @Test
    void shouldHandleActorFilters() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .actorId("user-123")
                .build();

        assertThat(criteria.actorId()).isEqualTo("user-123");
    }

    @Test
    void shouldHandleActionFilters() {
        List<Action.ActionType> actionTypes = List.of(
                Action.ActionType.CREATE,
                Action.ActionType.UPDATE,
                Action.ActionType.DELETE
        );

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .actionTypes(actionTypes)
                .build();

        assertThat(criteria.actionTypes()).hasSize(3);
        assertThat(criteria.actionTypes()).contains(Action.ActionType.CREATE);
        assertThat(criteria.actionTypes()).contains(Action.ActionType.UPDATE);
        assertThat(criteria.actionTypes()).contains(Action.ActionType.DELETE);
    }

    @Test
    void shouldHandleResourceFilters() {
        List<Resource.ResourceType> resourceTypes = List.of(
                Resource.ResourceType.DOCUMENT,
                Resource.ResourceType.FILE
        );

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .resourceTypes(resourceTypes)
                .build();

        assertThat(criteria.resourceTypes()).hasSize(2);
        assertThat(criteria.resourceTypes()).contains(Resource.ResourceType.DOCUMENT);
        assertThat(criteria.resourceTypes()).contains(Resource.ResourceType.FILE);
    }

    @Test
    void shouldHandlePagination() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .page(2)
                .size(50)
                .build();

        assertThat(criteria.page()).isEqualTo(2);
        assertThat(criteria.size()).isEqualTo(50);
    }

    @Test
    void shouldHaveDefaultPagination() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .build();

        assertThat(criteria.page()).isEqualTo(0);
        assertThat(criteria.size()).isEqualTo(20);
    }

    @Test
    void shouldHaveDefaultSorting() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .build();

        assertThat(criteria.sortBy()).isEqualTo("timestamp");
        assertThat(criteria.sortDirection()).isEqualTo(SearchCriteria.SortDirection.DESC);
    }

    @Test
    void shouldHandleCustomSorting() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .sortBy("actorId")
                .sortDirection(SearchCriteria.SortDirection.ASC)
                .build();

        assertThat(criteria.sortBy()).isEqualTo("actorId");
        assertThat(criteria.sortDirection()).isEqualTo(SearchCriteria.SortDirection.ASC);
    }

    @Test
    void shouldHandleCorrelationId() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .correlationId("corr-abc-123")
                .build();

        assertThat(criteria.correlationId()).isEqualTo("corr-abc-123");
    }

    @Test
    void shouldHandleSessionId() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .sessionId("session-xyz-789")
                .build();

        assertThat(criteria.sessionId()).isEqualTo("session-xyz-789");
    }

    @Test
    void shouldHandleTags() {
        Map<String, String> tags = Map.of("env", "production", "region", "us-east");

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .tags(tags)
                .build();

        assertThat(criteria.tags()).hasSize(2);
        assertThat(criteria.tags()).containsEntry("env", "production");
    }

    @Test
    void shouldHandleQuery() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .query("document created")
                .build();

        assertThat(criteria.query()).isEqualTo("document created");
    }

    @Test
    void shouldBuildWithAllFields() {
        DateRange dateRange = new DateRange(Instant.now().minusSeconds(3600), Instant.now());

        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .actorId("user-123")
                .actionTypes(List.of(Action.ActionType.CREATE))
                .resourceTypes(List.of(Resource.ResourceType.DOCUMENT))
                .dateRange(dateRange)
                .correlationId("corr-123")
                .sessionId("session-456")
                .tags(Map.of("key", "value"))
                .query("search query")
                .page(1)
                .size(25)
                .sortBy("timestamp")
                .sortDirection(SearchCriteria.SortDirection.DESC)
                .build();

        assertThat(criteria.tenantId()).isEqualTo("tenant-001");
        assertThat(criteria.actorId()).isEqualTo("user-123");
        assertThat(criteria.actionTypes()).hasSize(1);
        assertThat(criteria.resourceTypes()).hasSize(1);
        assertThat(criteria.dateRange()).isNotNull();
        assertThat(criteria.correlationId()).isEqualTo("corr-123");
        assertThat(criteria.sessionId()).isEqualTo("session-456");
        assertThat(criteria.tags()).hasSize(1);
        assertThat(criteria.query()).isEqualTo("search query");
        assertThat(criteria.page()).isEqualTo(1);
        assertThat(criteria.size()).isEqualTo(25);
    }

    @Test
    void shouldSupportSortDirectionEnum() {
        assertThat(SearchCriteria.SortDirection.ASC).isNotNull();
        assertThat(SearchCriteria.SortDirection.DESC).isNotNull();
        assertThat(SearchCriteria.SortDirection.values()).hasSize(2);
    }

    @Test
    void builderShouldReturnSelfForChaining() {
        SearchCriteria.Builder builder = SearchCriteria.builder();

        assertThat(builder.tenantId("t")).isEqualTo(builder);
        assertThat(builder.actorId("a")).isEqualTo(builder);
        assertThat(builder.page(1)).isEqualTo(builder);
        assertThat(builder.size(10)).isEqualTo(builder);
    }
}
