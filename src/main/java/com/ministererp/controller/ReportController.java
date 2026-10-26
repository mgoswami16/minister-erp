package com.ministererp.controller;

import com.ministererp.dto.response.ApiResponse;
import com.ministererp.model.Complaint;
import com.ministererp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ─────────────────────────────────────────
    // DAILY REPORT
    // GET /api/v1/reports/daily
    // ─────────────────────────────────────────
    @GetMapping("/daily")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<ApiResponse<List<Complaint>>>
    getDailyReport() {

        List<Complaint> complaints =
                reportService.getDailyReport();

        return ResponseEntity.ok(
                ApiResponse.success(
                        complaints,
                        "Daily report: "
                        + complaints.size()
                        + " complaints"));
    }

    // ─────────────────────────────────────────
    // WEEKLY REPORT
    // GET /api/v1/reports/weekly
    // ─────────────────────────────────────────
    @GetMapping("/weekly")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<ApiResponse<List<Complaint>>>
    getWeeklyReport() {

        List<Complaint> complaints =
                reportService.getWeeklyReport();

        return ResponseEntity.ok(
                ApiResponse.success(
                        complaints,
                        "Weekly report: "
                        + complaints.size()
                        + " complaints"));
    }

    // ─────────────────────────────────────────
    // MONTHLY REPORT
    // GET /api/v1/reports/monthly
    // ─────────────────────────────────────────
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<ApiResponse<List<Complaint>>>
    getMonthlyReport() {

        List<Complaint> complaints =
                reportService.getMonthlyReport();

        return ResponseEntity.ok(
                ApiResponse.success(
                        complaints,
                        "Monthly report: "
                        + complaints.size()
                        + " complaints"));
    }

    // ─────────────────────────────────────────
    // DATE RANGE REPORT
    // GET /api/v1/reports/range
    // ─────────────────────────────────────────
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<ApiResponse<List<Complaint>>>
    getDateRangeReport(
            @RequestParam String fromDate,
            @RequestParam String toDate) {

        List<Complaint> complaints =
                reportService.getComplaintsByDateRange(
                        fromDate,
                        toDate);

        return ResponseEntity.ok(
                ApiResponse.success(
                        complaints,
                        "Report from " + fromDate
                        + " to " + toDate
                        + ": " + complaints.size()
                        + " complaints"));
    }

    // ─────────────────────────────────────────
    // EXPORT DAILY EXCEL
    // GET /api/v1/reports/export/daily
    // ─────────────────────────────────────────
    @GetMapping("/export/daily")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<byte[]> exportDailyExcel()
            throws IOException {

        List<Complaint> complaints =
                reportService.getDailyReport();

        byte[] excelData =
                reportService.exportToExcel(
                        complaints,
                        "Daily Complaints Report");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-"
                + "officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData(
                "attachment",
                "daily-report.xlsx");
        headers.setContentLength(excelData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    // ─────────────────────────────────────────
    // EXPORT WEEKLY EXCEL
    // GET /api/v1/reports/export/weekly
    // ─────────────────────────────────────────
    @GetMapping("/export/weekly")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<byte[]> exportWeeklyExcel()
            throws IOException {

        List<Complaint> complaints =
                reportService.getWeeklyReport();

        byte[] excelData =
                reportService.exportToExcel(
                        complaints,
                        "Weekly Complaints Report");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-"
                + "officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData(
                "attachment",
                "weekly-report.xlsx");
        headers.setContentLength(excelData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    // ─────────────────────────────────────────
    // EXPORT MONTHLY EXCEL
    // GET /api/v1/reports/export/monthly
    // ─────────────────────────────────────────
    @GetMapping("/export/monthly")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<byte[]> exportMonthlyExcel()
            throws IOException {

        List<Complaint> complaints =
                reportService.getMonthlyReport();

        byte[] excelData =
                reportService.exportToExcel(
                        complaints,
                        "Monthly Complaints Report");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-"
                + "officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData(
                "attachment",
                "monthly-report.xlsx");
        headers.setContentLength(excelData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    // ─────────────────────────────────────────
    // EXPORT DATE RANGE EXCEL
    // GET /api/v1/reports/export/range
    // ─────────────────────────────────────────
    @GetMapping("/export/range")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<byte[]> exportRangeExcel(
            @RequestParam String fromDate,
            @RequestParam String toDate)
            throws IOException {

        List<Complaint> complaints =
                reportService.getComplaintsByDateRange(
                        fromDate, toDate);

        byte[] excelData =
                reportService.exportToExcel(
                        complaints,
                        "Complaints Report: "
                        + fromDate + " to " + toDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-"
                + "officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData(
                "attachment",
                "report-" + fromDate
                + "-to-" + toDate + ".xlsx");
        headers.setContentLength(excelData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    // ─────────────────────────────────────────
    // EXPORT OFFICER PERFORMANCE EXCEL
    // GET /api/v1/reports/export/officer-performance
    // ─────────────────────────────────────────
    @GetMapping("/export/officer-performance")
    @PreAuthorize("hasRole('MINISTER')")
    public ResponseEntity<byte[]>
    exportOfficerPerformance()
            throws IOException {

        byte[] excelData =
                reportService.exportOfficerPerformance();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-"
                + "officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData(
                "attachment",
                "officer-performance.xlsx");
        headers.setContentLength(excelData.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}