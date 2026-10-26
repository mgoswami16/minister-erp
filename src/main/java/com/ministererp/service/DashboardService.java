package com.ministererp.service;

import com.ministererp.dto.response.DashboardResponse;
import com.ministererp.model.Complaint;
import com.ministererp.model.User;
import com.ministererp.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final GoogleSheetsService googleSheetsService;
    private final DateUtil dateUtil;

    // ─────────────────────────────────────────
    // OPERATOR DASHBOARD
    // ─────────────────────────────────────────
    public DashboardResponse getOperatorDashboard(
            String operatorId) {

        List<Complaint> all =
                googleSheetsService.getAllComplaints();

        // Today's complaints by this operator
        long complaintsToday = all.stream()
                .filter(c -> operatorId.equals(
                        c.getOperatorId()))
                .filter(c -> dateUtil.isToday(
                        c.getCreatedDate()))
                .count();

        // Total pending
        long totalPending = all.stream()
                .filter(c -> Complaint.STATUS_PENDING
                        .equals(c.getStatus())
                        || Complaint.STATUS_NEW
                        .equals(c.getStatus()))
                .count();

        // Total escalated
        long totalEscalated = all.stream()
                .filter(c -> Complaint.STATUS_ESCALATED
                        .equals(c.getStatus()))
                .count();

        // Total resolved
        long totalResolved = all.stream()
                .filter(c -> Complaint.STATUS_RESOLVED
                        .equals(c.getStatus()))
                .count();

        // Total closed
        long totalClosed = all.stream()
                .filter(c -> Complaint.STATUS_CLOSED
                        .equals(c.getStatus()))
                .count();

        return DashboardResponse.builder()
                .complaintsToday((int) complaintsToday)
                .callsToday((int) complaintsToday)
                .totalPending((int) totalPending)
                .totalEscalated((int) totalEscalated)
                .totalResolved((int) totalResolved)
                .totalClosed((int) totalClosed)
                .totalComplaints(all.size())
                .build();
    }

    // ─────────────────────────────────────────
    // OFFICER DASHBOARD
    // ─────────────────────────────────────────
    public DashboardResponse getOfficerDashboard(
            String officerId) {

        List<Complaint> myComplaints =
                googleSheetsService
                        .getComplaintsByOfficer(officerId);

        long pending = myComplaints.stream()
                .filter(c -> Complaint.STATUS_PENDING
                        .equals(c.getStatus()))
                .count();

        long resolved = myComplaints.stream()
                .filter(c -> Complaint.STATUS_RESOLVED
                        .equals(c.getStatus()))
                .count();

        long escalated = myComplaints.stream()
                .filter(c -> Complaint.STATUS_ESCALATED
                        .equals(c.getStatus()))
                .count();

        long actionTaken = myComplaints.stream()
                .filter(c -> Complaint.STATUS_ACTION_TAKEN
                        .equals(c.getStatus()))
                .count();

        return DashboardResponse.builder()
                .assignedToMe(myComplaints.size())
                .myPending((int) pending)
                .myResolved((int) resolved)
                .myEscalated((int) escalated)
                .myActionTaken((int) actionTaken)
                .build();
    }

    // ─────────────────────────────────────────
    // MINISTER DASHBOARD
    // ─────────────────────────────────────────
    public DashboardResponse getMinisterDashboard() {

        List<Complaint> all =
                googleSheetsService.getAllComplaints();
        List<User> officers =
                googleSheetsService.getAllUsers()
                        .stream()
                        .filter(u -> User.ROLE_OFFICER
                                .equals(u.getRole()))
                        .collect(Collectors.toList());

        // Status counts
        long pending = all.stream()
                .filter(c -> Complaint.STATUS_PENDING
                        .equals(c.getStatus())
                        || Complaint.STATUS_NEW
                        .equals(c.getStatus())
                        || Complaint.STATUS_ASSIGNED
                        .equals(c.getStatus()))
                .count();

        long resolved = all.stream()
                .filter(c -> Complaint.STATUS_RESOLVED
                        .equals(c.getStatus()))
                .count();

        long escalated = all.stream()
                .filter(c -> Complaint.STATUS_ESCALATED
                        .equals(c.getStatus()))
                .count();

        long ministerReview = all.stream()
                .filter(c -> Complaint.STATUS_MINISTER_REVIEW
                        .equals(c.getStatus()))
                .count();

        long closed = all.stream()
                .filter(c -> Complaint.STATUS_CLOSED
                        .equals(c.getStatus()))
                .count();

        long critical = all.stream()
                .filter(c -> Complaint.PRIORITY_CRITICAL
                        .equals(c.getPriority()))
                .count();

        // Category wise
        Map<String, Integer> categoryWise =
                getCategoryWise(all);

        // District wise
        Map<String, Integer> districtWise =
                getDistrictWise(all);

        // Priority wise
        Map<String, Integer> priorityWise =
                getPriorityWise(all);

        // Status wise
        Map<String, Integer> statusWise =
                getStatusWise(all);

        // Monthly trend
        Map<String, Integer> monthlyTrend =
                getMonthlyTrend(all);

        // Officer performance
        List<DashboardResponse.OfficerStats> performance =
                getOfficerPerformance(officers, all);

        return DashboardResponse.builder()
                .totalCases(all.size())
                .pendingCases((int) pending)
                .resolvedCases((int) resolved)
                .escalatedCases((int) escalated)
                .ministerReviewCases((int) ministerReview)
                .closedCases((int) closed)
                .criticalCases((int) critical)
                .categoryWise(categoryWise)
                .districtWise(districtWise)
                .priorityWise(priorityWise)
                .statusWise(statusWise)
                .monthlyTrend(monthlyTrend)
                .officerPerformance(performance)
                .build();
    }
    
