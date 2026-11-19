package com.pulsepass.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record LoginRequest(@NotBlank @Size(max=150) String identifier,
                           @NotBlank @Size(max=256) String password) {
    @Override public String toString() { return "LoginRequest[redacted]"; }
}
