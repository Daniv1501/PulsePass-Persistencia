package com.pulsepass.exception;

import com.pulsepass.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }
    @ExceptionHandler({BusinessRuleException.class, DuplicateResourceException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integrity() {
        return error(HttpStatus.CONFLICT, "Conflicto con los datos existentes", Map.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        Map<String,String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> details.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Revisa los datos de la solicitud", details);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            ServletRequestBindingException.class, HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> malformed(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "Solicitud o parámetros inválidos", Map.of());
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> authentication() {
        return error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", Map.of());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> forbidden() {
        return error(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación", Map.of());
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> noRoute() {
        return error(HttpStatus.NOT_FOUND, "Recurso no encontrado", Map.of());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected() {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", Map.of());
    }
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, Map<String,String> details) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message, details));
    }
}
