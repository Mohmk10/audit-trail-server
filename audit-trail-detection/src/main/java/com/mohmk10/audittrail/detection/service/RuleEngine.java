package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.detection.domain.Alert;
import com.mohmk10.audittrail.detection.domain.RuleCondition;

import java.util.List;

public interface RuleEngine {

    List<Alert> evaluate(Event event);

    boolean matches(Event event, RuleCondition condition);
}
