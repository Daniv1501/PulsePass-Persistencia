package com.pulsepass.dto.request;

import com.pulsepass.domain.TicketType;
import jakarta.validation.constraints.*;

public record PurchaseTicketRequest(
        @NotBlank @Email @Size(max = 150) String userEmail,
        @NotBlank @Size(max = 30) String eventCode,
        @NotNull TicketType type
) {
}
