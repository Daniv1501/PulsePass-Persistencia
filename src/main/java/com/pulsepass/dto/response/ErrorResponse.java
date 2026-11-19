package com.pulsepass.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;

public record ErrorResponse(LocalDateTime timestamp, int status, String error,
                            String message, Map<String, String> details) {
    public static ErrorResponse of(HttpStatus status, String message, Map<String, String> details) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, details);
    }
}
