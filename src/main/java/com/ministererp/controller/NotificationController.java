package com.ministererp.controller;

import com.ministererp.dto.response.ApiResponse;
import com.ministererp.model.Notification;
import com.ministererp.security.JwtTokenProvider;
import com.ministererp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    // ─────────────────────────────────────────
    // GET MY NOTIFICATIONS
    // GET /api/v1/notifications
    // ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>>
    getMyNotifications(
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId =
                jwtTokenProvider.extractUserId(token);

        List<Notification> notifications =
                notificationService
                        .getUserNotifications(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        notifications,
                        "Fetched " + notifications.size()
                        + " notifications"));
    }

    // ─────────────────────────────────────────
    // GET UNREAD COUNT
    // GET /api/v1/notifications/unread-count
    // ─────────────────────────────────────────
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>>
    getUnreadCount(
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId =
                jwtTokenProvider.extractUserId(token);

        long count =
                notificationService.getUnreadCount(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        count,
                        "Unread notifications: "
                        + count));
    }

    // ─────────────────────────────────────────
    // MARK AS READ
    // PUT /api/v1/notifications/{id}/read
    // ─────────────────────────────────────────
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<String>>
    markAsRead(
            @PathVariable String id) {

        boolean marked =
                notificationService.markAsRead(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        marked ? "SUCCESS" : "FAILED",
                        "Notification marked as read"));
    }

    // ─────────────────────────────────────────
    // SEND REMINDER (Minister Only)
    // POST /api/v1/notifications/reminder
    // ─────────────────────────────────────────
    @PostMapping("/reminder")
    public ResponseEntity<ApiResponse<String>>
    sendReminder(
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization")
            String authHeader) {

        String toUserId = body.get("toUserId");
        String toUserName = body.get("toUserName");
        String message = body.get("message");
        String complaintId = body.get("complaintId");

        notificationService.sendNotification(
                toUserId,
                toUserName,
                "🔔 Reminder from Minister",
                message,
                Notification.TYPE_REMINDER,
                complaintId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Reminder sent to: "
                        + toUserName));
    }
}