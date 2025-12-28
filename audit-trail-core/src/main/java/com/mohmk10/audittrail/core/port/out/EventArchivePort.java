package com.mohmk10.audittrail.core.port.out;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.core.dto.DateRange;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventArchivePort {

    void archive(Event event);

    Optional<Event> retrieve(UUID id);

    List<Event> listArchived(DateRange dateRange);
}
