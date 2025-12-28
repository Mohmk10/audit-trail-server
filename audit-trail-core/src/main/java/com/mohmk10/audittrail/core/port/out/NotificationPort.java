package com.mohmk10.audittrail.core.port.out;

import com.mohmk10.audittrail.core.dto.Alert;
import java.util.List;

public interface NotificationPort {

    void send(Alert alert);

    void sendBatch(List<Alert> alerts);
}
