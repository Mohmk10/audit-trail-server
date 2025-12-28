package com.mohmk10.audittrail.ingestion.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventRequest;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.BatchEventResponse;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;

public interface EventIngestionService {

    Event ingest(EventRequest request);

    BatchEventResponse ingestBatch(BatchEventRequest request);
}
