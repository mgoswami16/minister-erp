package com.ministererp.controller;

import com.ministererp.dto.request.AppointmentRequest;
import com.ministererp.dto.response.ApiResponse;
import com.ministererp.model.Appointment;
import com.ministererp.security.JwtTokenProvider;
import com.ministererp.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final JwtTokenProvider jwtTokenProvider;

    // ─────────────────────────────────────────
    // CREATE APPOINTMENT
    // POST /api/v1/appointments
    // ─────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<ApiResponse<Appointment>>
    createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String operatorId =
                jwtTokenProvider.extractUserId(token);
        String operatorName =
                jwtTokenProvider.extractName(token);

        Appointment appointment =
                appointmentService.createAppointment(
                        request,
                        operatorId,
                        operatorName);

        return ResponseEntity.ok(
                ApiResponse.success(
                        appointment,
                        "Appointment created: "
                        + appointment.getAppointmentId()));
    }

    // ─────────────────────────────────────────
    // GET ALL APPOINTMENTS
    // GET /api/v1/appointments
    // ─────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('OFFICER','MINISTER')")
    public ResponseEntity<ApiResponse<List<Appointment>>>
    getAllAppointments() {

        List<Appointment> appointments =
                appointmentService.getAllAppointments();

        return ResponseEntity.ok(
                ApiResponse.success(
                        appointments,
                        "Fetched " + appointments.size()
                        + " appointments"));
    }

    // ─────────────────────────────────────────
    // GET PENDING APPOINTMENTS
    // GET /api/v1/appointments/pending
    // ─────────────────────────────────────────
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('OFFICER','MINISTER')")
    public ResponseEntity<ApiResponse<List<Appointment>>>
    getPendingAppointments() {

        List<Appointment> appointments =
                appointmentService.getPendingAppointments();

        return ResponseEntity.ok(
                ApiResponse.success(
                        appointments,
                        "Fetched " + appointments.size()
                        + " pending appointments"));
    }

    // ─────────────────────────────────────────
    // GET APPOINTMENT BY ID
    // GET /api/v1/appointments/{id}
    // ─────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Appointment>>
    getAppointmentById(
            @PathVariable String id) {

        Appointment appointment =
                appointmentService.getAppointmentById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        appointment,
                        "Appointment fetched"));
    }

    // ─────────────────────────────────────────
    // APPROVE APPOINTMENT
    // PUT /api/v1/appointments/{id}/approve
    // ─────────────────────────────────────────
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('OFFICER','MINISTER')")
    public ResponseEntity<ApiResponse<Appointment>>
    approveAppointment(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId =
                jwtTokenProvider.extractUserId(token);
        String userName =
                jwtTokenProvider.extractName(token);

        String approvedDate = body.get("approvedDate");
        String approvedTime = body.get("approvedTime");
        String approvedVenue = body.get("approvedVenue");

        Appointment appointment =
                appointmentService.approveAppointment(
                        id,
                        approvedDate,
                        approvedTime,
                        approvedVenue,
                        userId,
                        userName);

        return ResponseEntity.ok(
                ApiResponse.success(
                        appointment,
                        "Appointment approved for "
                        + approvedDate));
    }

    // ─────────────────────────────────────────
    // REJECT APPOINTMENT
    // PUT /api/v1/appointments/{id}/reject
    // ─────────────────────────────────────────
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('OFFICER','MINISTER')")
    public ResponseEntity<ApiResponse<Appointment>>
    rejectAppointment(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId =
                jwtTokenProvider.extractUserId(token);
        String userName =
                jwtTokenProvider.extractName(token);

        String rejectionReason =
                body.get("rejectionReason");

        Appointment appointment =
                appointmentService.rejectAppointment(
                        id,
                        rejectionReason,
                        userId,
                        userName);

        return ResponseEntity.ok(
                ApiResponse.success(
                        appointment,
                        "Appointment rejected"));
    }

    // ─────────────────────────────────────────
    // KEEP PENDING
    // PUT /api/v1/appointments/{id}/pending
    // ─────────────────────────────────────────
    @PutMapping("/{id}/pending")
    @PreAuthorize("hasAnyRole('OFFICER','MINISTER')")
    public ResponseEntity<ApiResponse<Appointment>>
    keepPending(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId =
                jwtTokenProvider.extractUserId(token);
        String userName =
                jwtTokenProvider.extractName(token);

        String remarks = body.get("remarks");

        Appointment appointment =
                appointmentService.keepPending(
                        id,
                        remarks,
                        userId,
                        userName);

        return ResponseEntity.ok(
                ApiResponse.success(
                        appointment,
                        "Appointment kept pending"));
    }

    // ─────────────────────────────────────────
    // SEARCH APPOINTMENTS
    // GET /api/v1/appointments/search
    // ─────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Appointment>>>
    searchAppointments(
            @RequestParam(required = false)
            String keyword) {

        List<Appointment> appointments =
                appointmentService
                        .searchAppointments(keyword);

        return ResponseEntity.ok(
                ApiResponse.success(
                        appointments,
                        "Found " + appointments.size()
                        + " appointments"));
    }

    // ─────────────────────────────────────────
    // GET BY STATUS
    // GET /api/v1/appointments/status/{status}
    // ─────────────────────────────────────────
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Appointment>>>
    getByStatus(
            @PathVariable String status) {

        List<Appointment> appointments =
                appointmentService
                        .getAppointmentsByStatus(status);

        return ResponseEntity.ok(
                ApiResponse.success(
                        appointments,
                        "Fetched " + appointments.size()
                        + " appointments"));
    }
}