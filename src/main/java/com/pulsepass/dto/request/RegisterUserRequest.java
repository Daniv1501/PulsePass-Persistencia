package com.pulsepass.dto.request;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import com.pulsepass.validation.BcryptPassword;

public record RegisterUserRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Size(max = 30) String phone,
        @Size(max = 100) String city,
        @NotNull LocalDate birthDate,
        @BcryptPassword String password
) {
    // Compatibilidad con las pruebas y llamadas internas de la fase de servicios.
    public RegisterUserRequest(String username, String email, String firstName, String lastName,
                               String phone, String city, LocalDate birthDate) {
        this(username, email, firstName, lastName, phone, city, birthDate, null);
    }

    @Override
    public String toString() { return "RegisterUserRequest[redacted]"; }
}
