package com.pulsepass.dto.response;
import com.pulsepass.security.Role;
import java.util.Set;
public record AuthenticatedUserResponse(Long userId, String username, String email, Set<Role> roles) {}
