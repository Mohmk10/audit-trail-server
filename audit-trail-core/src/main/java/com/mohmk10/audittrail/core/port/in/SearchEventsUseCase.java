package com.mohmk10.audittrail.core.port.in;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.SearchCriteria;
import com.mohmk10.audittrail.core.dto.SearchResult;
import java.util.Optional;
import java.util.UUID;

public interface SearchEventsUseCase {

    SearchResult<Event> search(SearchCriteria criteria);

    Optional<Event> findById(UUID id);

    SearchResult<Event> findByCorrelationId(String correlationId);
}
