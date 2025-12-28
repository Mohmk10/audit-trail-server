package com.mohmk10.audittrail.search.adapter.out.elasticsearch.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.mohmk10.audittrail.search.adapter.out.elasticsearch.document.EventDocument;

@Repository
public interface ElasticsearchEventRepository extends ElasticsearchRepository<EventDocument, String> {

    Page<EventDocument> findByTenantId(String tenantId, Pageable pageable);

    List<EventDocument> findByActorId(String actorId);

    List<EventDocument> findByResourceId(String resourceId);

    List<EventDocument> findByCorrelationId(String correlationId);

    Page<EventDocument> findByTenantIdAndActorId(String tenantId, String actorId, Pageable pageable);

    Page<EventDocument> findByTenantIdAndActionType(String tenantId, String actionType, Pageable pageable);

    Page<EventDocument> findByTenantIdAndResourceType(String tenantId, String resourceType, Pageable pageable);

    void deleteByTenantId(String tenantId);
}
