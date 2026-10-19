package com.pulsepass.controller;

import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Endpoints de usuarios (PRD Controladores, sección 8.4).
 *
 * <pre>
 * POST /api/users                          → UserService.register()       → 201 | 409
 * GET  /api/users/by-email?email=...       → UserService.findByEmail()    → 200 | 404
 * GET  /api/users/by-username?username=... → UserService.findByUsername() → 200 | 404
 * </pre>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // FR-CTRL-USR-001 (registro atómico: usuario + credencial BCrypt + ROLE_USER)
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse created = authService.register(request);
        URI location = UriComponentsBuilder.fromPath("/api/users/by-username")
                .queryParam("username", created.username())
                .encode()
                .build()
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // FR-CTRL-USR-002
    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> findByEmail(
            @RequestParam("email")
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be a valid email address") String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    // FR-CTRL-USR-003
    @GetMapping("/by-username")
    public ResponseEntity<UserResponse> findByUsername(
            @RequestParam("username") @NotBlank(message = "Username is required") String username) {
        return ResponseEntity.ok(userService.findByUsername(username));
    }
}
