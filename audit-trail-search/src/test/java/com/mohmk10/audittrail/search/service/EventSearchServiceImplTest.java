package com.mohmk10.audittrail.search.service;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.document.EventDocument;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.mapper.EventDocumentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.TotalHitsRelation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventSearchServiceImplTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private QueryBuilderService queryBuilderService;

    @Mock
    private EventDocumentMapper mapper;

    private EventSearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        searchService = new EventSearchServiceImpl(elasticsearchOperations, queryBuilderService, mapper);
    }

    private Event createTestEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, "Created", null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null,
                "hash-123",
                null
        );
    }

    private EventDocument createTestDocument() {
        EventDocument doc = new EventDocument();
        doc.setId(UUID.randomUUID().toString());
        doc.setTimestamp(Instant.now());
        doc.setActorId("actor-123");
        doc.setActionType("CREATE");
        doc.setResourceId("res-123");
        doc.setTenantId("tenant-001");
        return doc;
    }

    @SuppressWarnings("unchecked")
    private SearchHits<EventDocument> createMockSearchHits(List<EventDocument> documents, long totalHits) {
        SearchHits<EventDocument> searchHits = mock(SearchHits.class);
        List<SearchHit<EventDocument>> hits = documents.stream()
                .map(doc -> {
                    SearchHit<EventDocument> hit = mock(SearchHit.class);
                    when(hit.getContent()).thenReturn(doc);
                    return hit;
                })
                .toList();
        when(searchHits.getSearchHits()).thenReturn(hits);
        when(searchHits.getTotalHits()).thenReturn(totalHits);
        when(searchHits.hasAggregations()).thenReturn(false);
        return searchHits;
    }

    @Test
    void shouldSearchWithCriteria() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .page(0)
                .size(20)
                .build();

        Query mockQuery = mock(Query.class);
        NativeQuery baseQuery = mock(NativeQuery.class);
        when(baseQuery.getQuery()).thenReturn(mockQuery);
        when(queryBuilderService.buildQuery(criteria)).thenReturn(baseQuery);

        List<EventDocument> documents = List.of(createTestDocument(), createTestDocument());
        SearchHits<EventDocument> searchHits = createMockSearchHits(documents, 2);
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(EventDocument.class))).thenReturn(searchHits);

        Event event = createTestEvent();
        when(mapper.toDomain(any())).thenReturn(event);

        SearchResult<Event> result = searchService.search(criteria);

        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(2);
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(20);
    }

    @Test
    void shouldSearchWithDefaultSort() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .page(0)
                .size(20)
                .sortBy(null)
                .sortDirection(null)
                .build();

        Query mockQuery = mock(Query.class);
        NativeQuery baseQuery = mock(NativeQuery.class);
        when(baseQuery.getQuery()).thenReturn(mockQuery);
        when(queryBuilderService.buildQuery(criteria)).thenReturn(baseQuery);

        SearchHits<EventDocument> searchHits = createMockSearchHits(List.of(), 0);
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(EventDocument.class))).thenReturn(searchHits);

        SearchResult<Event> result = searchService.search(criteria);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldSearchWithAscSort() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .page(0)
                .size(20)
                .sortBy("timestamp")
                .sortDirection(SearchCriteria.SortDirection.ASC)
                .build();

        Query mockQuery = mock(Query.class);
        NativeQuery baseQuery = mock(NativeQuery.class);
        when(baseQuery.getQuery()).thenReturn(mockQuery);
        when(queryBuilderService.buildQuery(criteria)).thenReturn(baseQuery);

        SearchHits<EventDocument> searchHits = createMockSearchHits(List.of(), 0);
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(EventDocument.class))).thenReturn(searchHits);

        SearchResult<Event> result = searchService.search(criteria);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldQuickSearch() {
        Pageable pageable = PageRequest.of(0, 20);
        String query = "annual report";
        String tenantId = "tenant-001";

        Query mockQuery = mock(Query.class);
        NativeQuery baseQuery = mock(NativeQuery.class);
        when(baseQuery.getQuery()).thenReturn(mockQuery);
        when(queryBuilderService.buildQuickSearchQuery(query, tenantId)).thenReturn(baseQuery);

        List<EventDocument> documents = List.of(createTestDocument());
        SearchHits<EventDocument> searchHits = createMockSearchHits(documents, 1);
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(EventDocument.class))).thenReturn(searchHits);

        Event event = createTestEvent();
        when(mapper.toDomain(any())).thenReturn(event);

        SearchResult<Event> result = searchService.quickSearch(query, tenantId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    void shouldFindByCorrelationId() {
        String correlationId = "corr-123";

        Query mockQuery = mock(Query.class);
        NativeQuery baseQuery = mock(NativeQuery.class);
        when(baseQuery.getQuery()).thenReturn(mockQuery);
        when(queryBuilderService.buildCorrelationQuery(correlationId)).thenReturn(baseQuery);

        List<EventDocument> documents = List.of(createTestDocument(), createTestDocument());
        SearchHits<EventDocument> searchHits = createMockSearchHits(documents, 2);
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(EventDocument.class))).thenReturn(searchHits);

        Event event = createTestEvent();
        when(mapper.toDomain(any())).thenReturn(event);

        List<Event> result = searchService.findByCorrelationId(correlationId);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldGetTimeline() {
        String tenantId = "tenant-001";
        DateRange range = new DateRange(Instant.now().minusSeconds(3600), Instant.now());
        Pageable pageable = PageRequest.of(0, 50);

        Query mockQuery = mock(Query.class);
        NativeQuery baseQuery = mock(NativeQuery.class);
        when(baseQuery.getQuery()).thenReturn(mockQuery);
        when(queryBuilderService.buildTimelineQuery(tenantId, range)).thenReturn(baseQuery);

        List<EventDocument> documents = List.of(createTestDocument(), createTestDocument(), createTestDocument());
        SearchHits<EventDocument> searchHits = createMockSearchHits(documents, 3);
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(EventDocument.class))).thenReturn(searchHits);

        Event event = createTestEvent();
        when(mapper.toDomain(any())).thenReturn(event);

        List<Event> result = searchService.getTimeline(tenantId, range, pageable);

        assertThat(result).hasSize(3);
    }

    @Test
    void shouldReturnEmptyListWhenNoResults() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .page(0)
                .size(20)
                .build();

        Query mockQuery = mock(Query.class);
        NativeQuery baseQuery = mock(NativeQuery.class);
        when(baseQuery.getQuery()).thenReturn(mockQuery);
        when(queryBuilderService.buildQuery(criteria)).thenReturn(baseQuery);

        SearchHits<EventDocument> searchHits = createMockSearchHits(List.of(), 0);
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(EventDocument.class))).thenReturn(searchHits);

        SearchResult<Event> result = searchService.search(criteria);

        assertThat(result.items()).isEmpty();
        assertThat(result.totalCount()).isEqualTo(0);
    }

    @Test
    void shouldHandlePagination() {
        SearchCriteria criteria = SearchCriteria.builder()
                .tenantId("tenant-001")
                .page(2)
                .size(10)
                .build();

        Query mockQuery = mock(Query.class);
        NativeQuery baseQuery = mock(NativeQuery.class);
        when(baseQuery.getQuery()).thenReturn(mockQuery);
        when(queryBuilderService.buildQuery(criteria)).thenReturn(baseQuery);

        List<EventDocument> documents = List.of(createTestDocument());
        SearchHits<EventDocument> searchHits = createMockSearchHits(documents, 25);
        when(elasticsearchOperations.search(any(NativeQuery.class), eq(EventDocument.class))).thenReturn(searchHits);

        Event event = createTestEvent();
        when(mapper.toDomain(any())).thenReturn(event);

        SearchResult<Event> result = searchService.search(criteria);

        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalCount()).isEqualTo(25);
    }
}
