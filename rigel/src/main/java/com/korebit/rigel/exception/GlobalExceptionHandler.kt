package com.korebit.rigel.exception;

import com.korebit.rigel.filter.RequestCorrelationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

/**
 * Translates exceptions into a single JSON error contract:
 * {@code {timestamp, status, error, message, correlationId}}.
 *
 * <p>Also receives Spring Security errors raised in the filter chain (see {@code SecurityConfig}),
 * so {@code 401}/{@code 403} share this format.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ApiError(LocalDateTime timestamp, int status, String error, String message, String correlationId) {
    }

    // ── 400 ─────────────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud incorrecta", ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.debug("Unreadable request body: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Solicitud incorrecta",
                "El cuerpo de la solicitud es inválido o le faltan campos obligatorios", request);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class
    })
    public ResponseEntity<ApiError> handleBadParameter(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud incorrecta", ex.getMessage(), request);
    }

    // ── 401 / 403 ───────────────────────────────────────────────────────────

    /**
     * Spring Security's own message ("Bad credentials") is replaced unless the application threw
     * the exception with a specific message (e.g. an expired refresh token).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (message == null || message.isBlank() || "Bad credentials".equalsIgnoreCase(message)) {
            message = "Credenciales inválidas";
        }
        return build(HttpStatus.UNAUTHORIZED, "No autorizado", message, request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "No autorizado", "Usuario inactivo. Contacta a un administrador", request);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiError> handleAuthInfrastructure(InternalAuthenticationServiceException ex,
                                                             HttpServletRequest request) {
        return handleUnexpected(ex, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "No autorizado",
                "Se requiere una sesión válida para acceder a este recurso", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "Acceso denegado",
                "No tienes permisos para realizar esta acción", request);
    }

    // ── 404 / 405 / 409 ─────────────────────────────────────────────────────

    @ExceptionHandler(EntityNotFundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Entidad no encontrada", ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", "La ruta solicitada no existe", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido", ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Conflicto de datos",
                "La operación entra en conflicto con datos existentes o relacionados", request);
    }

    // ── 500 ─────────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception correlationId={}", correlationId(request), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
                "Ocurrió un error inesperado. Referencia: " + correlationId(request), request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String title, String message, HttpServletRequest request) {
        ApiError body = new ApiError(LocalDateTime.now(), status.value(), title, message, correlationId(request));
        return ResponseEntity.status(status).body(body);
    }

    private String correlationId(HttpServletRequest request) {
        Object correlationId = request.getAttribute(RequestCorrelationFilter.CORRELATION_ID_KEY);
        return correlationId != null ? correlationId.toString() : "N/A";
    }
}
