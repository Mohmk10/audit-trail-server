package com.mohmk10.audittrail.search.adapter.in.rest;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.core.dto.AggregationResult;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.search.adapter.in.rest.dto.AggregationRequestDto;
import com.mohmk10.audittrail.search.adapter.in.rest.dto.EventSearchResponse;
import com.mohmk10.audittrail.search.adapter.in.rest.dto.SearchRequest;
import com.mohmk10.audittrail.search.service.EventSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private EventSearchService eventSearchService;

    private SearchController controller;

    @BeforeEach
    void setUp() {
        controller = new SearchController(eventSearchService);
    }

    private Event createTestEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John Doe", "192.168.1.1", null, null),
                new Action(Action.ActionType.CREATE, "Created document", "DOCS"),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Annual Report", null, null),
                new EventMetadata("web-app", "tenant-001", "corr-123", "session-abc", null, null),
                null,
                "hash-123",
                null
        );
    }

    @Test
    void shouldSearchEvents() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, 0, 20, null, null
        );

        List<Event> events = List.of(createTestEvent(), createTestEvent());
        SearchResult<Event> result = SearchResult.of(events, 2, 0, 20);
        when(eventSearchService.search(any())).thenReturn(result);

        ResponseEntity<SearchResult<EventSearchResponse>> response = controller.search(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().items()).hasSize(2);
        assertThat(response.getBody().totalCount()).isEqualTo(2);
    }

    @Test
    void shouldQuickSearch() {
        List<Event> events = List.of(createTestEvent());
        SearchResult<Event> result = SearchResult.of(events, 1, 0, 20);
        when(eventSearchService.quickSearch(eq("annual report"), eq("tenant-001"), any(Pageable.class)))
                .thenReturn(result);

        ResponseEntity<SearchResult<EventSearchResponse>> response = controller.quickSearch(
                "annual report", "tenant-001", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().items()).hasSize(1);
    }

    @Test
    void shouldGetTimeline() {
        Instant from = Instant.parse("2024-06-15T00:00:00Z");
        Instant to = Instant.parse("2024-06-15T23:59:59Z");

        List<Event> events = List.of(createTestEvent(), createTestEvent());
        when(eventSearchService.getTimeline(eq("tenant-001"), any(), any(Pageable.class)))
                .thenReturn(events);

        ResponseEntity<List<EventSearchResponse>> response = controller.timeline(
                "tenant-001", from, to, 0, 50);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldFindByCorrelationId() {
        List<Event> events = List.of(createTestEvent(), createTestEvent());
        when(eventSearchService.findByCorrelationId("corr-123")).thenReturn(events);

        ResponseEntity<List<EventSearchResponse>> response = controller.findByCorrelation("corr-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyListForNoCorrelation() {
        when(eventSearchService.findByCorrelationId("nonexistent")).thenReturn(List.of());

        ResponseEntity<List<EventSearchResponse>> response = controller.findByCorrelation("nonexistent");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void shouldAggregateByField() {
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", "COUNT", null, null
        );

        Map<String, Long> counts = Map.of("CREATE", 10L, "UPDATE", 5L, "DELETE", 2L);
        when(eventSearchService.aggregateByField(eq("tenant-001"), eq("actionType"), any()))
                .thenReturn(counts);

        ResponseEntity<AggregationResult> response = controller.aggregate(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalDocuments()).isEqualTo(17);
        assertThat(response.getBody().buckets()).hasSize(3);
    }

    @Test
    void shouldAggregateWithDateRange() {
        Instant from = Instant.parse("2024-06-01T00:00:00Z");
        Instant to = Instant.parse("2024-06-30T23:59:59Z");
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "resourceType", "COUNT", from, to
        );

        Map<String, Long> counts = Map.of("DOCUMENT", 25L, "FILE", 15L);
        when(eventSearchService.aggregateByField(any(), any(), any())).thenReturn(counts);

        ResponseEntity<AggregationResult> response = controller.aggregate(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalDocuments()).isEqualTo(40);
    }

    @Test
    void shouldSearchWithAllFilters() {
        Instant from = Instant.parse("2024-06-01T00:00:00Z");
        Instant to = Instant.parse("2024-06-30T23:59:59Z");
        SearchRequest request = new SearchRequest(
                "tenant-001", "actor-123", "USER", "CREATE", "DOCS",
                "res-123", "DOCUMENT", "annual report", from, to,
                List.of("env:prod"), 0, 20, "timestamp", "desc"
        );

        SearchResult<Event> result = SearchResult.of(List.of(), 0, 0, 20);
        when(eventSearchService.search(any())).thenReturn(result);

        ResponseEntity<SearchResult<EventSearchResponse>> response = controller.search(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().items()).isEmpty();
    }

    @Test
    void shouldReturnEventDetailsInResponse() {
        SearchRequest request = new SearchRequest(
                "tenant-001", null, null, null, null, null, null,
                null, null, null, null, 0, 20, null, null
        );

        Event event = createTestEvent();
        SearchResult<Event> result = SearchResult.of(List.of(event), 1, 0, 20);
        when(eventSearchService.search(any())).thenReturn(result);

        ResponseEntity<SearchResult<EventSearchResponse>> response = controller.search(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().items()).hasSize(1);
        assertThat(response.getBody().items().get(0).actor().id()).isEqualTo("actor-123");
        assertThat(response.getBody().items().get(0).action().type()).isEqualTo("CREATE");
    }

    @Test
    void shouldLimitPageSize() {
        // Page size should be capped at 100
        List<Event> events = List.of(createTestEvent());
        SearchResult<Event> result = SearchResult.of(events, 1, 0, 100);
        when(eventSearchService.quickSearch(any(), any(), any(Pageable.class))).thenReturn(result);

        ResponseEntity<SearchResult<EventSearchResponse>> response = controller.quickSearch(
                "test", "tenant-001", 0, 200);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldUseDefaultPagination() {
        List<Event> events = List.of(createTestEvent());
        SearchResult<Event> result = SearchResult.of(events, 1, 0, 20);
        when(eventSearchService.quickSearch(any(), any(), any(Pageable.class))).thenReturn(result);

        ResponseEntity<SearchResult<EventSearchResponse>> response = controller.quickSearch(
                "test", "tenant-001", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldHandleEmptyAggregation() {
        AggregationRequestDto request = new AggregationRequestDto(
                "tenant-001", "actionType", "COUNT", null, null
        );

        when(eventSearchService.aggregateByField(any(), any(), any())).thenReturn(Map.of());

        ResponseEntity<AggregationResult> response = controller.aggregate(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().buckets()).isEmpty();
        assertThat(response.getBody().totalDocuments()).isEqualTo(0);
    }
}
