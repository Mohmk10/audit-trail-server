package com.mohmk10.audittrail.ingestion.service;

import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import java.util.List;

public interface EventValidationService {

    List<String> validate(EventRequest request);
}
