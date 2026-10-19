package com.pulsepass.exception;

import com.pulsepass.dto.response.ErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;

/**
 * Único punto de traducción de excepciones a HTTP (PRD Controladores, secciones 14 y 15).
 *
 * Los Controllers no tienen bloques try/catch: cualquier excepción lanzada por la capa
 * Service o por Spring MVC llega aquí y se transforma en un {@code ResponseEntity<ErrorResponse>}.
 *
 * <pre>
 * MethodArgumentNotValidException   → 400 (Bean Validation sobre @RequestBody)
 * HandlerMethodValidationException  → 400 (Bean Validation sobre @RequestParam / @PathVariable)
 * HttpMessageNotReadableException   → 400 (JSON mal formado, enum o fecha inválidos)
 * MissingServletRequestParameter    → 400 (falta un query param obligatorio)
 * MethodArgumentTypeMismatch        → 400 (por ejemplo artistId no numérico)
 * ResourceNotFoundException         → 404
 * NoResourceFoundException          → 404 (ruta inexistente)
 * HttpRequestMethodNotSupported     → 405
 * HttpMediaTypeNotSupported         → 415
 * DuplicateResourceException        → 409
 * BusinessRuleException             → 409
 * Otras excepciones de Spring MVC    → su propio código 4xx
 * Exception                         → 500 (sin exponer stack trace ni mensaje interno)
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String VALIDATION_FAILED = "Validation failed";
    static final String MALFORMED_REQUEST = "Malformed JSON request";
    static final String UNEXPECTED_ERROR = "An unexpected error occurred";

    // ---------- 400 Bad Request ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // Si un campo tiene varias violaciones se conserva la primera
            details.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> details.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, details);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleParameterValidation(HandlerMethodValidationException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getAllValidationResults().forEach(result -> {
            String key = parameterName(result.getMethodParameter());
            result.getResolvableErrors().stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .findFirst()
                    .ifPresent(message -> details.putIfAbsent(key, message));
        });
        return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedRequest(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, MALFORMED_REQUEST, Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED,
                Map.of(ex.getParameterName(), "Parameter is required"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED,
                Map.of(ex.getName(), "Invalid value: " + ex.getValue()));
    }

    // ---------- 404 Not Found ----------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoRoute(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Resource not found: /" + ex.getResourcePath(), Map.of());
    }

    // ---------- 405 / 415 ----------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "HTTP method not supported: " + ex.getMethod(), Map.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Content type not supported: " + ex.getContentType(), Map.of());
    }

    // ---------- 409 Conflict ----------

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }
    
        @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), Map.of());
    }
    
    
    // ---------------------------------------------------------------- seguridad (método)

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this operation", Map.of());
    }

    // ---------- 500 Internal Server Error ----------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        // Otras excepciones propias de Spring MVC (406, 400 de binding, etc.) ya traen su código
        // HTTP: se respeta ese código en lugar de convertirlas en un 500 falso.
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatus status = HttpStatus.resolve(springError.getStatusCode().value());
            if (status != null && !status.is5xxServerError()) {
                return build(status, status.getReasonPhrase(), Map.of());
            }
        }
        // NFR-CTRL-007: el detalle queda en el log del servidor, nunca en la respuesta
        log.error("Unexpected error processing request", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, UNEXPECTED_ERROR, Map.of());
    }

    /** Nombre público del parámetro: el declarado en @RequestParam/@PathVariable o el del método. */
    private static String parameterName(MethodParameter parameter) {
        RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null && !requestParam.name().isBlank()) {
            return requestParam.name();
        }
        PathVariable pathVariable = parameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null && !pathVariable.name().isBlank()) {
            return pathVariable.name();
        }
        String name = parameter.getParameterName();
        return name != null ? name : "parameter";
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, Map<String, String> details) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message, details));
    }
}
