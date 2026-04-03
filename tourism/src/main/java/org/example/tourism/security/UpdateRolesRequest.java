package org.example.tourism.security;

import jakarta.validation.constraints.NotNull;
import org.example.tourism.common.Role;

import java.util.Set;

public record UpdateRolesRequest(
        @NotNull(message = "Roles are required")
        Set<Role> roles
) {}