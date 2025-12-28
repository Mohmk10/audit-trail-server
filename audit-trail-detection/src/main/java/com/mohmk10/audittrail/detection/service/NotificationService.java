package com.mohmk10.audittrail.detection.service;

import com.mohmk10.audittrail.detection.domain.Alert;

public interface NotificationService {

    void notify(Alert alert);
}
