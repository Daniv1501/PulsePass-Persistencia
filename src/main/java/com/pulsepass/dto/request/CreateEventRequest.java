package com.pulsepass.dto.request;

import com.pulsepass.domain.EventCategory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request de creación de evento.
 *
 * Fase 3 (PRD Controladores, sección 11.1): las anotaciones de Bean Validation cubren la
 * validación estructural (campo obligatorio, longitud, valor mínimo). Las reglas de negocio
 * (venue activo, fecha futura, código único) siguen viviendo en EventService.
 * Las longitudes máximas coinciden con las columnas definidas en V1__create_schema.sql.
 */
public record CreateEventRequest(
        @NotBlank(message = "Event code is required")
        @Size(max = 30, message = "Event code must have at most 30 characters")
        String eventCode,

        @NotBlank(message = "Event name is required")
        @Size(max = 150, message = "Event name must have at most 150 characters")
        String name,

        @Size(max = 2000, message = "Description must have at most 2000 characters")
        String description,

        @NotNull(message = "Category is required")
        EventCategory category,

        @NotNull(message = "Event date is required")
        LocalDateTime eventDate,

        @NotNull(message = "Minimum age is required")
        @Min(value = 0, message = "Minimum age cannot be negative")
        Integer minimumAge,

        @NotBlank(message = "Venue code is required")
        @Size(max = 30, message = "Venue code must have at most 30 characters")
        String venueCode
) {
}
