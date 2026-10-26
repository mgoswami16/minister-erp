package com.ministererp.controller;

import com.ministererp.dto.request.UserRequest;
import com.ministererp.dto.response.ApiResponse;
import com.ministererp.model.User;
import com.ministererp.security.JwtTokenProvider;
import com.ministererp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // ─────────────────────────────────────────
    // GET ALL USERS
    // GET /api/v1/users
    // ─────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('MINISTER')")
    public ResponseEntity<ApiResponse<List<User>>>
    getAllUsers() {

        List<User> users = userService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        users,
                        "Fetched " + users.size()
                        + " users"));
    }

    // ─────────────────────────────────────────
    // GET ALL OFFICERS
    // GET /api/v1/users/officers
    // ─────────────────────────────────────────
    @GetMapping("/officers")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<ApiResponse<List<User>>>
    getAllOfficers() {

        List<User> officers = userService.getAllOfficers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        officers,
                        "Fetched " + officers.size()
                        + " officers"));
    }

    // ─────────────────────────────────────────
    // GET USER BY ID
    // GET /api/v1/users/{id}
    // ─────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MINISTER')")
    public ResponseEntity<ApiResponse<User>>
    getUserById(
            @PathVariable String id) {

        User user = userService.getUserById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        user,
                        "User fetched"));
    }

    // ─────────────────────────────────────────
    // CREATE USER
    // POST /api/v1/users
    // ─────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('MINISTER')")
    public ResponseEntity<ApiResponse<User>>
    createUser(
            @Valid @RequestBody UserRequest request,
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId =
                jwtTokenProvider.extractUserId(token);
        String userName =
                jwtTokenProvider.extractName(token);
        String userRole =
                jwtTokenProvider.extractRole(token);

        User user = userService.createUser(
                request,
                userId,
                userName,
                userRole);

        return ResponseEntity.ok(
                ApiResponse.success(
                        user,
                        "User created: "
                        + user.getName()));
    }

    // ─────────────────────────────────────────
    // UPDATE USER
    // PUT /api/v1/users/{id}
    // ─────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MINISTER')")
    public ResponseEntity<ApiResponse<User>>
    updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserRequest request,
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId =
                jwtTokenProvider.extractUserId(token);
        String userName =
                jwtTokenProvider.extractName(token);
        String userRole =
                jwtTokenProvider.extractRole(token);

        User user = userService.updateUser(
                id,
                request,
                userId,
                userName,
                userRole);

        return ResponseEntity.ok(
                ApiResponse.success(
                        user,
                        "User updated: "
                        + user.getName()));
    }

    // ─────────────────────────────────────────
    // TOGGLE USER STATUS
    // PUT /api/v1/users/{id}/toggle-status
    // ─────────────────────────────────────────
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('MINISTER')")
    public ResponseEntity<ApiResponse<User>>
    toggleStatus(
            @PathVariable String id,
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId =
                jwtTokenProvider.extractUserId(token);
        String userName =
                jwtTokenProvider.extractName(token);
        String userRole =
                jwtTokenProvider.extractRole(token);

        User user = userService.toggleUserStatus(
                id,
                userId,
                userName,
                userRole);

        return ResponseEntity.ok(
                ApiResponse.success(
                        user,
                        "User status changed to: "
                        + (user.isActive()
                                ? "ACTIVE"
                                : "INACTIVE")));
    }

    // ─────────────────────────────────────────
    // CHANGE PASSWORD
    // PUT /api/v1/users/change-password
    // ─────────────────────────────────────────
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>>
    changePassword(
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization")
            String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String userId =
                jwtTokenProvider.extractUserId(token);

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        boolean changed = userService.changePassword(
                userId,
                oldPassword,
                newPassword);

        return ResponseEntity.ok(
                ApiResponse.success(
                        changed ? "SUCCESS" : "FAILED",
                        "Password changed successfully"));
    }

    // ─────────────────────────────────────────
    // SUGGEST OFFICER BY CATEGORY
    // GET /api/v1/users/suggest
    // ─────────────────────────────────────────
    @GetMapping("/suggest")
    @PreAuthorize("hasAnyRole('OPERATOR','MINISTER')")
    public ResponseEntity<ApiResponse<User>>
    suggestOfficer(
            @RequestParam String category,
            @RequestParam(required = false)
            String district) {

        User suggested =
                userService.suggestOfficerByCategory(
                        category,
                        district);

        return ResponseEntity.ok(
                ApiResponse.success(
                        suggested,
                        suggested != null
                                ? "Suggested: "
                                + suggested.getName()
                                : "No suggestion"));
    }
}