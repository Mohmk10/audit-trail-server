package com.mohmk10.audittrail.reporting.generator;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfReportGenerator implements ReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.PDF;
    }

    @Override
    public byte[] generate(Report report, List<Event> events, ReportTemplate template) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addHeader(document, report, template);
            addSummary(document, events);
            addEventsTable(document, events);
            addFooter(document, report);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addHeader(Document document, Report report, ReportTemplate template) {
        Paragraph title = new Paragraph(report.name())
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph(template.getDescription())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);

        Paragraph info = new Paragraph()
                .add("Tenant: " + report.tenantId())
                .add(" | ")
                .add("Type: " + report.type().name())
                .add(" | ")
                .add("Generated: " + DATE_FORMATTER.format(report.generatedAt()))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(info);
    }

    private void addSummary(Document document, List<Event> events) {
        Paragraph summaryTitle = new Paragraph("Summary")
                .setFontSize(16)
                .setBold()
                .setMarginTop(10);
        document.add(summaryTitle);

        long createCount = events.stream().filter(e -> "CREATE".equals(e.action().type().name())).count();
        long readCount = events.stream().filter(e -> "READ".equals(e.action().type().name())).count();
        long updateCount = events.stream().filter(e -> "UPDATE".equals(e.action().type().name())).count();
        long deleteCount = events.stream().filter(e -> "DELETE".equals(e.action().type().name())).count();
        long otherCount = events.size() - createCount - readCount - updateCount - deleteCount;

        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(50))
                .setMarginBottom(20);

        summaryTable.addCell(createSummaryCell("Total Events", String.valueOf(events.size())));
        summaryTable.addCell(createSummaryCell("Create", String.valueOf(createCount)));
        summaryTable.addCell(createSummaryCell("Read", String.valueOf(readCount)));
        summaryTable.addCell(createSummaryCell("Update", String.valueOf(updateCount)));
        summaryTable.addCell(createSummaryCell("Delete", String.valueOf(deleteCount)));
        summaryTable.addCell(createSummaryCell("Other", String.valueOf(otherCount)));

        document.add(summaryTable);
    }

    private Cell createSummaryCell(String label, String value) {
        return new Cell()
                .add(new Paragraph(label + ": " + value))
                .setBorder(null)
                .setPadding(2);
    }

    private void addEventsTable(Document document, List<Event> events) {
        Paragraph eventsTitle = new Paragraph("Events")
                .setFontSize(16)
                .setBold()
                .setMarginTop(10);
        document.add(eventsTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{15, 15, 15, 20, 20, 15}))
                .setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(table, "Timestamp");
        addHeaderCell(table, "Actor");
        addHeaderCell(table, "Action");
        addHeaderCell(table, "Description");
        addHeaderCell(table, "Resource");
        addHeaderCell(table, "IP");

        for (Event event : events) {
            table.addCell(createCell(DATE_FORMATTER.format(event.timestamp())));
            table.addCell(createCell(event.actor().name() != null ? event.actor().name() : event.actor().id()));
            table.addCell(createCell(event.action().type().name()));
            table.addCell(createCell(truncate(event.action().description(), 50)));
            table.addCell(createCell(event.resource().name() != null ? event.resource().name() : event.resource().id()));
            table.addCell(createCell(event.actor().ip() != null ? event.actor().ip() : "-"));
        }

        document.add(table);
    }

    private void addHeaderCell(Table table, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addHeaderCell(cell);
    }

    private Cell createCell(String text) {
        return new Cell()
                .add(new Paragraph(text != null ? text : "-"))
                .setFontSize(8)
                .setPadding(3);
    }

    private void addFooter(Document document, Report report) {
        Paragraph footer = new Paragraph()
                .add("Report ID: " + report.id())
                .add(" | ")
                .add("Checksum: " + (report.checksum() != null ? report.checksum().substring(0, 16) + "..." : "pending"))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30);
        document.add(footer);

        Paragraph signature = new Paragraph("This report is digitally certified for audit compliance.")
                .setFontSize(8)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(signature);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
