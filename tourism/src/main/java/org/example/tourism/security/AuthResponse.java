package org.example.tourism.security;

import org.example.tourism.common.Role;

import java.util.Set;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        String email,
        String fullName,
        Set<Role> roles,
        Long userId,
        String message
) {}