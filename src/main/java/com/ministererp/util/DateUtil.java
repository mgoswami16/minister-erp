package com.ministererp.util;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class DateUtil {

    // ── Formatters
    public static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static final DateTimeFormatter DATE_ONLY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final DateTimeFormatter TIME_ONLY =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    public static final DateTimeFormatter SHEET_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static final DateTimeFormatter MONTH_YEAR =
            DateTimeFormatter.ofPattern("MMM yyyy");

    public static final DateTimeFormatter ISO_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ─────────────────────────────────────────
    // FORMAT METHODS
    // ─────────────────────────────────────────

    // Format for display: 15/12/2024 10:30:00
    public String formatDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_FORMAT);
    }

    // Format date only: 15/12/2024
    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_ONLY);
    }

    // Format time only: 10:30:00
    public String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_ONLY);
    }

    // Format for Google Sheets: 2024-12-15 10:30:00
    public String formatForSheet(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(SHEET_FORMAT);
    }

    // Format month year: Dec 2024
    public String formatMonthYear(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(MONTH_YEAR);
    }

    // Format ISO: 2024-12-15
    public String formatISO(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(ISO_FORMAT);
    }

    // ─────────────────────────────────────────
    // CURRENT DATE/TIME METHODS
    // ─────────────────────────────────────────

    // Get current timestamp as string
    public String getCurrentTimestamp() {
        return LocalDateTime.now().format(DISPLAY_FORMAT);
    }

    // Get current date as string
    public String getCurrentDate() {
        return LocalDateTime.now().format(DATE_ONLY);
    }

    // Get current time as string
    public String getCurrentTime() {
        return LocalDateTime.now().format(TIME_ONLY);
    }

    // Get current LocalDateTime
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    // Get start of today: 2024-12-15 00:00:00
    public LocalDateTime startOfToday() {
        return LocalDate.now().atStartOfDay();
    }

    // Get end of today: 2024-12-15 23:59:59
    public LocalDateTime endOfToday() {
        return LocalDate.now().atTime(23, 59, 59);
    }

    // Get start of current month
    public LocalDateTime startOfCurrentMonth() {
        return LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
    }

    // Get start of current week (Monday)
    public LocalDateTime startOfCurrentWeek() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(
                today.getDayOfWeek().getValue() - 1);
        return monday.atStartOfDay();
    }

    // ─────────────────────────────────────────
    // PARSE METHODS
    // ─────────────────────────────────────────

    // Parse from Google Sheets format
    public LocalDateTime parseFromSheet(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDateTime.parse(dateStr, SHEET_FORMAT);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateStr, DISPLAY_FORMAT);
            } catch (Exception ex) {
                try {
                    // Try date only - add midnight time
                    LocalDate date = LocalDate.parse(
                            dateStr, ISO_FORMAT);
                    return date.atStartOfDay();
                } catch (Exception exc) {
                    return null;
                }
            }
        }
    }

    // Parse from display format
    public LocalDateTime parseDisplay(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDateTime.parse(dateStr, DISPLAY_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    // Parse date only string to LocalDate
    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr, ISO_FORMAT);
        } catch (Exception e) {
            try {
                DateTimeFormatter fmt =
                        DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(dateStr, fmt);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    // ─────────────────────────────────────────
    // CALCULATION METHODS
    // ─────────────────────────────────────────

    // Get days between two dates
    public long getDaysBetween(
            LocalDateTime start,
            LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    // Get hours between two dates
    public long getHoursBetween(
            LocalDateTime start,
            LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.HOURS.between(start, end);
    }

    // Get minutes between two dates
    public long getMinutesBetween(
            LocalDateTime start,
            LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.MINUTES.between(start, end);
    }

    // Get age of complaint in days from now
    public long getAgeInDays(LocalDateTime createdDate) {
        if (createdDate == null) return 0;
        return getDaysBetween(createdDate, LocalDateTime.now());
    }

    // ─────────────────────────────────────────
    // STATUS CHECK METHODS
    // ─────────────────────────────────────────

    // Check if complaint is overdue (more than 7 days old)
    public boolean isOverdue(LocalDateTime createdDate) {
        if (createdDate == null) return false;
        return getAgeInDays(createdDate) > 7;
    }

    // Check if complaint is critical age (more than 3 days)
    public boolean isCriticalAge(LocalDateTime createdDate) {
        if (createdDate == null) return false;
        return getAgeInDays(createdDate) > 3;
    }

    // Check if date is today
    public boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.toLocalDate()
                .equals(LocalDate.now());
    }

    // Check if date is this week
    public boolean isThisWeek(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return !dateTime.isBefore(startOfCurrentWeek());
    }

    // Check if date is this month
    public boolean isThisMonth(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return !dateTime.isBefore(startOfCurrentMonth());
    }

    // ─────────────────────────────────────────
    // LABEL METHODS
    // ─────────────────────────────────────────

    // Get human readable age label
    public String getAgeLabel(LocalDateTime createdDate) {
        if (createdDate == null) return "Unknown";

        long minutes = getMinutesBetween(
                createdDate, LocalDateTime.now());

        if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else {
            long days = minutes / 1440;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }
    }

    // Get overdue label
    public String getOverdueLabel(LocalDateTime createdDate) {
        if (createdDate == null) return "";
        long days = getAgeInDays(createdDate);
        if (days > 7) return "🔴 OVERDUE (" + days + " days)";
        if (days > 3) return "🟠 At Risk (" + days + " days)";
        return "🟢 On Track (" + days + " days)";
    }
}