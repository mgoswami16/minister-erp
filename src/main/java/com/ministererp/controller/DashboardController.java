package com.ministererp.controller;

import com.ministererp.dto.response.ApiResponse;
import com.ministererp.dto.response.DashboardResponse;
import com.ministererp.security.JwtTokenProvider;
import com.ministererp.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final JwtTokenProvider jwtTokenProvider;

    // ─────────────────────────────────────────
    // OPERATOR DASHBOARD
    // GET /api/v1/dashboard/operator
    // ─────────────────────────────────────────
    @GetMapping("/operator")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<ApiResponse<DashboardResponse>>
    getOperatorDashboard(
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String operatorId =
                jwtTokenProvider.extractUserId(token);

        DashboardResponse dashboard =
                dashboardService
                        .getOperatorDashboard(operatorId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboard,
                        "Operator dashboard loaded"));
    }

    // ─────────────────────────────────────────
    // OFFICER DASHBOARD
    // GET /api/v1/dashboard/officer
    // ─────────────────────────────────────────
    @GetMapping("/officer")
    @PreAuthorize("hasAnyRole('OFFICER','MINISTER')")
    public ResponseEntity<ApiResponse<DashboardResponse>>
    getOfficerDashboard(
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String officerId =
                jwtTokenProvider.extractUserId(token);

        DashboardResponse dashboard =
                dashboardService
                        .getOfficerDashboard(officerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboard,
                        "Officer dashboard loaded"));
    }

    // ─────────────────────────────────────────
    // MINISTER DASHBOARD
    // GET /api/v1/dashboard/minister
    // ─────────────────────────────────────────
    @GetMapping("/minister")
    @PreAuthorize("hasRole('MINISTER')")
    public ResponseEntity<ApiResponse<DashboardResponse>>
    getMinisterDashboard() {

        DashboardResponse dashboard =
                dashboardService.getMinisterDashboard();

        return ResponseEntity.ok(
                ApiResponse.success(
                        dashboard,
                        "Minister dashboard loaded"));
    }
}