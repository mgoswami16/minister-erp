package com.ministererp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AppointmentRequest {

    @NotBlank(message = "Visitor name is required")
    private String visitorName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$",
             message = "Enter valid 10-digit mobile number")
    private String visitorMobile;

    private String visitorAddress;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    @NotBlank(message = "Preferred date is required")
    private String preferredDate;

    private String preferredTime;
    private String meetingLocation;
    private String remarks;
}