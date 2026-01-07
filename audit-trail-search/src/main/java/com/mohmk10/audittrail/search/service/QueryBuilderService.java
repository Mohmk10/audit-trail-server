package com.mohmk10.audittrail.search.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.core.dto.DateRange;
import com.mohmk10.audittrail.core.dto.SearchCriteria;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.json.JsonData;

@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class QueryBuilderService {

    public NativeQuery buildQuery(SearchCriteria criteria) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        addTenantFilter(boolQueryBuilder, criteria.tenantId());
        addActorFilter(boolQueryBuilder, criteria.actorId());
        addActionTypesFilter(boolQueryBuilder, criteria.actionTypes());
        addResourceTypesFilter(boolQueryBuilder, criteria.resourceTypes());
        addCorrelationIdFilter(boolQueryBuilder, criteria.correlationId());
        addSessionIdFilter(boolQueryBuilder, criteria.sessionId());
        addDateRangeFilter(boolQueryBuilder, criteria.dateRange());
        addFullTextQuery(boolQueryBuilder, criteria.query());
        addTagsFilter(boolQueryBuilder, criteria.tags());

        return NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .build();
    }

    public NativeQuery buildQuickSearchQuery(String query, String tenantId) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        addTenantFilter(boolQueryBuilder, tenantId);

        if (query != null && !query.isBlank()) {
            boolQueryBuilder.must(m -> m.multiMatch(mm -> mm
                    .query(query)
                    .fields("actorName", "actionDescription", "resourceName", "actorId", "resourceId")
                    .fuzziness("AUTO")));
        }

        return NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .build();
    }

    public NativeQuery buildCorrelationQuery(String correlationId) {
        return NativeQuery.builder()
                .withQuery(q -> q.term(t -> t.field("correlationId").value(correlationId)))
                .build();
    }

    public NativeQuery buildTimelineQuery(String tenantId, DateRange range) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        addTenantFilter(boolQueryBuilder, tenantId);
        addDateRangeFilter(boolQueryBuilder, range);

        return NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .build();
    }

    private void addTenantFilter(BoolQuery.Builder builder, String tenantId) {
        if (tenantId != null && !tenantId.isBlank()) {
            builder.filter(f -> f.term(t -> t.field("tenantId").value(tenantId)));
        }
    }

    private void addActorFilter(BoolQuery.Builder builder, String actorId) {
        if (actorId != null && !actorId.isBlank()) {
            builder.filter(f -> f.term(t -> t.field("actorId").value(actorId)));
        }
    }

    private void addActionTypesFilter(BoolQuery.Builder builder, List<Action.ActionType> actionTypes) {
        if (actionTypes != null && !actionTypes.isEmpty()) {
            List<String> types = actionTypes.stream().map(Enum::name).toList();
            builder.filter(f -> f.terms(t -> t
                    .field("actionType")
                    .terms(tv -> tv.value(types.stream()
                            .map(s -> co.elastic.clients.elasticsearch._types.FieldValue.of(s))
                            .toList()))));
        }
    }

    private void addResourceTypesFilter(BoolQuery.Builder builder, List<Resource.ResourceType> resourceTypes) {
        if (resourceTypes != null && !resourceTypes.isEmpty()) {
            List<String> types = resourceTypes.stream().map(Enum::name).toList();
            builder.filter(f -> f.terms(t -> t
                    .field("resourceType")
                    .terms(tv -> tv.value(types.stream()
                            .map(s -> co.elastic.clients.elasticsearch._types.FieldValue.of(s))
                            .toList()))));
        }
    }

    private void addCorrelationIdFilter(BoolQuery.Builder builder, String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            builder.filter(f -> f.term(t -> t.field("correlationId").value(correlationId)));
        }
    }

    private void addSessionIdFilter(BoolQuery.Builder builder, String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            builder.filter(f -> f.term(t -> t.field("sessionId").value(sessionId)));
        }
    }

    private void addDateRangeFilter(BoolQuery.Builder builder, DateRange dateRange) {
        if (dateRange != null) {
            builder.filter(f -> f.range(r -> r.untyped(u -> {
                u.field("timestamp");
                if (dateRange.from() != null) {
                    u.gte(JsonData.of(dateRange.from().toString()));
                }
                if (dateRange.to() != null) {
                    u.lte(JsonData.of(dateRange.to().toString()));
                }
                return u;
            })));
        }
    }

    private void addFullTextQuery(BoolQuery.Builder builder, String query) {
        if (query != null && !query.isBlank()) {
            builder.must(m -> m.multiMatch(mm -> mm
                    .query(query)
                    .fields("actorName", "actionDescription", "resourceName")
                    .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                    .fuzziness("AUTO")));
        }
    }

    private void addTagsFilter(BoolQuery.Builder builder, java.util.Map<String, String> tags) {
        if (tags != null && !tags.isEmpty()) {
            List<String> tagList = new ArrayList<>();
            tags.forEach((k, v) -> tagList.add(k + ":" + v));
            for (String tag : tagList) {
                builder.filter(f -> f.term(t -> t.field("tags").value(tag)));
            }
        }
    }
}
