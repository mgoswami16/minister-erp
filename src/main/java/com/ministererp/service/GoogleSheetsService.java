package com.ministererp.service;

import com.ministererp.model.Appointment;
import com.ministererp.model.AuditLog;
import com.ministererp.model.Complaint;
import com.ministererp.model.ForwardingLog;
import com.ministererp.model.Notification;
import com.ministererp.model.User;
import com.ministererp.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleSheetsService {

    private final WebClient webClient;
    private final DateUtil dateUtil;

    @Value("${google.sheets.script.url}")
    private String scriptUrl;

    // ═══════════════════════════════════════════
    // USER OPERATIONS
    // ═══════════════════════════════════════════

    // ── Find user by username
    public User findUserByUsername(String username) {
        try {
            List<List<Object>> rows =
                    getSheetData("USERS");
            for (List<Object> row : rows) {
                if (row.size() > 5) {
                    String rowUsername =
                            getString(row, 5);
                    if (username.equals(rowUsername)) {
                        return mapRowToUser(row);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error finding user: "
                    + e.getMessage());
        }
        return null;
    }

    // ── Find user by ID
    public User findUserById(String userId) {
        try {
            List<List<Object>> rows =
                    getSheetData("USERS");
            for (List<Object> row : rows) {
                if (row.size() > 0) {
                    String rowId = getString(row, 0);
                    if (userId.equals(rowId)) {
                        return mapRowToUser(row);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error finding user by ID: "
                    + e.getMessage());
        }
        return null;
    }

    // ── Get all users
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            List<List<Object>> rows =
                    getSheetData("USERS");
            for (List<Object> row : rows) {
                if (row.size() > 0
                        && !getString(row, 0)
                                .isEmpty()) {
                    users.add(mapRowToUser(row));
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting users: "
                    + e.getMessage());
        }
        return users;
    }

    // ── Save new user
    public boolean saveUser(User user) {
        try {
            List<Object> row = buildUserRow(user);
            return appendToSheet("USERS", row);
        } catch (Exception e) {
            System.out.println(
                    "Error saving user: "
                    + e.getMessage());
            return false;
        }
    }

    // ── Update user
    public boolean updateUser(User user) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "UPDATE");
            params.put("sheet", "USERS");
            params.put("id", user.getUserId());
            params.put("idColumn", 0);
            params.put("data", buildUserRow(user));
            return callScript(params);
        } catch (Exception e) {
            System.out.println(
                    "Error updating user: "
                    + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════
    // COMPLAINT OPERATIONS
    // ═══════════════════════════════════════════

    // ── Get all complaints
    public List<Complaint> getAllComplaints() {
        List<Complaint> complaints = new ArrayList<>();
        try {
            List<List<Object>> rows =
                    getSheetData("COMPLAINTS");
            for (List<Object> row : rows) {
                if (row.size() > 0
                        && !getString(row, 0)
                                .isEmpty()) {
                    complaints.add(
                            mapRowToComplaint(row));
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting complaints: "
                    + e.getMessage());
        }
        return complaints;
    }

    // ── Get complaint by ID
    public Complaint getComplaintById(
            String complaintId) {
        try {
            List<List<Object>> rows =
                    getSheetData("COMPLAINTS");
            for (List<Object> row : rows) {
                if (row.size() > 0
                        && complaintId.equals(
                                getString(row, 0))) {
                    return mapRowToComplaint(row);
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting complaint: "
                    + e.getMessage());
        }
        return null;
    }

    // ── Get complaints by officer
    public List<Complaint> getComplaintsByOfficer(
            String officerId) {
        List<Complaint> complaints = new ArrayList<>();
        try {
            List<List<Object>> rows =
                    getSheetData("COMPLAINTS");
            for (List<Object> row : rows) {
                if (row.size() > 12) {
                    String assignedTo =
                            getString(row, 12);
                    if (officerId.equals(assignedTo)) {
                        complaints.add(
                                mapRowToComplaint(row));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting officer complaints: "
                    + e.getMessage());
        }
        return complaints;
    }

    // ── Get complaints by status
    public List<Complaint> getComplaintsByStatus(
            String status) {
        List<Complaint> complaints = new ArrayList<>();
        try {
            List<List<Object>> rows =
                    getSheetData("COMPLAINTS");
            for (List<Object> row : rows) {
                if (row.size() > 11) {
                    String rowStatus =
                            getString(row, 11);
                    if (status.equals(rowStatus)) {
                        complaints.add(
                                mapRowToComplaint(row));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting complaints by status: "
                    + e.getMessage());
        }
        return complaints;
    }

    // ── Save complaint
    public boolean saveComplaint(Complaint complaint) {
        try {
            List<Object> row =
                    buildComplaintRow(complaint);
            return appendToSheet("COMPLAINTS", row);
        } catch (Exception e) {
            System.out.println(
                    "Error saving complaint: "
                    + e.getMessage());
            return false;
        }
    }

    // ── Update complaint
    public boolean updateComplaint(
            Complaint complaint) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "UPDATE");
            params.put("sheet", "COMPLAINTS");
            params.put("id",
                    complaint.getComplaintId());
            params.put("idColumn", 0);
            params.put("data",
                    buildComplaintRow(complaint));
            return callScript(params);
        } catch (Exception e) {
            System.out.println(
                    "Error updating complaint: "
                    + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════
    // APPOINTMENT OPERATIONS
    // ═══════════════════════════════════════════

    // ── Get all appointments
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments =
                new ArrayList<>();
        try {
            List<List<Object>> rows =
                    getSheetData("APPOINTMENTS");
            for (List<Object> row : rows) {
                if (row.size() > 0
                        && !getString(row, 0)
                                .isEmpty()) {
                    appointments.add(
                            mapRowToAppointment(row));
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting appointments: "
                    + e.getMessage());
        }
        return appointments;
    }

    // ── Get appointment by ID
    public Appointment getAppointmentById(
            String appointmentId) {
        try {
            List<List<Object>> rows =
                    getSheetData("APPOINTMENTS");
            for (List<Object> row : rows) {
                if (row.size() > 0
                        && appointmentId.equals(
                                getString(row, 0))) {
                    return mapRowToAppointment(row);
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting appointment: "
                    + e.getMessage());
        }
        return null;
    }

    // ── Save appointment
    public boolean saveAppointment(
            Appointment appointment) {
        try {
            List<Object> row =
                    buildAppointmentRow(appointment);
            return appendToSheet("APPOINTMENTS", row);
        } catch (Exception e) {
            System.out.println(
                    "Error saving appointment: "
                    + e.getMessage());
            return false;
        }
    }

    // ── Update appointment
    public boolean updateAppointment(
            Appointment appointment) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "UPDATE");
            params.put("sheet", "APPOINTMENTS");
            params.put("id",
                    appointment.getAppointmentId());
            params.put("idColumn", 0);
            params.put("data",
                    buildAppointmentRow(appointment));
            return callScript(params);
        } catch (Exception e) {
            System.out.println(
                    "Error updating appointment: "
                    + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════
    // AUDIT LOG OPERATIONS
    // ═══════════════════════════════════════════

    // ── Save audit log
    public boolean saveAuditLog(AuditLog auditLog) {
        try {
            List<Object> row = new ArrayList<>();
            row.add(auditLog.getAuditId());
            row.add(auditLog.getComplaintId());
            row.add(auditLog.getUserId());
            row.add(auditLog.getUserName());
            row.add(auditLog.getUserRole());
            row.add(auditLog.getAction());
            row.add(auditLog.getPreviousStatus());
            row.add(auditLog.getNewStatus());
            row.add(auditLog.getRemarks());
            row.add(dateUtil.formatForSheet(
                    auditLog.getTimestamp()));
            row.add(auditLog.getIpAddress());
            return appendToSheet("AUDIT", row);
        } catch (Exception e) {
            System.out.println(
                    "Error saving audit: "
                    + e.getMessage());
            return false;
        }
    }

    // ── Get audit logs by complaint ID
    public List<AuditLog> getAuditByComplaintId(
            String complaintId) {
        List<AuditLog> logs = new ArrayList<>();
        try {
            List<List<Object>> rows =
                    getSheetData("AUDIT");
            for (List<Object> row : rows) {
                if (row.size() > 1
                        && complaintId.equals(
                                getString(row, 1))) {
                    logs.add(mapRowToAuditLog(row));
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting audit logs: "
                    + e.getMessage());
        }
        return logs;
    }

    // ═══════════════════════════════════════════
    // NOTIFICATION OPERATIONS
    // ═══════════════════════════════════════════

    // ── Save notification
    public boolean saveNotification(
            Notification notification) {
        try {
            List<Object> row = new ArrayList<>();
            row.add(notification.getNotificationId());
            row.add(notification.getUserId());
            row.add(notification.getUserName());
            row.add(notification.getTitle());
            row.add(notification.getMessage());
            row.add(notification.getType());
            row.add(notification.getComplaintId());
            row.add(notification.isRead()
                    ? "TRUE" : "FALSE");
            row.add(dateUtil.formatForSheet(
                    notification.getCreatedAt()));
            return appendToSheet(
                    "NOTIFICATIONS", row);
        } catch (Exception e) {
            System.out.println(
                    "Error saving notification: "
                    + e.getMessage());
            return false;
        }
    }

    // ── Get notifications by user
    public List<Notification> getNotificationsByUser(
            String userId) {
        List<Notification> notifications =
                new ArrayList<>();
        try {
            List<List<Object>> rows =
                    getSheetData("NOTIFICATIONS");
            for (List<Object> row : rows) {
                if (row.size() > 1
                        && userId.equals(
                                getString(row, 1))) {
                    notifications.add(
                            mapRowToNotification(row));
                }
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting notifications: "
                    + e.getMessage());
        }
        return notifications;
    }

    // ── Mark notification as read
    public boolean markNotificationRead(
            String notificationId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "MARK_READ");
            params.put("sheet", "NOTIFICATIONS");
            params.put("id", notificationId);
            return callScript(params);
        } catch (Exception e) {
            System.out.println(
                    "Error marking notification: "
                    + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════
    // FORWARDING LOG OPERATIONS
    // ═══════════════════════════════════════════

    // ── Save forwarding log
    public boolean saveForwardingLog(
            ForwardingLog log) {
        try {
            List<Object> row = new ArrayList<>();
            row.add(log.getForwardId());
            row.add(log.getComplaintId());
            row.add(log.getFromUserId());
            row.add(log.getFromUserName());
            row.add(log.getFromUserRole());
            row.add(log.getToUserId());
            row.add(log.getToUserName());
            row.add(log.getToUserRole());
            row.add(log.getRemarks());
            row.add(dateUtil.formatForSheet(
                    log.getForwardedAt()));
            row.add(log.getForwardType());
            return appendToSheet("FORWARDING", row);
            } catch (Exception e) {
            System.out.println(
                    "Error saving forwarding log: "
                    + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════
    // GOOGLE SHEETS API CORE METHODS
    // ═══════════════════════════════════════════

    // ── Get all data from a sheet
    @SuppressWarnings("unchecked")
    private List<List<Object>> getSheetData(
            String sheetName) {
        try {
            String url = scriptUrl
                    + "?action=GET"
                    + "&sheet=" + sheetName;

            Map<String, Object> response =
                    webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            if (response != null
                    && response.containsKey("data")) {
                return (List<List<Object>>)
                        response.get("data");
            }
        } catch (Exception e) {
            System.out.println(
                    "Error getting sheet data ["
                    + sheetName + "]: "
                    + e.getMessage());
        }
        return new ArrayList<>();
    }

    // ── Append row to sheet
    @SuppressWarnings("unchecked")
    private boolean appendToSheet(
            String sheetName,
            List<Object> row) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("action", "APPEND");
            payload.put("sheet", sheetName);
            payload.put("data", row);

            Map<String, Object> response =
                    webClient.post()
                            .uri(scriptUrl)
                            .bodyValue(payload)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            return response != null
                    && Boolean.TRUE.equals(
                            response.get("success"));

        } catch (Exception e) {
            System.out.println(
                    "Error appending to sheet ["
                    + sheetName + "]: "
                    + e.getMessage());
            return false;
        }
    }

    // ── Call Google Apps Script
    @SuppressWarnings("unchecked")
    private boolean callScript(
            Map<String, Object> params) {
        try {
            Map<String, Object> response =
                    webClient.post()
                            .uri(scriptUrl)
                            .bodyValue(params)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();

            return response != null
                    && Boolean.TRUE.equals(
                            response.get("success"));

        } catch (Exception e) {
            System.out.println(
                    "Error calling script: "
                    + e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════════
    // ROW BUILDERS
    // ═══════════════════════════════════════════

    // ── Build User Row
    private List<Object> buildUserRow(User user) {
        List<Object> row = new ArrayList<>();
        row.add(user.getUserId());
        row.add(user.getName());
        row.add(user.getRole());
        row.add(user.getMobile());
        row.add(user.getEmail() != null
                ? user.getEmail() : "");
        row.add(user.getUsername());
        row.add(user.getPasswordHash());
        row.add(user.getDepartment() != null
                ? user.getDepartment() : "");
        row.add(user.isActive() ? "TRUE" : "FALSE");
        row.add(dateUtil.formatForSheet(
                user.getCreatedAt()));
        row.add(user.getLastLogin() != null
                ? dateUtil.formatForSheet(
                        user.getLastLogin()) : "");
        row.add(user.getCreatedBy() != null
                ? user.getCreatedBy() : "");
        return row;
    }

    // ── Build Complaint Row
    private List<Object> buildComplaintRow(
            Complaint complaint) {
        List<Object> row = new ArrayList<>();
        row.add(complaint.getComplaintId());
        row.add(dateUtil.formatForSheet(
                complaint.getCreatedDate()));
        row.add(complaint.getCallerName());
        row.add(complaint.getCallerMobile());
        row.add(complaint.getCallerAddress() != null
                ? complaint.getCallerAddress() : "");
        row.add(complaint.getDistrict());
        row.add(complaint.getState() != null
                ? complaint.getState() : "");
        row.add(complaint.getSubject());
        row.add(complaint.getDescription());
        row.add(complaint.getCategory());
        row.add(complaint.getPriority());
        row.add(complaint.getStatus());
        row.add(complaint.getAssignedTo() != null
                ? complaint.getAssignedTo() : "");
        row.add(complaint.getAssignedName() != null
                ? complaint.getAssignedName() : "");
        row.add(complaint.getAssignedDate() != null
                ? dateUtil.formatForSheet(
                        complaint.getAssignedDate()) : "");
        row.add(complaint.getOperatorId() != null
                ? complaint.getOperatorId() : "");
        row.add(complaint.getOperatorName() != null
                ? complaint.getOperatorName() : "");
        row.add(complaint.getLastUpdated() != null
                ? dateUtil.formatForSheet(
                        complaint.getLastUpdated()) : "");
        row.add(complaint.getResolvedDate() != null
                ? dateUtil.formatForSheet(
                        complaint.getResolvedDate()) : "");
        row.add(complaint.getClosedDate() != null
                ? dateUtil.formatForSheet(
                        complaint.getClosedDate()) : "");
        row.add(complaint.getMinisterNote() != null
                ? complaint.getMinisterNote() : "");
        row.add(complaint.getMinisterDirection() != null
                ? complaint.getMinisterDirection() : "");
        row.add(complaint.getActionTaken() != null
                ? complaint.getActionTaken() : "");
        row.add(complaint.getClosureRemark() != null
                ? complaint.getClosureRemark() : "");
        row.add(complaint.getEscalationRemark() != null
                ? complaint.getEscalationRemark() : "");
        return row;
    }

    // ── Build Appointment Row
    private List<Object> buildAppointmentRow(
            Appointment appointment) {
        List<Object> row = new ArrayList<>();
        row.add(appointment.getAppointmentId());
        row.add(dateUtil.formatForSheet(
                appointment.getCreatedDate()));
        row.add(appointment.getVisitorName());
        row.add(appointment.getVisitorMobile());
        row.add(appointment.getVisitorAddress() != null
                ? appointment.getVisitorAddress() : "");
        row.add(appointment.getPurpose());
        row.add(appointment.getPreferredDate() != null
                ? appointment.getPreferredDate() : "");
        row.add(appointment.getPreferredTime() != null
                ? appointment.getPreferredTime() : "");
        row.add(appointment.getMeetingLocation() != null
                ? appointment.getMeetingLocation() : "");
        row.add(appointment.getStatus());
        row.add(appointment.getApprovedDate() != null
                ? appointment.getApprovedDate() : "");
        row.add(appointment.getApprovedTime() != null
                ? appointment.getApprovedTime() : "");
        row.add(appointment.getApprovedVenue() != null
                ? appointment.getApprovedVenue() : "");
        row.add(appointment.getApprovedBy() != null
                ? appointment.getApprovedBy() : "");
        row.add(appointment.getApprovedAt() != null
                ? dateUtil.formatForSheet(
                        appointment.getApprovedAt()) : "");
        row.add(appointment.getRejectionReason() != null
                ? appointment.getRejectionReason() : "");
        row.add(appointment.getOperatorId() != null
                ? appointment.getOperatorId() : "");
        row.add(appointment.getOperatorName() != null
                ? appointment.getOperatorName() : "");
        row.add(appointment.getLastUpdated() != null
                ? dateUtil.formatForSheet(
                        appointment.getLastUpdated()) : "");
        row.add(appointment.getRemarks() != null
                ? appointment.getRemarks() : "");
        return row;
    }

    // ═══════════════════════════════════════════
    // ROW MAPPERS
    // ═══════════════════════════════════════════

    // ── Map Row to User
    private User mapRowToUser(List<Object> row) {
        return User.builder()
                .userId(getString(row, 0))
                .name(getString(row, 1))
                .role(getString(row, 2))
                .mobile(getString(row, 3))
                .email(getString(row, 4))
                .username(getString(row, 5))
                .passwordHash(getString(row, 6))
                .department(getString(row, 7))
                .isActive("TRUE".equals(
                        getString(row, 8)))
                .createdAt(dateUtil.parseFromSheet(
                        getString(row, 9)))
                .lastLogin(dateUtil.parseFromSheet(
                        getString(row, 10)))
                .createdBy(getString(row, 11))
                .build();
    }

    // ── Map Row to Complaint
    private Complaint mapRowToComplaint(
            List<Object> row) {
        return Complaint.builder()
                .complaintId(getString(row, 0))
                .createdDate(dateUtil.parseFromSheet(
                        getString(row, 1)))
                .callerName(getString(row, 2))
                .callerMobile(getString(row, 3))
                .callerAddress(getString(row, 4))
                .district(getString(row, 5))
                .state(getString(row, 6))
                .subject(getString(row, 7))
                .description(getString(row, 8))
                .category(getString(row, 9))
                .priority(getString(row, 10))
                .status(getString(row, 11))
                .assignedTo(getString(row, 12))
                .assignedName(getString(row, 13))
                .assignedDate(dateUtil.parseFromSheet(
                        getString(row, 14)))
                .operatorId(getString(row, 15))
                .operatorName(getString(row, 16))
                .lastUpdated(dateUtil.parseFromSheet(
                        getString(row, 17)))
                .resolvedDate(dateUtil.parseFromSheet(
                        getString(row, 18)))
                .closedDate(dateUtil.parseFromSheet(
                        getString(row, 19)))
                .ministerNote(getString(row, 20))
                .ministerDirection(getString(row, 21))
                .actionTaken(getString(row, 22))
                .closureRemark(getString(row, 23))
                .escalationRemark(getString(row, 24))
                .build();
    }

    // ── Map Row to Appointment
    private Appointment mapRowToAppointment(
            List<Object> row) {
        return Appointment.builder()
                .appointmentId(getString(row, 0))
                .createdDate(dateUtil.parseFromSheet(
                        getString(row, 1)))
                .visitorName(getString(row, 2))
                .visitorMobile(getString(row, 3))
                .visitorAddress(getString(row, 4))
                .purpose(getString(row, 5))
                .preferredDate(getString(row, 6))
                .preferredTime(getString(row, 7))
                .meetingLocation(getString(row, 8))
                .status(getString(row, 9))
                .approvedDate(getString(row, 10))
                .approvedTime(getString(row, 11))
                .approvedVenue(getString(row, 12))
                .approvedBy(getString(row, 13))
                .approvedAt(dateUtil.parseFromSheet(
                        getString(row, 14)))
                .rejectionReason(getString(row, 15))
                .operatorId(getString(row, 16))
                .operatorName(getString(row, 17))
                .lastUpdated(dateUtil.parseFromSheet(
                        getString(row, 18)))
                .remarks(getString(row, 19))
                .build();
    }

    // ── Map Row to AuditLog
    private AuditLog mapRowToAuditLog(
            List<Object> row) {
        return AuditLog.builder()
                .auditId(getString(row, 0))
                .complaintId(getString(row, 1))
                .userId(getString(row, 2))
                .userName(getString(row, 3))
                .userRole(getString(row, 4))
                .action(getString(row, 5))
                .previousStatus(getString(row, 6))
                .newStatus(getString(row, 7))
                .remarks(getString(row, 8))
                .timestamp(dateUtil.parseFromSheet(
                        getString(row, 9)))
                .ipAddress(getString(row, 10))
                .build();
    }

    // ── Map Row to Notification
    private Notification mapRowToNotification(
            List<Object> row) {
        return Notification.builder()
                .notificationId(getString(row, 0))
                .userId(getString(row, 1))
                .userName(getString(row, 2))
                .title(getString(row, 3))
                .message(getString(row, 4))
                .type(getString(row, 5))
                .complaintId(getString(row, 6))
                .isRead("TRUE".equals(
                        getString(row, 7)))
                .createdAt(dateUtil.parseFromSheet(
                        getString(row, 8)))
                .build();
    }

    // ═══════════════════════════════════════════
    // UTILITY HELPER
    // ═══════════════════════════════════════════

    // ── Safely get string from row
    private String getString(
            List<Object> row,
            int index) {
        try {
            if (row != null
                    && index < row.size()
                    && row.get(index) != null) {
                return row.get(index)
                        .toString()
                        .trim();
            }
        } catch (Exception e) {
            // Return empty string on error
        }
        return "";
    }
}