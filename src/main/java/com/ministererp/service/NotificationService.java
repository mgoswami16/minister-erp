package com.ministererp.service;

import com.ministererp.model.Notification;
import com.ministererp.model.User;
import com.ministererp.util.DateUtil;
import com.ministererp.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final GoogleSheetsService googleSheetsService;
    private final IdGenerator idGenerator;
    private final DateUtil dateUtil;

    // ─────────────────────────────────────────
    // SEND NOTIFICATION
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void sendNotification(
            String userId,
            String userName,
            String title,
            String message,
            String type,
            String complaintId) {

        try {
            Notification notification =
                    Notification.builder()
                            .notificationId(
                                    idGenerator
                                    .generateNotificationId())
                            .userId(userId)
                            .userName(userName)
                            .title(title)
                            .message(message)
                            .type(type)
                            .complaintId(complaintId)
                            .isRead(false)
                            .createdAt(dateUtil.now())
                            .build();

            googleSheetsService
                    .saveNotification(notification);

        } catch (Exception e) {
            System.out.println(
                    "Error sending notification: "
                    + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // NOTIFY NEW ASSIGNMENT
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void notifyAssignment(
            String officerId,
            String officerName,
            String complaintId,
            String subject) {

        sendNotification(
                officerId,
                officerName,
                "📋 New Complaint Assigned",
                "Complaint " + complaintId
                + " has been assigned to you. Subject: "
                + subject,
                Notification.TYPE_ASSIGNMENT,
                complaintId
        );
    }

    // ─────────────────────────────────────────
    // NOTIFY ESCALATION
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void notifyEscalation(
            String officerId,
            String officerName,
            String complaintId,
            String remarks) {

        sendNotification(
                officerId,
                officerName,
                "⚠️ Complaint Escalated",
                "Complaint " + complaintId
                + " has been escalated. Remarks: "
                + remarks,
                Notification.TYPE_ESCALATION,
                complaintId
        );
    }

    // ─────────────────────────────────────────
    // NOTIFY FORWARDING
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void notifyForwarding(
            String toOfficerId,
            String toOfficerName,
            String fromOfficerName,
            String complaintId,
            String remarks) {

        sendNotification(
                toOfficerId,
                toOfficerName,
                "📨 Complaint Forwarded To You",
                "Complaint " + complaintId
                + " forwarded from "
                + fromOfficerName
                + ". Remarks: " + remarks,
                Notification.TYPE_FORWARDING,
                complaintId
        );
    }

    // ─────────────────────────────────────────
    // NOTIFY MINISTER DIRECTION
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void notifyMinisterDirection(
            String officerId,
            String officerName,
            String complaintId,
            String direction) {

        sendNotification(
                officerId,
                officerName,
                "📌 Minister Direction Received",
                "Minister has given direction for "
                + complaintId + ": " + direction,
                Notification.TYPE_DIRECTION,
                complaintId
        );
    }

    // ─────────────────────────────────────────
    // NOTIFY APPOINTMENT UPDATE
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void notifyAppointmentUpdate(
            String userId,
            String userName,
            String appointmentId,
            String status) {

        String title = status.equals("APPROVED")
                ? "✅ Appointment Approved"
                : "❌ Appointment Rejected";

        sendNotification(
                userId,
                userName,
                title,
                "Your appointment " + appointmentId
                + " has been " + status.toLowerCase(),
                Notification.TYPE_APPOINTMENT,
                appointmentId
        );
    }

    // ─────────────────────────────────────────
    // GET USER NOTIFICATIONS
    // ─────────────────────────────────────────
    public List<Notification> getUserNotifications(
            String userId) {
        return googleSheetsService
                .getNotificationsByUser(userId);
    }

    // ─────────────────────────────────────────
    // MARK AS READ
    // ─────────────────────────────────────────
    public boolean markAsRead(String notificationId) {
        return googleSheetsService
                .markNotificationRead(notificationId);
    }

    // ─────────────────────────────────────────
    // GET UNREAD COUNT
    // ─────────────────────────────────────────
    public long getUnreadCount(String userId) {
        return getUserNotifications(userId)
                .stream()
                .filter(n -> !n.isRead())
                .count();
    }
}