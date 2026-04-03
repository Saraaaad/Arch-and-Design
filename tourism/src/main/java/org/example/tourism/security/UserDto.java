package org.example.tourism.security;

import org.example.tourism.common.Role;

import java.util.Set;

public record UserDto(
        Long id,
        String username,
        String email,
        String fullName,
        boolean enabled,
        Set<Role> roles
) {}