package com.mohmk10.audittrail.ingestion.service;

import com.mohmk10.audittrail.core.domain.Event;

public interface EventEnrichmentService {

    Event enrich(Event event);
}
