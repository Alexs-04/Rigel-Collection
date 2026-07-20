package com.korebit.rigel.exception;

import com.korebit.rigel.filter.RequestCorrelationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleNullPointerException(NullPointerException ex, HttpServletRequest request) {

        Map<String, Object> error = buildError(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {

        Map<String, Object> error = buildError(HttpStatus.BAD_REQUEST, "Solicitud incorrecta", ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EntityNotFundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<?> handleEntityNotFundException(EntityNotFundException ex, HttpServletRequest request) {

        Map<String, Object> error = buildError(HttpStatus.NOT_FOUND, "Entidad no encontrada", ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {

        Map<String, Object> error = buildError(HttpStatus.CONFLICT, "Conflicto de datos", ex.getMostSpecificCause().getMessage(), request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<?> handleGenericException(Exception ex, HttpServletRequest request) {

        Map<String, Object> error = buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", ex.getMessage(), request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private Map<String, Object> buildError(HttpStatus status,
                                           String errorTitle,
                                           String message,
                                           HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", errorTitle);
        error.put("message", message);

        Object correlationId = request.getAttribute(RequestCorrelationFilter.CORRELATION_ID_KEY);
        error.put("correlationId", correlationId != null ? correlationId.toString() : "N/A");

        return error;
    }
}
