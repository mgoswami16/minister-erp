package com.ministererp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

public class User {

    private String userId;
    private String name;
    private String role;
    private String mobile;
    private String email;
    private String username;
    private String passwordHash;
    private String department;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String createdBy;

    // ── Role Constants
    public static final String ROLE_OPERATOR = "OPERATOR";
    public static final String ROLE_OFFICER  = "OFFICER";
    public static final String ROLE_MINISTER = "MINISTER";

    // ── Department Constants
    public static final String DEPT_KANNAUJ        = "KANNAUJ";
    public static final String DEPT_POLICE         = "POLICE";
    public static final String DEPT_CORRESPONDENCE = "CORRESPONDENCE";
    public static final String DEPT_WESTERN_UP     = "WESTERN_UP";
    public static final String DEPT_APPOINTMENTS   = "APPOINTMENTS";
    public static final String DEPT_BJP            = "BJP";

    // ── Constructors
    public User() {}

    // ── Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private User user = new User();

        public Builder userId(String v) {
            user.userId = v; return this; }
        public Builder name(String v) {
            user.name = v; return this; }
        public Builder role(String v) {
            user.role = v; return this; }
        public Builder mobile(String v) {
            user.mobile = v; return this; }
        public Builder email(String v) {
            user.email = v; return this; }
        public Builder username(String v) {
            user.username = v; return this; }
        public Builder passwordHash(String v) {
            user.passwordHash = v; return this; }
        public Builder department(String v) {
            user.department = v; return this; }
        public Builder isActive(boolean v) {
            user.active = v; return this; }
        public Builder createdAt(LocalDateTime v) {
            user.createdAt = v; return this; }
        public Builder lastLogin(LocalDateTime v) {
            user.lastLogin = v; return this; }
        public Builder createdBy(String v) {
            user.createdBy = v; return this; }
        public User build() { return user; }
    }

    // ── Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getMobile() { return mobile; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }

    @JsonIgnore
    public String getPasswordHash() {
        return passwordHash; }

    public String getDepartment() {
        return department; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() {
        return createdAt; }
    public LocalDateTime getLastLogin() {
        return lastLogin; }
    public String getCreatedBy() {
        return createdBy; }

    // ── Setters
    public void setUserId(String v) {
        this.userId = v; }
    public void setName(String v) {
        this.name = v; }
    public void setRole(String v) {
        this.role = v; }
    public void setMobile(String v) {
        this.mobile = v; }
    public void setEmail(String v) {
        this.email = v; }
    public void setUsername(String v) {
        this.username = v; }
    public void setPasswordHash(String v) {
        this.passwordHash = v; }
    public void setDepartment(String v) {
        this.department = v; }
    public void setActive(boolean v) {
        this.active = v; }
    public void setCreatedAt(LocalDateTime v) {
        this.createdAt = v; }
    public void setLastLogin(LocalDateTime v) {
        this.lastLogin = v; }
    public void setCreatedBy(String v) {
        this.createdBy = v; }
}