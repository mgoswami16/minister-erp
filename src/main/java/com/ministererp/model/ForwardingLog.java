package com.ministererp.model;

import java.time.LocalDateTime;

public class ForwardingLog {

    private String forwardId;
    private String complaintId;
    private String fromUserId;
    private String fromUserName;
    private String fromUserRole;
    private String toUserId;
    private String toUserName;
    private String toUserRole;
    private String remarks;
    private LocalDateTime forwardedAt;
    private String forwardType;

    public static final String TYPE_INTERNAL    = "INTERNAL";
    public static final String TYPE_TO_MINISTER = "TO_MINISTER";
    public static final String TYPE_REASSIGNMENT= "REASSIGNMENT";

    public ForwardingLog() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ForwardingLog f =
            new ForwardingLog();

        public Builder forwardId(String v) {
            f.forwardId = v; return this; }
        public Builder complaintId(String v) {
            f.complaintId = v; return this; }
        public Builder fromUserId(String v) {
            f.fromUserId = v; return this; }
        public Builder fromUserName(String v) {
            f.fromUserName = v; return this; }
        public Builder fromUserRole(String v) {
            f.fromUserRole = v; return this; }
        public Builder toUserId(String v) {
            f.toUserId = v; return this; }
        public Builder toUserName(String v) {
            f.toUserName = v; return this; }
        public Builder toUserRole(String v) {
            f.toUserRole = v; return this; }
        public Builder remarks(String v) {
            f.remarks = v; return this; }
        public Builder forwardedAt(
                LocalDateTime v) {
            f.forwardedAt = v; return this; }
        public Builder forwardType(String v) {
            f.forwardType = v; return this; }
        public ForwardingLog build() {
            return f; }
    }

    public String getForwardId() { return forwardId; }
    public String getComplaintId() { return complaintId; }
    public String getFromUserId() { return fromUserId; }
    public String getFromUserName() { return fromUserName; }
    public String getFromUserRole() { return fromUserRole; }
    public String getToUserId() { return toUserId; }
    public String getToUserName() { return toUserName; }
    public String getToUserRole() { return toUserRole; }
    public String getRemarks() { return remarks; }
    public LocalDateTime getForwardedAt() { return forwardedAt; }
    public String getForwardType() { return forwardType; }

    public void setForwardId(String v) { forwardId = v; }
    public void setComplaintId(String v) { complaintId = v; }
    public void setFromUserId(String v) { fromUserId = v; }
    public void setFromUserName(String v) { fromUserName = v; }
    public void setFromUserRole(String v) { fromUserRole = v; }
    public void setToUserId(String v) { toUserId = v; }
    public void setToUserName(String v) { toUserName = v; }
    public void setToUserRole(String v) { toUserRole = v; }
    public void setRemarks(String v) { remarks = v; }
    public void setForwardedAt(LocalDateTime v) { forwardedAt = v; }
    public void setForwardType(String v) { forwardType = v; }
}