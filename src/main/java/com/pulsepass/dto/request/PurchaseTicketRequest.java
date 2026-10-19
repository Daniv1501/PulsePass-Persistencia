package com.pulsepass.dto.request;

import com.pulsepass.domain.TicketType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request de compra de ticket.
 *
 * Fase 3 (PRD Controladores, sección 11.3): validación estructural con Bean Validation.
 * Usuario activo, evento publicado, edad mínima y capacidad siguen en TicketService.
 */
public record PurchaseTicketRequest(
        @NotBlank(message = "User email is required")
        @Email(message = "User email must be a valid email address")
        String userEmail,

        @NotBlank(message = "Event code is required")
        String eventCode,

        @NotNull(message = "Ticket type is required")
        TicketType type
) {
}
