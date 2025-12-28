package com.mohmk10.audittrail.search.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.document.EventDocument;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.mapper.EventDocumentMapper;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.repository.ElasticsearchEventRepository;

@Service
public class EventIndexingServiceImpl implements EventIndexingService {

    private static final Logger log = LoggerFactory.getLogger(EventIndexingServiceImpl.class);
    private static final String INDEX_NAME = "events";

    private final ElasticsearchEventRepository esRepository;
    private final EventDocumentMapper mapper;
    private final ElasticsearchOperations elasticsearchOperations;

    public EventIndexingServiceImpl(
            ElasticsearchEventRepository esRepository,
            EventDocumentMapper mapper,
            ElasticsearchOperations elasticsearchOperations) {
        this.esRepository = esRepository;
        this.mapper = mapper;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public void index(Event event) {
        if (event == null) {
            return;
        }
        EventDocument doc = mapper.toDocument(event);
        esRepository.save(doc);
        log.debug("Indexed event: {}", event.id());
    }

    @Override
    public void indexBatch(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        List<EventDocument> documents = mapper.toDocumentList(events);
        esRepository.saveAll(documents);
        log.debug("Indexed {} events", events.size());
    }

    @Override
    public void reindexAll() {
        log.info("Starting index recreation");
        deleteIndex();
        createIndex();
        log.info("Index recreated. Use indexBatch() to re-index events from storage.");
    }

    @Override
    public void deleteIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(INDEX_NAME));
        if (indexOps.exists()) {
            indexOps.delete();
            log.info("Deleted index: {}", INDEX_NAME);
        }
    }

    @Override
    public void deleteByTenantId(String tenantId) {
        esRepository.deleteByTenantId(tenantId);
        log.info("Deleted all documents for tenant: {}", tenantId);
    }

    private void createIndex() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(EventDocument.class);
        if (!indexOps.exists()) {
            indexOps.create();
            indexOps.putMapping(indexOps.createMapping());
            log.info("Created index: {}", INDEX_NAME);
        }
    }
}
