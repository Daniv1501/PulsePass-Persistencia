package com.pulsepass.dto.response;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Contrato JSON único para todos los errores HTTP de la API (PRD Controladores, sección 13).
 *
 * @param timestamp momento en que se produjo el error
 * @param status    código HTTP numérico
 * @param error     frase estándar del código HTTP (por ejemplo "Not Found")
 * @param message   descripción legible del error, sin detalles internos
 * @param details   errores por campo (validación); vacío en el resto de casos
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details
) {

    public static ErrorResponse of(HttpStatus status, String message) {
        return of(status, message, Map.of());
    }

    public static ErrorResponse of(HttpStatus status, String message, Map<String, String> details) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                details == null ? Map.of() : details
        );
    }
}
