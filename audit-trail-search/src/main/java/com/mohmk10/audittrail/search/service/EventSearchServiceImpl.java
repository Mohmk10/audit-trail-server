package com.mohmk10.audittrail.search.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.document.EventDocument;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.mapper.EventDocumentMapper;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.json.JsonData;

@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class EventSearchServiceImpl implements EventSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final QueryBuilderService queryBuilderService;
    private final EventDocumentMapper mapper;

    public EventSearchServiceImpl(
            ElasticsearchOperations elasticsearchOperations,
            QueryBuilderService queryBuilderService,
            EventDocumentMapper mapper) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.queryBuilderService = queryBuilderService;
        this.mapper = mapper;
    }

    @Override
    public SearchResult<Event> search(SearchCriteria criteria) {
        NativeQuery baseQuery = queryBuilderService.buildQuery(criteria);

        Sort sort = buildSort(criteria.sortBy(), criteria.sortDirection());
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(baseQuery.getQuery())
                .withPageable(pageable)
                .build();

        SearchHits<EventDocument> searchHits = elasticsearchOperations.search(nativeQuery, EventDocument.class);

        List<Event> events = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(mapper::toDomain)
                .toList();

        return SearchResult.of(events, searchHits.getTotalHits(), criteria.page(), criteria.size());
    }

    @Override
    public SearchResult<Event> quickSearch(String query, String tenantId, Pageable pageable) {
        NativeQuery baseQuery = queryBuilderService.buildQuickSearchQuery(query, tenantId);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(baseQuery.getQuery())
                .withPageable(pageable)
                .build();

        SearchHits<EventDocument> searchHits = elasticsearchOperations.search(nativeQuery, EventDocument.class);

        List<Event> events = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(mapper::toDomain)
                .toList();

        return SearchResult.of(events, searchHits.getTotalHits(), pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    public List<Event> findByCorrelationId(String correlationId) {
        NativeQuery baseQuery = queryBuilderService.buildCorrelationQuery(correlationId);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(baseQuery.getQuery())
                .withSort(Sort.by(Sort.Direction.ASC, "timestamp"))
                .build();

        SearchHits<EventDocument> searchHits = elasticsearchOperations.search(nativeQuery, EventDocument.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Event> getTimeline(String tenantId, DateRange range, Pageable pageable) {
        NativeQuery baseQuery = queryBuilderService.buildTimelineQuery(tenantId, range);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(baseQuery.getQuery())
                .withPageable(pageable)
                .withSort(Sort.by(Sort.Direction.DESC, "timestamp"))
                .build();

        SearchHits<EventDocument> searchHits = elasticsearchOperations.search(nativeQuery, EventDocument.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Map<String, Long> aggregateByField(String tenantId, String field, DateRange range) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    b.filter(f -> f.term(t -> t.field("tenantId").value(tenantId)));
                    if (range != null) {
                        b.filter(f -> f.range(r -> r.untyped(u -> {
                            u.field("timestamp");
                            if (range.from() != null) {
                                u.gte(JsonData.of(range.from().toString()));
                            }
                            if (range.to() != null) {
                                u.lte(JsonData.of(range.to().toString()));
                            }
                            return u;
                        })));
                    }
                    return b;
                }))
                .withAggregation("field_agg", Aggregation.of(a -> a.terms(t -> t.field(field).size(100))))
                .withMaxResults(0)
                .build();

        SearchHits<EventDocument> searchHits = elasticsearchOperations.search(query, EventDocument.class);

        Map<String, Long> result = new HashMap<>();

        if (searchHits.hasAggregations()) {
            ElasticsearchAggregations aggs = (ElasticsearchAggregations) searchHits.getAggregations();
            if (aggs != null) {
                var termsAgg = aggs.aggregations().get(0);
                if (termsAgg != null) {
                    var buckets = termsAgg.aggregation().getAggregate().sterms().buckets().array();
                    for (StringTermsBucket bucket : buckets) {
                        result.put(bucket.key().stringValue(), bucket.docCount());
                    }
                }
            }
        }

        return result;
    }

    private Sort buildSort(String sortBy, SearchCriteria.SortDirection sortDirection) {
        String field = sortBy != null ? sortBy : "timestamp";
        Sort.Direction direction = sortDirection == SearchCriteria.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}
