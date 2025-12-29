package com.mohmk10.audittrail.search.service;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.domain.EventMetadata;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.document.EventDocument;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.mapper.EventDocumentMapper;
import com.mohmk10.audittrail.search.adapter.out.elasticsearch.repository.ElasticsearchEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventIndexingServiceImplTest {

    @Mock
    private ElasticsearchEventRepository esRepository;

    @Mock
    private EventDocumentMapper mapper;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @Mock
    private IndexOperations indexOperations;

    private EventIndexingServiceImpl indexingService;

    @BeforeEach
    void setUp() {
        indexingService = new EventIndexingServiceImpl(esRepository, mapper, elasticsearchOperations);
    }

    private Event createTestEvent() {
        return new Event(
                UUID.randomUUID(),
                Instant.now(),
                new Actor("actor-123", Actor.ActorType.USER, "John", null, null, null),
                new Action(Action.ActionType.CREATE, "Created", null),
                new Resource("res-123", Resource.ResourceType.DOCUMENT, "Report", null, null),
                new EventMetadata("web-app", "tenant-001", null, null, null, null),
                null,
                "hash-123",
                null
        );
    }

    private EventDocument createTestDocument() {
        EventDocument doc = new EventDocument();
        doc.setId(UUID.randomUUID().toString());
        doc.setTimestamp(Instant.now());
        doc.setActorId("actor-123");
        doc.setActorType("USER");
        doc.setActionType("CREATE");
        doc.setResourceId("res-123");
        doc.setResourceType("DOCUMENT");
        doc.setTenantId("tenant-001");
        return doc;
    }

    @Test
    void shouldIndexEvent() {
        Event event = createTestEvent();
        EventDocument document = createTestDocument();

        when(mapper.toDocument(event)).thenReturn(document);

        indexingService.index(event);

        verify(mapper).toDocument(event);
        verify(esRepository).save(document);
    }

    @Test
    void shouldNotIndexNullEvent() {
        indexingService.index(null);

        verify(mapper, never()).toDocument(any());
        verify(esRepository, never()).save(any());
    }

    @Test
    void shouldIndexBatchOfEvents() {
        List<Event> events = List.of(createTestEvent(), createTestEvent(), createTestEvent());
        List<EventDocument> documents = List.of(createTestDocument(), createTestDocument(), createTestDocument());

        when(mapper.toDocumentList(events)).thenReturn(documents);

        indexingService.indexBatch(events);

        verify(mapper).toDocumentList(events);
        verify(esRepository).saveAll(documents);
    }

    @Test
    void shouldNotIndexNullBatch() {
        indexingService.indexBatch(null);

        verify(mapper, never()).toDocumentList(any());
        verify(esRepository, never()).saveAll(any());
    }

    @Test
    void shouldNotIndexEmptyBatch() {
        indexingService.indexBatch(List.of());

        verify(mapper, never()).toDocumentList(any());
        verify(esRepository, never()).saveAll(any());
    }

    @Test
    void shouldDeleteIndex() {
        when(elasticsearchOperations.indexOps(IndexCoordinates.of("events"))).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true);

        indexingService.deleteIndex();

        verify(indexOperations).delete();
    }

    @Test
    void shouldNotDeleteNonExistentIndex() {
        when(elasticsearchOperations.indexOps(IndexCoordinates.of("events"))).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(false);

        indexingService.deleteIndex();

        verify(indexOperations, never()).delete();
    }

    @Test
    void shouldDeleteByTenantId() {
        String tenantId = "tenant-001";

        indexingService.deleteByTenantId(tenantId);

        verify(esRepository).deleteByTenantId(tenantId);
    }

    @Test
    void shouldReindexAll() {
        Document mockMapping = mock(Document.class);
        when(elasticsearchOperations.indexOps(IndexCoordinates.of("events"))).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(EventDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(true).thenReturn(false);
        when(indexOperations.createMapping()).thenReturn(mockMapping);

        indexingService.reindexAll();

        verify(indexOperations).delete();
        verify(indexOperations).create();
        verify(indexOperations).putMapping(mockMapping);
    }

    @Test
    void shouldHandleIndexCreationWhenNotExists() {
        Document mockMapping = mock(Document.class);
        when(elasticsearchOperations.indexOps(IndexCoordinates.of("events"))).thenReturn(indexOperations);
        when(elasticsearchOperations.indexOps(EventDocument.class)).thenReturn(indexOperations);
        when(indexOperations.exists()).thenReturn(false).thenReturn(false);
        when(indexOperations.createMapping()).thenReturn(mockMapping);

        indexingService.reindexAll();

        verify(indexOperations, never()).delete();
        verify(indexOperations).create();
    }
}
