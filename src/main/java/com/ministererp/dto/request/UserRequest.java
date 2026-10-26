package com.ministererp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Mobile is required")
    @Pattern(regexp = "^[6-9]\\d{9}$",
             message = "Enter valid 10-digit mobile number")
    private String mobile;

    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private String department;
    private boolean isActive;
}