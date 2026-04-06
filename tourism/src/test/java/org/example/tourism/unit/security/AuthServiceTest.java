package org.example.tourism.unit.security;

import org.example.tourism.common.Role;
import org.example.tourism.security.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService tokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        // Manually create AuthService with mocked dependencies
        long refreshTokenDays = 7L;
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                tokenService,
                refreshTokenRepository,
                refreshTokenDays
        );

        testUser = new User();
        testUser.setId(100L);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setEnabled(true);
        testUser.setRoles(Set.of(Role.GUEST));

        loginRequest = new LoginRequest("testuser", "password123");

        registerRequest = new RegisterRequest(
                "newuser",
                "password123",
                "newuser@example.com",
                "New User",
                Set.of(Role.GUEST)
        );

        refreshToken = new RefreshToken();
        refreshToken.setId(1L);
        refreshToken.setToken("refresh-token-123");
        refreshToken.setUser(testUser);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60));
        refreshToken.setRevoked(false);
    }

    @Test
    void login_ShouldSucceed_WhenValidCredentials() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(tokenService.generateAccessToken(anyString(), anyList(), anyLong()))
                .thenReturn("access-token-123");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isNotNull();
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.message()).isEqualTo("Login successful");

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(tokenService, times(1)).generateAccessToken(anyString(), anyList(), anyLong());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_ShouldThrowException_WhenUserDisabled() {
        // Given
        testUser.setEnabled(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIncorrect() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
    }

    @Test
    void register_ShouldSucceed_WhenValidRequest() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenService.generateAccessToken(anyString(), anyList(), anyLong()))
                .thenReturn("access-token-123");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.message()).isEqualTo("Registration successful");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(savedUser.getFullName()).isEqualTo("New User");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.getRoles()).contains(Role.GUEST);
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenValidRefreshToken() {
        // Given
        when(refreshTokenRepository.findByToken("refresh-token-123")).thenReturn(Optional.of(refreshToken));
        when(tokenService.generateAccessToken(anyString(), anyList(), anyLong()))
                .thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // When
        AuthResponse response = authService.refreshToken("refresh-token-123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.message()).isEqualTo("Token refreshed successfully");
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenNotFound() {
        // Given
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenRevoked() {
        // Given
        refreshToken.setRevoked(true);
        when(refreshTokenRepository.findByToken("refresh-token-123")).thenReturn(Optional.of(refreshToken));

        // When/Then
        assertThatThrownBy(() -> authService.refreshToken("refresh-token-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token has been revoked");
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenExpired() {
        // Given
        refreshToken.setExpiryDate(Instant.now().minusSeconds(60));
        when(refreshTokenRepository.findByToken("refresh-token-123")).thenReturn(Optional.of(refreshToken));

        // When/Then
        assertThatThrownBy(() -> authService.refreshToken("refresh-token-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token has expired");
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void revokeRefreshToken_ShouldRevokeToken_WhenExists() {
        // Given
        when(refreshTokenRepository.findByToken("refresh-token-123")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // When
        authService.revokeRefreshToken("refresh-token-123");

        // Then
        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void revokeRefreshToken_ShouldThrowException_WhenTokenNotFound() {
        // Given
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> authService.revokeRefreshToken("invalid-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token not found");
    }

    @Test
    void logout_ShouldRevokeToken_WhenExists() {
        // Given
        when(refreshTokenRepository.findByToken("refresh-token-123")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        // When
        authService.logout("refresh-token-123");

        // Then
        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void logout_ShouldDoNothing_WhenTokenNotFound() {
        // Given
        when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When
        authService.logout("invalid-token");

        // Then
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }
}