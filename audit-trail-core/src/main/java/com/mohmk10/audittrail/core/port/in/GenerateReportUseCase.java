package com.mohmk10.audittrail.core.port.in;

import com.mohmk10.audittrail.core.dto.ReportRequest;
import com.mohmk10.audittrail.core.dto.ReportResult;
import com.mohmk10.audittrail.core.dto.ScheduledReportRequest;

public interface GenerateReportUseCase {

    ReportResult generate(ReportRequest request);

    String schedule(ScheduledReportRequest request);
}
