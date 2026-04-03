package org.example.tourism.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.tourism.common.Role;

import java.util.Set;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 60, message = "Username must be between 3 and 60 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Full name is required")
        String fullName,

        @NotNull(message = "Role is required")
        Set<Role> roles  // For registration, ADMIN can set roles, others get default GUEST
) {}