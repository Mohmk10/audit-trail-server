package com.mohmk10.audittrail.reporting.generator;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CsvReportGenerator implements ReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.CSV;
    }

    @Override
    public byte[] generate(Report report, List<Event> events, ReportTemplate template) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(UTF8_BOM);

            try (OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
                 CSVWriter writer = new CSVWriter(osw)) {

                String[] header = {
                        "ID",
                        "Timestamp",
                        "Actor ID",
                        "Actor Type",
                        "Actor Name",
                        "Actor IP",
                        "Action Type",
                        "Action Description",
                        "Action Category",
                        "Resource ID",
                        "Resource Type",
                        "Resource Name",
                        "Correlation ID",
                        "Session ID",
                        "Source",
                        "Tenant ID",
                        "Hash"
                };
                writer.writeNext(header);

                for (Event event : events) {
                    String[] row = {
                            event.id().toString(),
                            DATE_FORMATTER.format(event.timestamp()),
                            event.actor().id(),
                            event.actor().type().name(),
                            event.actor().name() != null ? event.actor().name() : "",
                            event.actor().ip() != null ? event.actor().ip() : "",
                            event.action().type().name(),
                            event.action().description() != null ? event.action().description() : "",
                            event.action().category() != null ? event.action().category() : "",
                            event.resource().id(),
                            event.resource().type().name(),
                            event.resource().name() != null ? event.resource().name() : "",
                            event.metadata().correlationId() != null ? event.metadata().correlationId() : "",
                            event.metadata().sessionId() != null ? event.metadata().sessionId() : "",
                            event.metadata().source() != null ? event.metadata().source() : "",
                            event.metadata().tenantId(),
                            event.hash() != null ? event.hash() : ""
                    };
                    writer.writeNext(row);
                }
            }

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }
}
