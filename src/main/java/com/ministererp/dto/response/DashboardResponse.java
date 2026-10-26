package com.ministererp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    // ── Operator Dashboard
    private int callsToday;
    private int complaintsToday;
    private int totalPending;
    private int totalEscalated;
    private int totalResolved;
    private int totalClosed;
    private int totalComplaints;

    // ── Officer Dashboard
    private int assignedToMe;
    private int myPending;
    private int myResolved;
    private int myEscalated;
    private int myActionTaken;

    // ── Minister Dashboard
    private int totalCases;
    private int pendingCases;
    private int resolvedCases;
    private int escalatedCases;
    private int ministerReviewCases;
    private int closedCases;
    private int criticalCases;

    // ── Analytics
    private Map<String, Integer> categoryWise;
    private Map<String, Integer> districtWise;
    private Map<String, Integer> priorityWise;
    private Map<String, Integer> statusWise;
    private Map<String, Integer> monthlyTrend;

    // ── Officer Performance
    private List<OfficerStats> officerPerformance;

    // ── Recent Complaints
    private List<ComplaintResponse> recentComplaints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OfficerStats {
        private String officerId;
        private String officerName;
        private String department;
        private int totalAssigned;
        private int resolved;
        private int pending;
        private int escalated;
        private double resolutionRate;
    }
}