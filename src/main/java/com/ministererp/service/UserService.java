package com.ministererp.service;

import com.ministererp.dto.request.UserRequest;
import com.ministererp.exception.ResourceNotFoundException;
import com.ministererp.model.User;
import com.ministererp.util.DateUtil;
import com.ministererp.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GoogleSheetsService googleSheetsService;
    private final IdGenerator idGenerator;
    private final DateUtil dateUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    // ─────────────────────────────────────────
    // GET ALL USERS
    // ─────────────────────────────────────────
    public List<User> getAllUsers() {
        return googleSheetsService.getAllUsers();
    }

    // ─────────────────────────────────────────
    // GET ALL OFFICERS
    // ─────────────────────────────────────────
    public List<User> getAllOfficers() {
        return googleSheetsService.getAllUsers()
                .stream()
                .filter(u -> User.ROLE_OFFICER
                        .equals(u.getRole()))
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // GET USER BY ID
    // ─────────────────────────────────────────
    public User getUserById(String userId) {
        User user = googleSheetsService
                .findUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException(
                    "User", "id", userId);
        }
        return user;
    }

    // ─────────────────────────────────────────
    // CREATE USER
    // ─────────────────────────────────────────
    public User createUser(
            UserRequest request,
            String createdByUserId,
            String createdByName,
            String createdByRole) {

        // Check username already exists
        User existing = googleSheetsService
                .findUserByUsername(request.getUsername());
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Username already exists: "
                    + request.getUsername());
        }

        // Build new user
        User user = User.builder()
                .userId(idGenerator.generateUserId())
                .name(request.getName())
                .role(request.getRole())
                .mobile(request.getMobile())
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder
                        .encode(request.getPassword()))
                .department(request.getDepartment())
                .isActive(true)
                .createdAt(dateUtil.now())
                .createdBy(createdByUserId)
                .build();

        // Save to Google Sheets
        boolean saved = googleSheetsService.saveUser(user);
        if (!saved) {
            throw new RuntimeException(
                    "Failed to create user");
        }

        // Log audit
        auditService.logAction(
                createdByUserId,
                createdByName,
                createdByRole,
                "USER_CREATED",
                null,
                null,
                null,
                "New user created: " + user.getName()
                + " Role: " + user.getRole(),
                null
        );

        return user;
    }

    // ─────────────────────────────────────────
    // UPDATE USER
    // ─────────────────────────────────────────
    public User updateUser(
            String userId,
            UserRequest request,
            String updatedByUserId,
            String updatedByName,
            String updatedByRole) {

        User user = getUserById(userId);

        // Update fields
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        user.setActive(request.isActive());

        // Update password if provided
        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {
            user.setPasswordHash(
                    passwordEncoder.encode(
                            request.getPassword()));
        }

        boolean updated = googleSheetsService
                .updateUser(user);
        if (!updated) {
            throw new RuntimeException(
                    "Failed to update user");
        }

        // Log audit
        auditService.logAction(
                updatedByUserId,
                updatedByName,
                updatedByRole,
                "USER_UPDATED",
                null,
                null,
                null,
                "User updated: " + user.getName(),
                null
        );

        return user;
    }

    // ─────────────────────────────────────────
    // TOGGLE USER STATUS
    // ─────────────────────────────────────────
    public User toggleUserStatus(
            String userId,
            String updatedByUserId,
            String updatedByName,
            String updatedByRole) {

        User user = getUserById(userId);
        boolean previousStatus = user.isActive();
        user.setActive(!previousStatus);

        googleSheetsService.updateUser(user);

        auditService.logAction(
                updatedByUserId,
                updatedByName,
                updatedByRole,
                previousStatus
                        ? "USER_DISABLED"
                        : "USER_ENABLED",
                null,
                null,
                null,
                "User " + user.getName()
                + " status changed to: "
                + (user.isActive()
                        ? "ACTIVE" : "INACTIVE"),
                null
        );

        return user;
    }

    // ─────────────────────────────────────────
    // CHANGE PASSWORD
    // ─────────────────────────────────────────
    public boolean changePassword(
            String userId,
            String oldPassword,
            String newPassword) {

        User user = getUserById(userId);

        // Verify old password
        if (!passwordEncoder.matches(
                oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "Current password is incorrect");
        }

        // Set new password
        user.setPasswordHash(
                passwordEncoder.encode(newPassword));

        return googleSheetsService.updateUser(user);
    }

    // ─────────────────────────────────────────
    // GET OFFICER BY DEPARTMENT
    // ─────────────────────────────────────────
    public User getOfficerByDepartment(
            String department) {
        return googleSheetsService.getAllUsers()
                .stream()
                .filter(u -> User.ROLE_OFFICER
                        .equals(u.getRole()))
                .filter(User::isActive)
                .filter(u -> department.equals(
                        u.getDepartment()))
                .findFirst()
                .orElse(null);
    }

    // ─────────────────────────────────────────
    // AUTO SUGGEST OFFICER BY CATEGORY
    // ─────────────────────────────────────────
    public User suggestOfficerByCategory(
            String category,
            String district) {

        // Auto assignment logic
        String department = null;

        if (category != null) {
            switch (category.toUpperCase()) {
                case "KANNAUJ":
                    department = User.DEPT_KANNAUJ;
                    break;
                case "POLICE":
                case "FIR":
                case "CRIME":
                    department = User.DEPT_POLICE;
                    break;
                case "APPOINTMENT":
                    department = User.DEPT_APPOINTMENTS;
                    break;
                case "BJP":
                case "RSS":
                    department = User.DEPT_BJP;
                    break;
                case "WESTERN_UP":
                    department = User.DEPT_WESTERN_UP;
                    break;
                default:
                    break;
            }
        }

        // Check district for Western UP / Delhi NCR
        if (department == null && district != null) {
            List<String> westernUpDistricts = List.of(
                    "MEERUT", "AGRA", "MATHURA",
                    "ALIGARH", "MORADABAD", "BAREILLY",
                    "SAHARANPUR", "MUZAFFARNAGAR",
                    "GHAZIABAD", "NOIDA", "GAUTAM BUDDH NAGAR"
            );
            if (westernUpDistricts.contains(
                    district.toUpperCase())) {
                department = User.DEPT_WESTERN_UP;
            }
        }

        if (department != null) {
            return getOfficerByDepartment(department);
        }

        return null;
    }
}