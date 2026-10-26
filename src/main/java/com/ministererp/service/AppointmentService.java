package com.ministererp.service;

import com.ministererp.dto.request.AppointmentRequest;
import com.ministererp.exception.ResourceNotFoundException;
import com.ministererp.model.Appointment;
import com.ministererp.util.DateUtil;
import com.ministererp.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final GoogleSheetsService googleSheetsService;
    private final IdGenerator idGenerator;
    private final DateUtil dateUtil;
    private final AuditService auditService;
    private final NotificationService notificationService;

    // ─────────────────────────────────────────
    // CREATE APPOINTMENT
    // ─────────────────────────────────────────
    public Appointment createAppointment(
            AppointmentRequest request,
            String operatorId,
            String operatorName) {

        Appointment appointment = Appointment.builder()
                .appointmentId(
                        idGenerator.generateAppointmentId())
                .createdDate(dateUtil.now())
                .visitorName(request.getVisitorName())
                .visitorMobile(request.getVisitorMobile())
                .visitorAddress(request.getVisitorAddress())
                .purpose(request.getPurpose())
                .preferredDate(request.getPreferredDate())
                .preferredTime(request.getPreferredTime())
                .meetingLocation(request.getMeetingLocation())
                .status(Appointment.STATUS_PENDING)
                .operatorId(operatorId)
                .operatorName(operatorName)
                .remarks(request.getRemarks())
                .lastUpdated(dateUtil.now())
                .build();

        boolean saved = googleSheetsService
                .saveAppointment(appointment);
        if (!saved) {
            throw new RuntimeException(
                    "Failed to create appointment");
        }

        auditService.logAction(
                operatorId,
                operatorName,
                "OPERATOR",
                "APPOINTMENT_CREATED",
                appointment.getAppointmentId(),
                null,
                Appointment.STATUS_PENDING,
                "New appointment registered for: "
                        + request.getVisitorName(),
                null
        );

        return appointment;
    }

    // ─────────────────────────────────────────
    // GET ALL APPOINTMENTS
    // ─────────────────────────────────────────
    public List<Appointment> getAllAppointments() {
        return googleSheetsService.getAllAppointments();
    }

    // ─────────────────────────────────────────
    // GET PENDING APPOINTMENTS
    // ─────────────────────────────────────────
    public List<Appointment> getPendingAppointments() {
        return googleSheetsService.getAllAppointments()
                .stream()
                .filter(a -> Appointment.STATUS_PENDING
                        .equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET APPOINTMENT BY ID
    // ─────────────────────────────────────────
    public Appointment getAppointmentById(
            String appointmentId) {
        Appointment appointment = googleSheetsService
                .getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new ResourceNotFoundException(
                    "Appointment", "id", appointmentId);
        }
        return appointment;
    }

    // ─────────────────────────────────────────
    // APPROVE APPOINTMENT
    // ─────────────────────────────────────────
    public Appointment approveAppointment(
            String appointmentId,
            String approvedDate,
            String approvedTime,
            String approvedVenue,
            String approvedByUserId,
            String approvedByName) {

        Appointment appointment =
                getAppointmentById(appointmentId);

        appointment.setStatus(Appointment.STATUS_APPROVED);
        appointment.setApprovedDate(approvedDate);
        appointment.setApprovedTime(approvedTime);
        appointment.setApprovedVenue(approvedVenue);
        appointment.setApprovedBy(approvedByUserId);
        appointment.setApprovedAt(dateUtil.now());
        appointment.setLastUpdated(dateUtil.now());

        googleSheetsService.updateAppointment(appointment);

        // Log audit
        auditService.logAction(
                approvedByUserId,
                approvedByName,
                "OFFICER",
                "APPOINTMENT_APPROVED",
                appointmentId,
                Appointment.STATUS_PENDING,
                Appointment.STATUS_APPROVED,
                "Appointment approved for "
                        + approvedDate
                        + " at " + approvedVenue,
                null
        );

        // Send notification
        notificationService.notifyAppointmentUpdate(
                appointment.getOperatorId(),
                appointment.getOperatorName(),
                appointmentId,
                "APPROVED"
        );

        return appointment;
    }

    // ─────────────────────────────────────────
    // REJECT APPOINTMENT
    // ─────────────────────────────────────────
    public Appointment rejectAppointment(
            String appointmentId,
            String rejectionReason,
            String rejectedByUserId,
            String rejectedByName) {

        Appointment appointment =
                getAppointmentById(appointmentId);

        appointment.setStatus(Appointment.STATUS_REJECTED);
        appointment.setRejectionReason(rejectionReason);
        appointment.setLastUpdated(dateUtil.now());

        googleSheetsService.updateAppointment(appointment);

        // Log audit
        auditService.logAction(
                rejectedByUserId,
                rejectedByName,
                "OFFICER",
                "APPOINTMENT_REJECTED",
                appointmentId,
                Appointment.STATUS_PENDING,
                Appointment.STATUS_REJECTED,
                "Rejected. Reason: " + rejectionReason,
                null
        );

        // Send notification
        notificationService.notifyAppointmentUpdate(
                appointment.getOperatorId(),
                appointment.getOperatorName(),
                appointmentId,
                "REJECTED"
        );

        return appointment;
    }

    // ─────────────────────────────────────────
    // KEEP APPOINTMENT PENDING
    // ─────────────────────────────────────────
    public Appointment keepPending(
            String appointmentId,
            String remarks,
            String userId,
            String userName) {

        Appointment appointment =
                getAppointmentById(appointmentId);

        appointment.setRemarks(remarks);
        appointment.setLastUpdated(dateUtil.now());

        googleSheetsService.updateAppointment(appointment);

        auditService.logAction(
                userId,
                userName,
                "OFFICER",
                "APPOINTMENT_KEPT_PENDING",
                appointmentId,
                Appointment.STATUS_PENDING,
                Appointment.STATUS_PENDING,
                "Kept pending. Remarks: " + remarks,
                null
        );

        return appointment;
    }

    // ─────────────────────────────────────────
    // GET APPOINTMENTS BY STATUS
    // ─────────────────────────────────────────
    public List<Appointment> getAppointmentsByStatus(
            String status) {
        return googleSheetsService.getAllAppointments()
                .stream()
                .filter(a -> status.equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // SEARCH APPOINTMENTS
    // ─────────────────────────────────────────
    public List<Appointment> searchAppointments(
            String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllAppointments();
        }
        String kw = keyword.toLowerCase();
        return googleSheetsService.getAllAppointments()
                .stream()
                .filter(a ->
                    (a.getVisitorName() != null
                        && a.getVisitorName()
                           .toLowerCase().contains(kw))
                    || (a.getVisitorMobile() != null
                        && a.getVisitorMobile().contains(kw))
                    || (a.getPurpose() != null
                        && a.getPurpose()
                           .toLowerCase().contains(kw))
                    || (a.getAppointmentId() != null
                        && a.getAppointmentId()
                           .toLowerCase().contains(kw))
                )
                .collect(Collectors.toList());
    }
}