// ─────────────────────────────────────────
    // CATEGORY WISE COUNT
    // ─────────────────────────────────────────
    private Map<String, Integer> getCategoryWise(
            List<Complaint> complaints) {

        Map<String, Integer> map = new HashMap<>();
        for (Complaint c : complaints) {
            String cat = c.getCategory() != null
                    ? c.getCategory() : "GENERAL";
            map.put(cat, map.getOrDefault(cat, 0) + 1);
        }
        return map;
    }

    // ─────────────────────────────────────────
    // DISTRICT WISE COUNT
    // ─────────────────────────────────────────
    private Map<String, Integer> getDistrictWise(
            List<Complaint> complaints) {

        Map<String, Integer> map = new HashMap<>();
        for (Complaint c : complaints) {
            String dist = c.getDistrict() != null
                    ? c.getDistrict() : "UNKNOWN";
            map.put(dist,
                    map.getOrDefault(dist, 0) + 1);
        }
        return map;
    }

    // ─────────────────────────────────────────
    // PRIORITY WISE COUNT
    // ─────────────────────────────────────────
    private Map<String, Integer> getPriorityWise(
            List<Complaint> complaints) {

        Map<String, Integer> map = new HashMap<>();
        map.put("LOW", 0);
        map.put("MEDIUM", 0);
        map.put("HIGH", 0);
        map.put("CRITICAL", 0);

        for (Complaint c : complaints) {
            String pri = c.getPriority() != null
                    ? c.getPriority() : "LOW";
            map.put(pri,
                    map.getOrDefault(pri, 0) + 1);
        }
        return map;
    }

    // ─────────────────────────────────────────
    // STATUS WISE COUNT
    // ─────────────────────────────────────────
    private Map<String, Integer> getStatusWise(
            List<Complaint> complaints) {

        Map<String, Integer> map = new HashMap<>();
        map.put("NEW", 0);
        map.put("ASSIGNED", 0);
        map.put("PENDING", 0);
        map.put("ACTION_TAKEN", 0);
        map.put("ESCALATED", 0);
        map.put("MINISTER_REVIEW", 0);
        map.put("RESOLVED", 0);
        map.put("CLOSED", 0);

        for (Complaint c : complaints) {
            String status = c.getStatus() != null
                    ? c.getStatus() : "NEW";
            map.put(status,
                    map.getOrDefault(status, 0) + 1);
        }
        return map;
    }

    // ─────────────────────────────────────────
    // MONTHLY TREND
    // ─────────────────────────────────────────
    private Map<String, Integer> getMonthlyTrend(
            List<Complaint> complaints) {

        Map<String, Integer> map = new HashMap<>();
        for (Complaint c : complaints) {
            if (c.getCreatedDate() != null) {
                String monthYear = dateUtil
                        .formatMonthYear(
                                c.getCreatedDate());
                map.put(monthYear,
                        map.getOrDefault(
                                monthYear, 0) + 1);
            }
        }
        return map;
    }

    // ─────────────────────────────────────────
    // OFFICER PERFORMANCE
    // ─────────────────────────────────────────
    private List<DashboardResponse.OfficerStats>
    getOfficerPerformance(
            List<User> officers,
            List<Complaint> allComplaints) {

        List<DashboardResponse.OfficerStats> stats =
                new ArrayList<>();

        for (User officer : officers) {
            List<Complaint> officerComplaints =
                    allComplaints.stream()
                            .filter(c -> officer
                                    .getUserId()
                                    .equals(c
                                    .getAssignedTo()))
                            .collect(Collectors.toList());

            int total = officerComplaints.size();

            int resolved = (int) officerComplaints
                    .stream()
                    .filter(c ->
                            Complaint.STATUS_RESOLVED
                            .equals(c.getStatus())
                            || Complaint.STATUS_CLOSED
                            .equals(c.getStatus()))
                    .count();

            int pending = (int) officerComplaints
                    .stream()
                    .filter(c ->
                            Complaint.STATUS_PENDING
                            .equals(c.getStatus())
                            || Complaint.STATUS_ASSIGNED
                            .equals(c.getStatus())
                            || Complaint.STATUS_NEW
                            .equals(c.getStatus()))
                    .count();

            int escalated = (int) officerComplaints
                    .stream()
                    .filter(c ->
                            Complaint.STATUS_ESCALATED
                            .equals(c.getStatus()))
                    .count();

            double resolutionRate = total > 0
                    ? ((double) resolved / total) * 100
                    : 0.0;

            stats.add(
                DashboardResponse.OfficerStats.builder()
                    .officerId(officer.getUserId())
                    .officerName(officer.getName())
                    .department(officer.getDepartment())
                    .totalAssigned(total)
                    .resolved(resolved)
                    .pending(pending)
                    .escalated(escalated)
                    .resolutionRate(Math.round(
                            resolutionRate * 100.0)
                            / 100.0)
                    .build()
            );
        }

        return stats;
    }
}