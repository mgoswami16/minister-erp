package com.ministererp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    private String notificationId;
    private String userId;
    private String userName;
    private String title;
    private String message;
    private String type;
    private String complaintId;
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // ── Type Constants
    public static final String TYPE_ASSIGNMENT  = "ASSIGNMENT";
    public static final String TYPE_ESCALATION  = "ESCALATION";
    public static final String TYPE_FORWARDING  = "FORWARDING";
    public static final String TYPE_REMINDER    = "REMINDER";
    public static final String TYPE_DIRECTION   = "DIRECTION";
    public static final String TYPE_APPOINTMENT = "APPOINTMENT";
    public static final String TYPE_RESOLVED    = "RESOLVED";
}