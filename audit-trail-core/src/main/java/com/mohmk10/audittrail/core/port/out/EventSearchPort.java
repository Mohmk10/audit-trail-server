package com.mohmk10.audittrail.core.port.out;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.AggregationRequest;
import com.mohmk10.audittrail.core.dto.AggregationResult;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;

public interface EventSearchPort {

    SearchResult<Event> search(SearchCriteria criteria);

    AggregationResult aggregate(AggregationRequest request);
}
