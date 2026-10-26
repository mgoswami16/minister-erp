package com.ministererp.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IdGenerator {

    private static final AtomicInteger complaintCounter
            = new AtomicInteger(1);
    private static final AtomicInteger appointmentCounter
            = new AtomicInteger(1);
    private static final AtomicInteger userCounter
            = new AtomicInteger(1);
    private static final AtomicInteger auditCounter
            = new AtomicInteger(1);
    private static final AtomicInteger notificationCounter
            = new AtomicInteger(1);
    private static final AtomicInteger forwardCounter
            = new AtomicInteger(1);

    private static final DateTimeFormatter YEAR_FORMAT
            = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter DATE_FORMAT
            = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ── Generate Complaint ID: CMP-2024-0001
    public String generateComplaintId() {
        String year = LocalDateTime.now().format(YEAR_FORMAT);
        int count = complaintCounter.getAndIncrement();
        return String.format("CMP-%s-%04d", year, count);
    }

    // ── Generate Appointment ID: APT-20241215-0001
    public String generateAppointmentId() {
        String date = LocalDateTime.now().format(DATE_FORMAT);
        int count = appointmentCounter.getAndIncrement();
        return String.format("APT-%s-%04d", date, count);
    }

    // ── Generate User ID: USR-0001
    public String generateUserId() {
        int count = userCounter.getAndIncrement();
        return String.format("USR-%04d", count);
    }

    // ── Generate Audit ID: AUD-20241215-0001
    public String generateAuditId() {
        String date = LocalDateTime.now().format(DATE_FORMAT);
        int count = auditCounter.getAndIncrement();
        return String.format("AUD-%s-%04d", date, count);
    }

    // ── Generate Notification ID: NTF-0001
    public String generateNotificationId() {
        int count = notificationCounter.getAndIncrement();
        return String.format("NTF-%04d", count);
    }

    // ── Generate Forwarding ID: FWD-0001
    public String generateForwardId() {
        int count = forwardCounter.getAndIncrement();
        return String.format("FWD-%04d", count);
    }

    // ── Reset counters (use carefully)
    public void resetCounters(
            int complaint,
            int appointment,
            int user,
            int audit) {
        complaintCounter.set(complaint);
        appointmentCounter.set(appointment);
        userCounter.set(user);
        auditCounter.set(audit);
    }
}