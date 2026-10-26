package com.ministererp.service;

import com.ministererp.model.AuditLog;
import com.ministererp.util.DateUtil;
import com.ministererp.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final GoogleSheetsService googleSheetsService;
    private final IdGenerator idGenerator;
    private final DateUtil dateUtil;

    // ─────────────────────────────────────────
    // LOG ANY ACTION
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void logAction(
            String userId,
            String userName,
            String userRole,
            String action,
            String complaintId,
            String previousStatus,
            String newStatus,
            String remarks,
            String ipAddress) {

        try {
            AuditLog log = AuditLog.builder()
                    .auditId(idGenerator.generateAuditId())
                    .complaintId(complaintId)
                    .userId(userId)
                    .userName(userName)
                    .userRole(userRole)
                    .action(action)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .remarks(remarks)
                    .timestamp(dateUtil.now())
                    .ipAddress(ipAddress)
                    .build();

            googleSheetsService.saveAuditLog(log);

        } catch (Exception e) {
            System.out.println(
                    "Error saving audit log: "
                    + e.getMessage());
        }
    }

    // ─────────────────────────────────────────
    // GET AUDIT LOGS BY COMPLAINT
    // ─────────────────────────────────────────
    public List<AuditLog> getAuditByComplaint(
            String complaintId) {
        return googleSheetsService
                .getAuditByComplaintId(complaintId);
    }

    // ─────────────────────────────────────────
    // LOG COMPLAINT CREATED
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void logComplaintCreated(
            String userId,
            String userName,
            String role,
            String complaintId) {

        logAction(
                userId,
                userName,
                role,
                AuditLog.ACTION_CREATED,
                complaintId,
                null,
                "NEW",
                "Complaint registered",
                null
        );
    }

    // ─────────────────────────────────────────
    // LOG COMPLAINT ASSIGNED
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void logComplaintAssigned(
            String userId,
            String userName,
            String role,
            String complaintId,
            String officerName) {

        logAction(
                userId,
                userName,
                role,
                AuditLog.ACTION_ASSIGNED,
                complaintId,
                "NEW",
                "ASSIGNED",
                "Assigned to: " + officerName,
                null
        );
    }

    // ─────────────────────────────────────────
    // LOG STATUS UPDATED
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void logStatusUpdated(
            String userId,
            String userName,
            String role,
            String complaintId,
            String previousStatus,
            String newStatus,
            String remarks) {

        logAction(
                userId,
                userName,
                role,
                AuditLog.ACTION_UPDATED,
                complaintId,
                previousStatus,
                newStatus,
                remarks,
                null
        );
    }

    // ─────────────────────────────────────────
    // LOG ESCALATION
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void logEscalation(
            String userId,
            String userName,
            String role,
            String complaintId,
            String remarks) {

        logAction(
                userId,
                userName,
                role,
                AuditLog.ACTION_ESCALATED,
                complaintId,
                "PENDING",
                "ESCALATED",
                remarks,
                null
        );
    }

    // ─────────────────────────────────────────
    // LOG FORWARDING
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void logForwarding(
            String userId,
            String userName,
            String role,
            String complaintId,
            String toOfficerName,
            String remarks) {

        logAction(
                userId,
                userName,
                role,
                AuditLog.ACTION_FORWARDED,
                complaintId,
                "ASSIGNED",
                "ASSIGNED",
                "Forwarded to: " + toOfficerName
                + " | Remarks: " + remarks,
                null
        );
    }

    // ─────────────────────────────────────────
    // LOG MINISTER ACTION
    // ─────────────────────────────────────────
    @Async("taskExecutor")
    public void logMinisterAction(
            String userId,
            String userName,
            String complaintId,
            String action,
            String previousStatus,
            String newStatus,
            String remarks) {

        logAction(
                userId,
                userName,
                "MINISTER",
                action,
                complaintId,
                previousStatus,
                newStatus,
                remarks,
                null
        );
    }
}