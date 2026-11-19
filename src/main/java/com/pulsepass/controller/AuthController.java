package com.pulsepass.controller;

import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.AuthenticatedUserResponse;
import com.pulsepass.security.PulsePassPrincipal;
import com.pulsepass.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth=auth; }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(auth.login(request));
    }
    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(@AuthenticationPrincipal PulsePassPrincipal principal) {
        return ResponseEntity.ok(new AuthenticatedUserResponse(principal.userId(), principal.username(),
                principal.email(), principal.roles()));
    }
}
