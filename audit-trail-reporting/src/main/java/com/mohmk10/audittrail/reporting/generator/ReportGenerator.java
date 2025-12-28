package com.mohmk10.audittrail.reporting.generator;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;

import java.util.List;

public interface ReportGenerator {
    ReportFormat getFormat();
    byte[] generate(Report report, List<Event> events, ReportTemplate template);
}
