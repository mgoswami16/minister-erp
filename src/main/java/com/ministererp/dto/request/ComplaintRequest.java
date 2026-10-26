package com.ministererp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ComplaintRequest {

    // ── Caller Details
    @NotBlank(message = "Caller name is required")
    private String callerName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$",
             message = "Enter valid 10-digit mobile number")
    private String callerMobile;

    private String callerAddress;

    @NotBlank(message = "District is required")
    private String district;

    private String state;

    // ── Complaint Details
    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Priority is required")
    private String priority;

    // ── Assignment
    private String assignedTo;
    private String assignedName;
}