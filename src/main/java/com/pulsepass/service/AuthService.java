package com.pulsepass.service;
import com.pulsepass.dto.request.LoginRequest;
import com.pulsepass.dto.request.RegisterUserRequest;
import com.pulsepass.dto.response.AuthResponse;
import com.pulsepass.dto.response.UserResponse;
public interface AuthService {
    AuthResponse login(LoginRequest request);
    UserResponse register(RegisterUserRequest request);
}
