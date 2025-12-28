package com.mohmk10.audittrail.core.port.in;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.AnomalyResult;
import java.util.List;

public interface DetectAnomalyUseCase {

    AnomalyResult evaluate(Event event);

    List<AnomalyResult> evaluatePattern(List<Event> events);
}
