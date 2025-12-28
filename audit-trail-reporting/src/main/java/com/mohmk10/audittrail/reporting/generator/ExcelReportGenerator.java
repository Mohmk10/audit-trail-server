package com.mohmk10.audittrail.reporting.generator;

import com.mohmk10.audittrail.core.domain.Event;
import com.mohmk10.audittrail.reporting.domain.Report;
import com.mohmk10.audittrail.reporting.domain.ReportFormat;
import com.mohmk10.audittrail.reporting.template.ReportTemplate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExcelReportGenerator implements ReportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Override
    public ReportFormat getFormat() {
        return ReportFormat.XLSX;
    }

    @Override
    public byte[] generate(Report report, List<Event> events, ReportTemplate template) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            createEventsSheet(workbook, events);
            createSummarySheet(workbook, report, events, template);

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private void createEventsSheet(Workbook workbook, List<Event> events) {
        Sheet sheet = workbook.createSheet("Events");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);

        String[] headers = {
                "ID", "Timestamp", "Actor ID", "Actor Type", "Actor Name", "Actor IP",
                "Action Type", "Action Description", "Resource ID", "Resource Type",
                "Resource Name", "Correlation ID", "Source", "Hash"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Event event : events) {
            Row row = sheet.createRow(rowNum++);
            int col = 0;

            row.createCell(col++).setCellValue(event.id().toString());

            Cell dateCell = row.createCell(col++);
            dateCell.setCellValue(DATE_FORMATTER.format(event.timestamp()));
            dateCell.setCellStyle(dateStyle);

            row.createCell(col++).setCellValue(event.actor().id());
            row.createCell(col++).setCellValue(event.actor().type().name());
            row.createCell(col++).setCellValue(event.actor().name() != null ? event.actor().name() : "");
            row.createCell(col++).setCellValue(event.actor().ip() != null ? event.actor().ip() : "");
            row.createCell(col++).setCellValue(event.action().type().name());
            row.createCell(col++).setCellValue(event.action().description() != null ? event.action().description() : "");
            row.createCell(col++).setCellValue(event.resource().id());
            row.createCell(col++).setCellValue(event.resource().type().name());
            row.createCell(col++).setCellValue(event.resource().name() != null ? event.resource().name() : "");
            row.createCell(col++).setCellValue(event.metadata().correlationId() != null ? event.metadata().correlationId() : "");
            row.createCell(col++).setCellValue(event.metadata().source() != null ? event.metadata().source() : "");
            row.createCell(col++).setCellValue(event.hash() != null ? event.hash() : "");
        }

        sheet.setAutoFilter(new CellRangeAddress(0, rowNum - 1, 0, headers.length - 1));

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createSummarySheet(Workbook workbook, Report report, List<Event> events, ReportTemplate template) {
        Sheet sheet = workbook.createSheet("Summary");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle boldStyle = createBoldStyle(workbook);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(report.name());
        titleCell.setCellStyle(boldStyle);

        rowNum++;

        addSummaryRow(sheet, rowNum++, "Report Type", report.type().name(), boldStyle);
        addSummaryRow(sheet, rowNum++, "Tenant ID", report.tenantId(), boldStyle);
        addSummaryRow(sheet, rowNum++, "Generated At", DATE_FORMATTER.format(report.generatedAt()), boldStyle);
        addSummaryRow(sheet, rowNum++, "Total Events", String.valueOf(events.size()), boldStyle);

        rowNum++;

        Row statsHeader = sheet.createRow(rowNum++);
        statsHeader.createCell(0).setCellValue("Action Type");
        statsHeader.createCell(1).setCellValue("Count");
        statsHeader.getCell(0).setCellStyle(headerStyle);
        statsHeader.getCell(1).setCellStyle(headerStyle);

        Map<String, Long> actionCounts = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.action().type().name(),
                        Collectors.counting()
                ));

        for (Map.Entry<String, Long> entry : actionCounts.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void addSummaryRow(Sheet sheet, int rowNum, String label, String value, CellStyle labelStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(value);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }
}
