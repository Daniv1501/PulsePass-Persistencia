package com.pulsepass.controller;

import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.UserResponse;
import com.pulsepass.service.AuthService;
import com.pulsepass.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;
    private final AuthService auth;
    public UserController(UserService users, AuthService auth) { this.users=users; this.auth=auth; }
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request));
    }
    @GetMapping("/by-email")
    @PreAuthorize("@authorizationService.canReadUserEmail(#email)")
    public ResponseEntity<UserResponse> byEmail(@RequestParam @NotBlank @Email String email) {
        return ResponseEntity.ok(users.findByEmail(email));
    }
    @GetMapping("/by-username")
    @PreAuthorize("@authorizationService.canReadUserUsername(#username)")
    public ResponseEntity<UserResponse> byUsername(@RequestParam @NotBlank String username) {
        return ResponseEntity.ok(users.findByUsername(username));
    }
}
