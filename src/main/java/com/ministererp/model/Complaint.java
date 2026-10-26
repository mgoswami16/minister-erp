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
public class Complaint {

    private String complaintId;
    private LocalDateTime createdDate;

    // ── Caller Details
    private String callerName;
    private String callerMobile;
    private String callerAddress;
    private String district;
    private String state;

    // ── Complaint Details
    private String subject;
    private String description;
    private String category;
    private String priority;
    private String status;

    // ── Assignment
    private String assignedTo;
    private String assignedName;
    private LocalDateTime assignedDate;

    // ── Operator Info
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

    // ── Priority Constants
    public static final String PRIORITY_LOW      = "LOW";
    public static final String PRIORITY_MEDIUM   = "MEDIUM";
    public static final String PRIORITY_HIGH     = "HIGH";
    public static final String PRIORITY_CRITICAL = "CRITICAL";

    // ── Status Constants
    public static final String STATUS_NEW             = "NEW";
    public static final String STATUS_ASSIGNED        = "ASSIGNED";
    public static final String STATUS_PENDING         = "PENDING";
    public static final String STATUS_ACTION_TAKEN    = "ACTION_TAKEN";
    public static final String STATUS_ESCALATED       = "ESCALATED";
    public static final String STATUS_MINISTER_REVIEW = "MINISTER_REVIEW";
    public static final String STATUS_RESOLVED        = "RESOLVED";
    public static final String STATUS_CLOSED          = "CLOSED";

    // ── Category Constants
    public static final String CAT_KANNAUJ     = "KANNAUJ";
    public static final String CAT_POLICE      = "POLICE";
    public static final String CAT_APPOINTMENT = "APPOINTMENT";
    public static final String CAT_BJP         = "BJP";
    public static final String CAT_WESTERN_UP  = "WESTERN_UP";
    public static final String CAT_GENERAL     = "GENERAL";
}