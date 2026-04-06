package org.example.tourism.security;

import org.example.tourism.common.DuplicateResourceException;
import org.example.tourism.common.InvalidTokenException;
import org.example.tourism.common.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenDays;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService tokenService,
            RefreshTokenRepository refreshTokenRepository,
            @Value("${security.jwt.refresh-token-days:7}") long refreshTokenDays
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            User user = userRepository.findByUsername(request.username())
                    .filter(User::isEnabled)
                    .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new BadCredentialsException("Invalid username or password");
            }

            List<String> roles = user.getRoles().stream().map(Enum::name).toList();
            String accessToken = tokenService.generateAccessToken(user.getUsername(), roles, user.getId());
            String refreshToken = createRefreshToken(user);

            return new AuthResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    tokenService.getAccessTokenExpiresInSeconds(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRoles(),
                    user.getId(),
                    "Login successful"
            );
        } catch (BadCredentialsException e) {
            throw e; // This will be caught by GlobalExceptionHandler
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request){
        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists");
        }

        // For regular registration, force GUEST role unless it's the first user
        Set<Role> roles = request.roles();
        if (roles == null || roles.isEmpty()) {
            roles = Set.of(Role.GUEST);
        }

        // Create new user
        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(
                request.username(),
                passwordHash,
                request.email(),
                request.fullName(),
                true,
                roles
        );

        User savedUser = userRepository.save(user);

        // Generate tokens
        List<String> roleNames = savedUser.getRoles().stream().map(Enum::name).toList();
        String accessToken = tokenService.generateAccessToken(savedUser.getUsername(), roleNames, savedUser.getId());
        String refreshToken = createRefreshToken(savedUser);

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                tokenService.getAccessTokenExpiresInSeconds(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRoles(),
                savedUser.getId(),
                "Registration successful"
        );
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidTokenException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        List<String> roles = user.getRoles().stream().map(Enum::name).toList();
        String accessToken = tokenService.generateAccessToken(user.getUsername(), roles, user.getId());

        // Rotate refresh token
        String newRefreshToken = rotateRefreshToken(refreshToken);

        return new AuthResponse(
                accessToken,
                newRefreshToken,
                "Bearer",
                tokenService.getAccessTokenExpiresInSeconds(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles(),
                user.getId(),
                "Token refreshed successfully"
        );
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusSeconds(refreshTokenDays * 24 * 60 * 60);
        RefreshToken refreshToken = new RefreshToken(token, user, expiryDate);
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    private String rotateRefreshToken(RefreshToken oldToken) {
        refreshTokenRepository.delete(oldToken);
        return createRefreshToken(oldToken.getUser());
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}