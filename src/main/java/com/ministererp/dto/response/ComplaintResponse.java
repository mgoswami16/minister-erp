package com.ministererp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {

    private String complaintId;
    private LocalDateTime createdDate;

    // ── Caller
    private String callerName;
    private String callerMobile;
    private String callerAddress;
    private String district;
    private String state;

    // ── Complaint
    private String subject;
    private String description;
    private String category;
    private String priority;
    private String priorityLabel;
    private String status;
    private String statusLabel;

    // ── Assignment
    private String assignedTo;
    private String assignedName;
    private LocalDateTime assignedDate;

    // ── Operator
    private String operatorId;
    private String operatorName;

    // ── Timestamps
    private LocalDateTime lastUpdated;
    private LocalDateTime resolvedDate;
    private LocalDateTime closedDate;

    // ── Notes
    private String ministerNote;
    private String ministerDirection;
    private String actionTaken;
    private String closureRemark;
    private String escalationRemark;

    // ── Helper method for priority label
    public String getPriorityLabel() {
        if (priority == null) return "";
        return switch (priority) {
            case "LOW"      -> "🟢 Low";
            case "MEDIUM"   -> "🟡 Medium";
            case "HIGH"     -> "🟠 High";
            case "CRITICAL" -> "🔴 Critical";
            default         -> priority;
        };
    }

    // ── Helper method for status label
    public String getStatusLabel() {
        if (status == null) return "";
        return switch (status) {
            case "NEW"             -> "🆕 New";
            case "ASSIGNED"        -> "📋 Assigned";
            case "PENDING"         -> "⏳ Pending";
            case "ACTION_TAKEN"    -> "✅ Action Taken";
            case "ESCALATED"       -> "⚠️ Escalated";
            case "MINISTER_REVIEW" -> "👁️ Minister Review";
            case "RESOLVED"        -> "✔️ Resolved";
            case "CLOSED"          -> "🔒 Closed";
            default                -> status;
        };
    }
}