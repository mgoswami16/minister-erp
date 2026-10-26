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
public class Appointment {

    private String appointmentId;
    private LocalDateTime createdDate;

    // ── Visitor Details
    private String visitorName;
    private String visitorMobile;
    private String visitorAddress;
    private String purpose;

    // ── Appointment Details
    private String preferredDate;
    private String preferredTime;
    private String meetingLocation;
    private String status;

    // ── Approved Details
    private String approvedDate;
    private String approvedTime;
    private String approvedVenue;
    private String approvedBy;
    private LocalDateTime approvedAt;

    // ── Rejection
    private String rejectionReason;

    // ── Operator Info
    private String operatorId;
    private String operatorName;

    private LocalDateTime lastUpdated;
    private String remarks;

    // ── Status Constants
    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
}
