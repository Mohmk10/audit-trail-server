package com.mohmk10.audittrail.core.port.in;

import com.mohmk10.audittrail.core.domain.Event;
import java.util.List;

public interface IngestEventUseCase {

    Event ingest(Event event);

    List<Event> ingestBatch(List<Event> events);
}
