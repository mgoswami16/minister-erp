package com.ministererp.model;

import java.time.LocalDateTime;

public class AuditLog {

    private String auditId;
    private String complaintId;
    private String userId;
    private String userName;
    private String userRole;
    private String action;
    private String previousStatus;
    private String newStatus;
    private String remarks;
    private LocalDateTime timestamp;
    private String ipAddress;

    public static final String ACTION_CREATED   = "CREATED";
    public static final String ACTION_ASSIGNED  = "ASSIGNED";
    public static final String ACTION_UPDATED   = "UPDATED";
    public static final String ACTION_FORWARDED = "FORWARDED";
    public static final String ACTION_ESCALATED = "ESCALATED";
    public static final String ACTION_RESOLVED  = "RESOLVED";
    public static final String ACTION_CLOSED    = "CLOSED";
    public static final String ACTION_COMMENTED = "COMMENTED";
    public static final String ACTION_REVIEWED  = "MINISTER_REVIEWED";

    public AuditLog() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AuditLog a = new AuditLog();

        public Builder auditId(String v) {
            a.auditId = v; return this; }
        public Builder complaintId(String v) {
            a.complaintId = v; return this; }
        public Builder userId(String v) {
            a.userId = v; return this; }
        public Builder userName(String v) {
            a.userName = v; return this; }
        public Builder userRole(String v) {
            a.userRole = v; return this; }
        public Builder action(String v) {
            a.action = v; return this; }
        public Builder previousStatus(String v) {
            a.previousStatus = v; return this; }
        public Builder newStatus(String v) {
            a.newStatus = v; return this; }
        public Builder remarks(String v) {
            a.remarks = v; return this; }
        public Builder timestamp(LocalDateTime v) {
            a.timestamp = v; return this; }
        public Builder ipAddress(String v) {
            a.ipAddress = v; return this; }
        public AuditLog build() { return a; }
    }

    public String getAuditId() { return auditId; }
    public String getComplaintId() { return complaintId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserRole() { return userRole; }
    public String getAction() { return action; }
    public String getPreviousStatus() { return previousStatus; }
    public String getNewStatus() { return newStatus; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getIpAddress() { return ipAddress; }

    public void setAuditId(String v) { auditId = v; }
    public void setComplaintId(String v) { complaintId = v; }
    public void setUserId(String v) { userId = v; }
    public void setUserName(String v) { userName = v; }
    public void setUserRole(String v) { userRole = v; }
    public void setAction(String v) { action = v; }
    public void setPreviousStatus(String v) { previousStatus = v; }
    public void setNewStatus(String v) { newStatus = v; }
    public void setRemarks(String v) { remarks = v; }
    public void setTimestamp(LocalDateTime v) { timestamp = v; }
    public void setIpAddress(String v) { ipAddress = v; }
}