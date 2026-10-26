package com.ministererp.service;

import com.ministererp.model.Complaint;
import com.ministererp.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final GoogleSheetsService googleSheetsService;
    private final DateUtil dateUtil;

    // ─────────────────────────────────────────
    // GET COMPLAINTS BY DATE RANGE
    // ─────────────────────────────────────────
    public List<Complaint> getComplaintsByDateRange(
            String fromDate,
            String toDate) {

        LocalDateTime from = dateUtil
                .parseFromSheet(fromDate + " 00:00:00");
        LocalDateTime to = dateUtil
                .parseFromSheet(toDate + " 23:59:59");

        return googleSheetsService.getAllComplaints()
                .stream()
                .filter(c -> c.getCreatedDate() != null
                        && !c.getCreatedDate().isBefore(from)
                        && !c.getCreatedDate().isAfter(to))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET DAILY REPORT
    // ─────────────────────────────────────────
    public List<Complaint> getDailyReport() {
        return googleSheetsService.getAllComplaints()
                .stream()
                .filter(c -> dateUtil.isToday(
                        c.getCreatedDate()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET WEEKLY REPORT
    // ─────────────────────────────────────────
    public List<Complaint> getWeeklyReport() {
        return googleSheetsService.getAllComplaints()
                .stream()
                .filter(c -> dateUtil.isThisWeek(
                        c.getCreatedDate()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET MONTHLY REPORT
    // ─────────────────────────────────────────
    public List<Complaint> getMonthlyReport() {
        return googleSheetsService.getAllComplaints()
                .stream()
                .filter(c -> dateUtil.isThisMonth(
                        c.getCreatedDate()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // EXPORT TO EXCEL
    // ─────────────────────────────────────────
    public byte[] exportToExcel(
            List<Complaint> complaints,
            String reportTitle) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Complaints");

            // ── Title Style
            CellStyle titleStyle =
                    workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(
                    IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(
                    HorizontalAlignment.CENTER);

            // ── Header Style
            CellStyle headerStyle =
                    workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(
                    IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(
                    IndexedColors.ORANGE.getIndex());
            headerStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // ── Data Style
            CellStyle dataStyle =
                    workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // ── Alternate Row Style
            CellStyle altStyle =
                    workbook.createCellStyle();
            altStyle.setFillForegroundColor(
                    IndexedColors.LIGHT_YELLOW.getIndex());
            altStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND);
            altStyle.setBorderBottom(BorderStyle.THIN);
            altStyle.setBorderTop(BorderStyle.THIN);
            altStyle.setBorderLeft(BorderStyle.THIN);
            altStyle.setBorderRight(BorderStyle.THIN);

            int rowNum = 0;

            // ── Title Row
            Row titleRow = sheet.createRow(rowNum++);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(reportTitle
                    + " | Generated: "
                    + dateUtil.getCurrentTimestamp());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(
                    new CellRangeAddress(0, 0, 0, 13));

            // ── Empty Row
            sheet.createRow(rowNum++);

            // ── Header Row
            String[] headers = {
                "Complaint ID", "Date", "Caller Name",
                "Mobile", "District", "State",
                "Subject", "Category", "Priority",
                "Status", "Assigned To",
                "Action Taken", "Resolved Date",
                "Operator"
            };

            Row headerRow = sheet.createRow(rowNum++);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data Rows
            for (Complaint c : complaints) {
                Row row = sheet.createRow(rowNum++);
                CellStyle style = (rowNum % 2 == 0)
                        ? altStyle : dataStyle;

                createCell(row, 0,
                        c.getComplaintId(), style);
                createCell(row, 1,
                        dateUtil.formatDisplay(
                                c.getCreatedDate()), style);
                createCell(row, 2,
                        c.getCallerName(), style);
                createCell(row, 3,
                        c.getCallerMobile(), style);
                createCell(row, 4,
                        c.getDistrict(), style);
                createCell(row, 5,
                        c.getState(), style);
                createCell(row, 6,
                        c.getSubject(), style);
                createCell(row, 7,
                        c.getCategory(), style);
                createCell(row, 8,
                        c.getPriority(), style);
                createCell(row, 9,
                        c.getStatus(), style);
                createCell(row, 10,
                        c.getAssignedName(), style);
                createCell(row, 11,
                        c.getActionTaken(), style);
                createCell(row, 12,
                        dateUtil.formatDisplay(
                                c.getResolvedDate()), style);
                createCell(row, 13,
                        c.getOperatorName(), style);
            }

            // ── Summary Row
            rowNum++;
            Row summaryRow = sheet.createRow(rowNum++);
            CellStyle summaryStyle =
                    workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true);
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(
                    IndexedColors.LIGHT_GREEN.getIndex());
            summaryStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND);

            Cell sumLabel = summaryRow.createCell(0);
            sumLabel.setCellValue(
                    "Total Records: "
                    + complaints.size());
            sumLabel.setCellStyle(summaryStyle);
            sheet.addMergedRegion(new CellRangeAddress(
                    rowNum - 1, rowNum - 1, 0, 13));

            // ── Auto size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // ── Write to byte array
            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // ─────────────────────────────────────────
    // HELPER: CREATE CELL
    // ─────────────────────────────────────────
    private void createCell(
            Row row,
            int column,
            String value,
            CellStyle style) {

        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    // ─────────────────────────────────────────
    // EXPORT OFFICER PERFORMANCE TO EXCEL
    // ─────────────────────────────────────────
    public byte[] exportOfficerPerformance()
            throws IOException {

        List<Complaint> allComplaints =
                googleSheetsService.getAllComplaints();

        List<com.ministererp.model.User> officers =
                googleSheetsService.getAllUsers()
                        .stream()
                        .filter(u ->
                                com.ministererp.model.User
                                .ROLE_OFFICER
                                .equals(u.getRole()))
                        .collect(
                                java.util.stream
                                .Collectors.toList());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(
                    "Officer Performance");

            // ── Header Style
            CellStyle headerStyle =
                    workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(
                    IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(
                    IndexedColors.ORANGE.getIndex());
            headerStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(
                    HorizontalAlignment.CENTER);

            // ── Data Style
            CellStyle dataStyle =
                    workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(
                    HorizontalAlignment.CENTER);

            int rowNum = 0;

            // ── Title
            Row titleRow = sheet.createRow(rowNum++);
            CellStyle titleStyle =
                    workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(
                    IndexedColors.WHITE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(
                    IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(
                    HorizontalAlignment.CENTER);

            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(
                    "Officer Performance Report | "
                    + dateUtil.getCurrentDate());
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(
                    new CellRangeAddress(0, 0, 0, 7));

            sheet.createRow(rowNum++);

            // ── Headers
            String[] headers = {
                "Officer Name", "Department",
                "Total Assigned", "Resolved",
                "Pending", "Escalated",
                "Resolution Rate %", "Status"
            };

            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Data Rows
            for (com.ministererp.model.User officer
                    : officers) {

                List<Complaint> officerComplaints =
                        allComplaints.stream()
                                .filter(c -> officer
                                        .getUserId()
                                        .equals(c
                                        .getAssignedTo()))
                                .collect(
                                    java.util.stream
                                    .Collectors.toList());

                int total = officerComplaints.size();

                long resolved = officerComplaints
                        .stream()
                        .filter(c ->
                            Complaint.STATUS_RESOLVED
                            .equals(c.getStatus())
                            || Complaint.STATUS_CLOSED
                            .equals(c.getStatus()))
                        .count();

                long pending = officerComplaints
                        .stream()
                        .filter(c ->
                            Complaint.STATUS_PENDING
                            .equals(c.getStatus())
                            || Complaint.STATUS_ASSIGNED
                            .equals(c.getStatus()))
                        .count();

                long escalated = officerComplaints
                        .stream()
                        .filter(c ->
                            Complaint.STATUS_ESCALATED
                            .equals(c.getStatus()))
                        .count();

                double rate = total > 0
                        ? ((double) resolved / total)
                            * 100
                        : 0.0;

                String performanceStatus =
                        rate >= 80 ? "Excellent ✅"
                        : rate >= 60 ? "Good 🟡"
                        : rate >= 40 ? "Average 🟠"
                        : "Needs Improvement 🔴";

                Row row = sheet.createRow(rowNum++);

                createCell(row, 0,
                        officer.getName(), dataStyle);
                createCell(row, 1,
                        officer.getDepartment(), dataStyle);
                createCell(row, 2,
                        String.valueOf(total), dataStyle);
                createCell(row, 3,
                        String.valueOf(resolved), dataStyle);
                createCell(row, 4,
                        String.valueOf(pending), dataStyle);
                createCell(row, 5,
                        String.valueOf(escalated), dataStyle);
                createCell(row, 6,
                        String.format("%.1f%%", rate),
                        dataStyle);
                createCell(row, 7,
                        performanceStatus, dataStyle);
            }

            // ── Auto size
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}