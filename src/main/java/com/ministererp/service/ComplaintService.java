package com.ministererp.service;

import com.ministererp.dto.request.ComplaintRequest;
import com.ministererp.dto.response.ComplaintResponse;
import com.ministererp.exception.ResourceNotFoundException;
import com.ministererp.model.Complaint;
import com.ministererp.model.ForwardingLog;
import com.ministererp.model.User;
import com.ministererp.util.DateUtil;
import com.ministererp.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final GoogleSheetsService googleSheetsService;
    private final IdGenerator idGenerator;
    private final DateUtil dateUtil;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final UserService userService;

    // ─────────────────────────────────────────
    // CREATE COMPLAINT
    // ─────────────────────────────────────────
    public Complaint createComplaint(
            ComplaintRequest request,
            String operatorId,
            String operatorName) {

        // Build complaint object
        Complaint complaint = Complaint.builder()
                .complaintId(
                        idGenerator.generateComplaintId())
                .createdDate(dateUtil.now())
                .callerName(request.getCallerName())
                .callerMobile(request.getCallerMobile())
                .callerAddress(request.getCallerAddress())
                .district(request.getDistrict())
                .state(request.getState())
                .subject(request.getSubject())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .status(Complaint.STATUS_NEW)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .lastUpdated(dateUtil.now())
                .build();

        // Auto assign officer if not provided
        if (request.getAssignedTo() != null
                && !request.getAssignedTo().isBlank()) {
            complaint.setAssignedTo(
                    request.getAssignedTo());
            complaint.setAssignedName(
                    request.getAssignedName());
            complaint.setStatus(Complaint.STATUS_ASSIGNED);
            complaint.setAssignedDate(dateUtil.now());
        } else {
            // Try auto assignment
            User suggested = userService
                    .suggestOfficerByCategory(
                            request.getCategory(),
                            request.getDistrict());
            if (suggested != null) {
                complaint.setAssignedTo(
                        suggested.getUserId());
                complaint.setAssignedName(
                        suggested.getName());
                complaint.setStatus(
                        Complaint.STATUS_ASSIGNED);
                complaint.setAssignedDate(dateUtil.now());
            }
        }

        // Save to Google Sheets
        boolean saved = googleSheetsService
                .saveComplaint(complaint);
        if (!saved) {
            throw new RuntimeException(
                    "Failed to save complaint");
        }

        // Log audit
        auditService.logComplaintCreated(
                operatorId,
                operatorName,
                User.ROLE_OPERATOR,
                complaint.getComplaintId()
        );

        // Send notification to assigned officer
        if (complaint.getAssignedTo() != null) {
            notificationService.notifyAssignment(
                    complaint.getAssignedTo(),
                    complaint.getAssignedName(),
                    complaint.getComplaintId(),
                    complaint.getSubject()
            );
        }

        return complaint;
    }

    // ─────────────────────────────────────────
    // GET ALL COMPLAINTS
    // ─────────────────────────────────────────
    public List<Complaint> getAllComplaints() {
        return googleSheetsService.getAllComplaints();
    }

    // ─────────────────────────────────────────
    // GET COMPLAINT BY ID
    // ─────────────────────────────────────────
    public Complaint getComplaintById(
            String complaintId) {
        Complaint complaint = googleSheetsService
                .getComplaintById(complaintId);
        if (complaint == null) {
            throw new ResourceNotFoundException(
                    "Complaint", "id", complaintId);
        }
        return complaint;
    }

    // ─────────────────────────────────────────
    // GET COMPLAINTS BY OFFICER
    // ─────────────────────────────────────────
    public List<Complaint> getComplaintsByOfficer(
            String officerId) {
        return googleSheetsService
                .getComplaintsByOfficer(officerId);
    }

    // ─────────────────────────────────────────
    // UPDATE COMPLAINT STATUS
    // ─────────────────────────────────────────
    public Complaint updateStatus(
            String complaintId,
            String newStatus,
            String actionTaken,
            String userId,
            String userName,
            String userRole) {

        Complaint complaint =
                getComplaintById(complaintId);
        String previousStatus = complaint.getStatus();

        complaint.setStatus(newStatus);
        complaint.setActionTaken(actionTaken);
        complaint.setLastUpdated(dateUtil.now());

        if (newStatus.equals(Complaint.STATUS_RESOLVED)) {
            complaint.setResolvedDate(dateUtil.now());
        }
        if (newStatus.equals(Complaint.STATUS_CLOSED)) {
            complaint.setClosedDate(dateUtil.now());
        }

        googleSheetsService.updateComplaint(complaint);

        // Log audit
        auditService.logStatusUpdated(
                userId,
                userName,
                userRole,
                complaintId,
                previousStatus,
                newStatus,
                actionTaken
        );

        return complaint;
    }

    // ─────────────────────────────────────────
    // ASSIGN COMPLAINT
    // ─────────────────────────────────────────
    public Complaint assignComplaint(
            String complaintId,
            String officerId,
            String officerName,
            String assignedByUserId,
            String assignedByName,
            String assignedByRole) {

        Complaint complaint =
                getComplaintById(complaintId);

        complaint.setAssignedTo(officerId);
        complaint.setAssignedName(officerName);
        complaint.setStatus(Complaint.STATUS_ASSIGNED);
        complaint.setAssignedDate(dateUtil.now());
        complaint.setLastUpdated(dateUtil.now());

        googleSheetsService.updateComplaint(complaint);

        // Log audit
        auditService.logComplaintAssigned(
                assignedByUserId,
                assignedByName,
                assignedByRole,
                complaintId,
                officerName
        );

        // Notify officer
        notificationService.notifyAssignment(
                officerId,
                officerName,
                complaintId,
                complaint.getSubject()
        );

        return complaint;
    }

    // ─────────────────────────────────────────
    // FORWARD COMPLAINT
    // ─────────────────────────────────────────
    public Complaint forwardComplaint(
            String complaintId,
            String toOfficerId,
            String toOfficerName,
            String fromUserId,
            String fromUserName,
            String fromUserRole,
            String remarks) {

        Complaint complaint =
                getComplaintById(complaintId);

        // Save forwarding log
        ForwardingLog log = ForwardingLog.builder()
                .forwardId(
                        idGenerator.generateForwardId())
                .complaintId(complaintId)
                .fromUserId(fromUserId)
                .fromUserName(fromUserName)
                .fromUserRole(fromUserRole)
                .toUserId(toOfficerId)
                .toUserName(toOfficerName)
                .remarks(remarks)
                .forwardedAt(dateUtil.now())
                .forwardType(ForwardingLog.TYPE_INTERNAL)
                .build();

        googleSheetsService.saveForwardingLog(log);

        // Update complaint assignment
        complaint.setAssignedTo(toOfficerId);
        complaint.setAssignedName(toOfficerName);
        complaint.setAssignedDate(dateUtil.now());
        complaint.setLastUpdated(dateUtil.now());

        googleSheetsService.updateComplaint(complaint);

        // Log audit
        auditService.logForwarding(
                fromUserId,
                fromUserName,
                fromUserRole,
                complaintId,
                toOfficerName,
                remarks
        );

        // Notify new officer
        notificationService.notifyForwarding(
                toOfficerId,
                toOfficerName,
                fromUserName,
                complaintId,
                remarks
        );

        return complaint;
    }

    // ─────────────────────────────────────────
    // ESCALATE COMPLAINT
    // ─────────────────────────────────────────
    public Complaint escalateComplaint(
            String complaintId,
            String remarks,
            String userId,
            String userName,
            String userRole) {

        Complaint complaint =
                getComplaintById(complaintId);
        String previousStatus = complaint.getStatus();

        complaint.setStatus(Complaint.STATUS_ESCALATED);
        complaint.setEscalationRemark(remarks);
        complaint.setLastUpdated(dateUtil.now());

        googleSheetsService.updateComplaint(complaint);

        // Log audit
        auditService.logEscalation(
                userId,
                userName,
                userRole,
                complaintId,
                remarks
        );

        return complaint;
    }

    // ─────────────────────────────────────────
    // SEND TO MINISTER REVIEW
    // ─────────────────────────────────────────
    public Complaint sendToMinisterReview(
            String complaintId,
            String remarks,
            String userId,
            String userName,
            String userRole) {

        Complaint complaint =
                getComplaintById(complaintId);
        String previousStatus = complaint.getStatus();

        complaint.setStatus(
                Complaint.STATUS_MINISTER_REVIEW);
        complaint.setLastUpdated(dateUtil.now());

        googleSheetsService.updateComplaint(complaint);

        // Log audit
        auditService.logStatusUpdated(
                userId,
                userName,
                userRole,
                complaintId,
                previousStatus,
                Complaint.STATUS_MINISTER_REVIEW,
                remarks
        );

        return complaint;
    }

    // ─────────────────────────────────────────
    // MINISTER ACTIONS
    // ─────────────────────────────────────────
    public Complaint ministerAction(
            String complaintId,
            String action,
            String direction,
            String note,
            String newOfficerId,
            String newOfficerName,
            String ministerId,
            String ministerName) {

        Complaint complaint =
                getComplaintById(complaintId);
        String previousStatus = complaint.getStatus();
        String newStatus = previousStatus;

        switch (action.toUpperCase()) {

            case "APPROVE":
                newStatus = Complaint.STATUS_ACTION_TAKEN;
                complaint.setMinisterNote(note);
                break;

            case "REJECT":
                newStatus = Complaint.STATUS_PENDING;
                complaint.setMinisterNote(note);
                break;

            case "GIVE_DIRECTION":
                complaint.setMinisterDirection(direction);
                complaint.setMinisterNote(note);
                // Notify assigned officer
                if (complaint.getAssignedTo() != null) {
                    notificationService
                            .notifyMinisterDirection(
                                    complaint.getAssignedTo(),
                                    complaint.getAssignedName(),
                                    complaintId,
                                    direction
                            );
                }
                break;

            case "REASSIGN":
                complaint.setAssignedTo(newOfficerId);
                complaint.setAssignedName(newOfficerName);
                complaint.setAssignedDate(dateUtil.now());
                newStatus = Complaint.STATUS_ASSIGNED;
                // Notify new officer
                notificationService.notifyAssignment(
                        newOfficerId,
                        newOfficerName,
                        complaintId,
                        complaint.getSubject()
                );
                break;

            case "CLOSE":
                newStatus = Complaint.STATUS_CLOSED;
                complaint.setClosureRemark(note);
                complaint.setClosedDate(dateUtil.now());
                break;

            case "RESOLVE":
                newStatus = Complaint.STATUS_RESOLVED;
                complaint.setResolvedDate(dateUtil.now());
                complaint.setMinisterNote(note);
                break;
        }

        complaint.setStatus(newStatus);
        complaint.setLastUpdated(dateUtil.now());

        googleSheetsService.updateComplaint(complaint);

        // Log minister action
        auditService.logMinisterAction(
                ministerId,
                ministerName,
                complaintId,
                "MINISTER_" + action.toUpperCase(),
                previousStatus,
                newStatus,
                direction != null ? direction : note
        );

        return complaint;
    }

    // ─────────────────────────────────────────
    // CLOSE COMPLAINT
    // ─────────────────────────────────────────
    public Complaint closeComplaint(
            String complaintId,
            String closureRemark,
            String userId,
            String userName,
            String userRole) {

        Complaint complaint =
                getComplaintById(complaintId);

        complaint.setStatus(Complaint.STATUS_CLOSED);
        complaint.setClosureRemark(closureRemark);
        complaint.setClosedDate(dateUtil.now());
        complaint.setLastUpdated(dateUtil.now());

        googleSheetsService.updateComplaint(complaint);

        auditService.logStatusUpdated(
                userId, userName, userRole,
                complaintId,
                complaint.getStatus(),
                Complaint.STATUS_CLOSED,
                closureRemark
        );

        return complaint;
    }

    // ─────────────────────────────────────────
    // SEARCH COMPLAINTS
    // ─────────────────────────────────────────
    public List<Complaint> searchComplaints(
            String keyword,
            String status,
            String priority,
            String category,
            String district) {

        return googleSheetsService
                .getAllComplaints()
                .stream()
                .filter(c -> {
                    boolean matches = true;

                    if (keyword != null
                            && !keyword.isBlank()) {
                        String kw = keyword.toLowerCase();
                        matches = c.getCallerName() != null
                                && c.getCallerName()
                                   .toLowerCase()
                                   .contains(kw)
                            || c.getSubject() != null
                                && c.getSubject()
                                   .toLowerCase()
                                   .contains(kw)
                            || c.getComplaintId() != null
                                && c.getComplaintId()
                                   .toLowerCase()
                                   .contains(kw)
                            || c.getCallerMobile() != null
                                && c.getCallerMobile()
                                   .contains(kw);
                    }

                    if (status != null
                            && !status.isBlank()) {
                        matches = matches
                                && status.equals(
                                        c.getStatus());
                    }

                    if (priority != null
                            && !priority.isBlank()) {
                        matches = matches
                                && priority.equals(
                                        c.getPriority());
                    }

                    if (category != null
                            && !category.isBlank()) {
                        matches = matches
                                && category.equals(
                                        c.getCategory());
                    }

                    if (district != null
                            && !district.isBlank()) {
                        matches = matches
                                && c.getDistrict() != null
                                && c.getDistrict()
                                   .toLowerCase()
                                   .contains(
                                        district
                                        .toLowerCase());
                    }

                    return matches;
                })
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // MAP TO RESPONSE
    // ─────────────────────────────────────────
    public ComplaintResponse mapToResponse(
            Complaint complaint) {
        return ComplaintResponse.builder()
                .complaintId(complaint.getComplaintId())
                .createdDate(complaint.getCreatedDate())
                .callerName(complaint.getCallerName())
                .callerMobile(complaint.getCallerMobile())
                .callerAddress(complaint.getCallerAddress())
                .district(complaint.getDistrict())
                .state(complaint.getState())
                .subject(complaint.getSubject())
                .description(complaint.getDescription())
                .category(complaint.getCategory())
                .priority(complaint.getPriority())
                .status(complaint.getStatus())
                .assignedTo(complaint.getAssignedTo())
                .assignedName(complaint.getAssignedName())
                .assignedDate(complaint.getAssignedDate())
                .operatorId(complaint.getOperatorId())
                .operatorName(complaint.getOperatorName())
                .lastUpdated(complaint.getLastUpdated())
                .resolvedDate(complaint.getResolvedDate())
                .closedDate(complaint.getClosedDate())
                .ministerNote(complaint.getMinisterNote())
                .ministerDirection(
                        complaint.getMinisterDirection())
                .actionTaken(complaint.getActionTaken())
                .closureRemark(complaint.getClosureRemark())
                .escalationRemark(
                        complaint.getEscalationRemark())
                .build();
    }
}