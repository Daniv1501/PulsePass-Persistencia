package com.pulsepass.dto.request;

import com.pulsepass.domain.EventCategory;

import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

public record CreateEventRequest(
        @NotBlank @Size(max = 30) String eventCode,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotNull EventCategory category,
        @NotNull LocalDateTime eventDate,
        @NotNull @Min(0) Integer minimumAge,
        @NotBlank @Size(max = 30) String venueCode
) {
}
