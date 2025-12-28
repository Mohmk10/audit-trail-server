package com.mohmk10.audittrail.ingestion.adapter.in;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.port.in.IngestEventUseCase;
import com.mohmk10.audittrail.storage.service.ImmutableStorageService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class IngestEventUseCaseImpl implements IngestEventUseCase {

    private final ImmutableStorageService immutableStorageService;

    public IngestEventUseCaseImpl(ImmutableStorageService immutableStorageService) {
        this.immutableStorageService = immutableStorageService;
    }

    @Override
    public Event ingest(Event event) {
        return immutableStorageService.store(event);
    }

    @Override
    public List<Event> ingestBatch(List<Event> events) {
        return immutableStorageService.storeBatch(events);
    }
}
