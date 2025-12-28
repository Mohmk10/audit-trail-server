package com.mohmk10.audittrail.storage.service;

import com.mohmk10.audittrail.core.domain.Event;
import java.util.List;

public interface HashChainService {

    String calculateHash(Event event, String previousHash);

    boolean verifyChain(List<Event> events);

    String getLastHash(String tenantId);
}
