package com.mohmk10.audittrail.storage.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.storage.adapter.out.persistence.entity.EventEntity;
import com.mohmk10.audittrail.storage.adapter.out.persistence.mapper.EventMapper;
import com.mohmk10.audittrail.storage.adapter.out.persistence.repository.JpaEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImmutableStorageServiceImpl implements ImmutableStorageService {

    private final JpaEventRepository jpaEventRepository;
    private final HashChainService hashChainService;
    private final SignatureService signatureService;

    public ImmutableStorageServiceImpl(
            JpaEventRepository jpaEventRepository,
            HashChainService hashChainService,
            SignatureService signatureService) {
        this.jpaEventRepository = jpaEventRepository;
        this.hashChainService = hashChainService;
        this.signatureService = signatureService;
    }

    @Override
    @Transactional
    public Event store(Event event) {
        String tenantId = event.metadata() != null ? event.metadata().tenantId() : "default";
        String previousHash = hashChainService.getLastHash(tenantId);
        String hash = hashChainService.calculateHash(event, previousHash);
        String signature = signatureService.sign(hash);

        Event securedEvent = new Event(
                event.id(),
                event.timestamp(),
                event.actor(),
                event.action(),
                event.resource(),
                event.metadata(),
                previousHash,
                hash,
                signature
        );

        EventEntity entity = EventMapper.toEntity(securedEvent);
        EventEntity savedEntity = jpaEventRepository.save(entity);
        return EventMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional
    public List<Event> storeBatch(List<Event> events) {
        List<Event> storedEvents = new ArrayList<>();
        for (Event event : events) {
            storedEvents.add(store(event));
        }
        return storedEvents;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> findById(UUID id) {
        return jpaEventRepository.findById(id)
                .map(EventMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyIntegrity(UUID eventId) {
        Optional<EventEntity> entityOpt = jpaEventRepository.findById(eventId);
        if (entityOpt.isEmpty()) {
            return false;
        }

        EventEntity entity = entityOpt.get();
        Event event = EventMapper.toDomain(entity);

        String recalculatedHash = hashChainService.calculateHash(event, event.previousHash());
        if (!recalculatedHash.equals(event.hash())) {
            return false;
        }

        return signatureService.verify(event.hash(), event.signature());
    }
}
