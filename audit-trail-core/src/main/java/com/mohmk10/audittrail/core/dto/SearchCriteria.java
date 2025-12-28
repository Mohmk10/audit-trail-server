package com.mohmk10.audittrail.core.dto;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Resource;
import java.util.List;
import java.util.Map;

public record SearchCriteria(
        String actorId,
        List<Action.ActionType> actionTypes,
        List<Resource.ResourceType> resourceTypes,
        DateRange dateRange,
        String tenantId,
        String correlationId,
        String sessionId,
        Map<String, String> tags,
        String query,
        int page,
        int size,
        String sortBy,
        SortDirection sortDirection
) {
    public enum SortDirection {
        ASC, DESC
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String actorId;
        private List<Action.ActionType> actionTypes;
        private List<Resource.ResourceType> resourceTypes;
        private DateRange dateRange;
        private String tenantId;
        private String correlationId;
        private String sessionId;
        private Map<String, String> tags;
        private String query;
        private int page = 0;
        private int size = 20;
        private String sortBy = "timestamp";
        private SortDirection sortDirection = SortDirection.DESC;

        public Builder actorId(String actorId) {
            this.actorId = actorId;
            return this;
        }

        public Builder actionTypes(List<Action.ActionType> actionTypes) {
            this.actionTypes = actionTypes;
            return this;
        }

        public Builder resourceTypes(List<Resource.ResourceType> resourceTypes) {
            this.resourceTypes = resourceTypes;
            return this;
        }

        public Builder dateRange(DateRange dateRange) {
            this.dateRange = dateRange;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder tags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder sortDirection(SortDirection sortDirection) {
            this.sortDirection = sortDirection;
            return this;
        }

        public SearchCriteria build() {
            return new SearchCriteria(
                    actorId, actionTypes, resourceTypes, dateRange, tenantId,
                    correlationId, sessionId, tags, query, page, size, sortBy, sortDirection
            );
        }
    }
}
