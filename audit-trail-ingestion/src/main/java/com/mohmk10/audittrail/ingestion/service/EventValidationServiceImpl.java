package com.mohmk10.audittrail.ingestion.service;

import com.mohmk10.audittrail.core.domain.Action;
import com.mohmk10.audittrail.core.domain.Actor;
import com.mohmk10.audittrail.core.domain.Resource;
import com.mohmk10.audittrail.ingestion.adapter.in.rest.dto.EventRequest;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EventValidationServiceImpl implements EventValidationService {

    private static final Set<String> VALID_ACTOR_TYPES = Stream.of(Actor.ActorType.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    private static final Set<String> VALID_ACTION_TYPES = Stream.of(Action.ActionType.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    private static final Set<String> VALID_RESOURCE_TYPES = Stream.of(Resource.ResourceType.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    @Override
    public List<String> validate(EventRequest request) {
        List<String> violations = new ArrayList<>();

        if (request.actor() != null) {
            validateActorType(request.actor().type(), violations);
        }

        if (request.action() != null) {
            validateActionType(request.action().type(), violations);
        }

        if (request.resource() != null) {
            validateResourceType(request.resource().type(), violations);
        }

        return violations;
    }

    private void validateActorType(String type, List<String> violations) {
        if (type != null && !VALID_ACTOR_TYPES.contains(type.toUpperCase())) {
            violations.add("Invalid actor type: " + type + ". Valid types: " + VALID_ACTOR_TYPES);
        }
    }

    private void validateActionType(String type, List<String> violations) {
        if (type != null && !VALID_ACTION_TYPES.contains(type.toUpperCase())) {
            violations.add("Invalid action type: " + type + ". Valid types: " + VALID_ACTION_TYPES);
        }
    }

    private void validateResourceType(String type, List<String> violations) {
        if (type != null && !VALID_RESOURCE_TYPES.contains(type.toUpperCase())) {
            violations.add("Invalid resource type: " + type + ". Valid types: " + VALID_RESOURCE_TYPES);
        }
    }
}
