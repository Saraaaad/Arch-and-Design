package org.example.tourism.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing users (Admin only)")
public class UserManagementController {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserManagementController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (Admin only)")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID (Admin only)")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by username (Admin only)")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/admin-create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a user with specific roles (Admin only)")
    public ResponseEntity<AuthResponse> createUserByAdmin(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PutMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable a user (Admin only)")
    public ResponseEntity<UserDto> disableUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(false);
                    User savedUser = userRepository.save(user);
                    return ResponseEntity.ok(mapToDto(savedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable a user (Admin only)")
    public ResponseEntity<UserDto> enableUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setEnabled(true);
                    User savedUser = userRepository.save(user);
                    return ResponseEntity.ok(mapToDto(savedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user roles (Admin only)")
    public ResponseEntity<UserDto> updateUserRoles(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRolesRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setRoles(request.roles());
                    User savedUser = userRepository.save(user);
                    return ResponseEntity.ok(mapToDto(savedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user (Admin only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                user.getRoles()
        );
    }
}