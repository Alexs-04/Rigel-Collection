package com.korebit.rigel.exception

import com.korebit.rigel.filter.RequestCorrelationFilter
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.LocalDateTime

/**
 * Translates exceptions into a single JSON error contract:
 * `{timestamp, status, error, message, correlationId}`.
 *
 * Also receives Spring Security errors raised in the filter chain (see `SecurityConfig`),
 * so `401`/`403` share this format.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    data class ApiError(
        val timestamp: LocalDateTime,
        val status: Int,
        val error: String,
        val message: String?,
        val correlationId: String,
    )

    // ── 400 ─────────────────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.BAD_REQUEST, "Solicitud incorrecta", ex.message, request)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(ex: HttpMessageNotReadableException, request: HttpServletRequest): ResponseEntity<ApiError> {
        log.debug("Unreadable request body: {}", ex.message)
        return build(
            HttpStatus.BAD_REQUEST, "Solicitud incorrecta",
            "El cuerpo de la solicitud es inválido o le faltan campos obligatorios", request,
        )
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
        MissingRequestHeaderException::class,
    )
    fun handleBadParameter(ex: Exception, request: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.BAD_REQUEST, "Solicitud incorrecta", ex.message, request)

    // ── 401 / 403 ───────────────────────────────────────────────────────────

    /**
     * Spring Security's own message ("Bad credentials") is replaced unless the application threw
     * the exception with a specific message (e.g. an expired refresh token).
     */
    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException, request: HttpServletRequest): ResponseEntity<ApiError> {
        var message = ex.message
        if (message.isNullOrBlank() || "Bad credentials".equals(message, ignoreCase = true)) {
            message = "Credenciales inválidas"
        }
        return build(HttpStatus.UNAUTHORIZED, "No autorizado", message, request)
    }

    @ExceptionHandler(DisabledException::class)
    fun handleDisabled(ex: DisabledException, request: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.UNAUTHORIZED, "No autorizado", "Usuario inactivo. Contacta a un administrador", request)

    @ExceptionHandler(InternalAuthenticationServiceException::class)
    fun handleAuthInfrastructure(
        ex: InternalAuthenticationServiceException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> = handleUnexpected(ex, request)

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(ex: AuthenticationException, request: HttpServletRequest): ResponseEntity<ApiError> =
        build(
            HttpStatus.UNAUTHORIZED, "No autorizado",
            "Se requiere una sesión válida para acceder a este recurso", request,
        )

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException, request: HttpServletRequest): ResponseEntity<ApiError> =
        build(
            HttpStatus.FORBIDDEN, "Acceso denegado",
            "No tienes permisos para realizar esta acción", request,
        )

    // ── 404 / 405 / 409 ─────────────────────────────────────────────────────

    @ExceptionHandler(EntityNotFundException::class)
    fun handleEntityNotFound(ex: EntityNotFundException, request: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.NOT_FOUND, "Entidad no encontrada", ex.message, request)

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(ex: NoResourceFoundException, request: HttpServletRequest): ResponseEntity<ApiError> =
        build(HttpStatus.NOT_FOUND, "Recurso no encontrado", "La ruta solicitada no existe", request)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> = build(HttpStatus.METHOD_NOT_ALLOWED, "Método no permitido", ex.message, request)

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException, request: HttpServletRequest): ResponseEntity<ApiError> {
        log.warn("Data integrity violation: {}", ex.mostSpecificCause.message)
        return build(
            HttpStatus.CONFLICT, "Conflicto de datos",
            "La operación entra en conflicto con datos existentes o relacionados", request,
        )
    }

    // ── 500 ─────────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception, request: HttpServletRequest): ResponseEntity<ApiError> {
        log.error("Unhandled exception correlationId={}", correlationId(request), ex)
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
            "Ocurrió un error inesperado. Referencia: ${correlationId(request)}", request,
        )
    }

    private fun build(
        status: HttpStatus,
        title: String,
        message: String?,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        val body = ApiError(LocalDateTime.now(), status.value(), title, message, correlationId(request))
        return ResponseEntity.status(status).body(body)
    }

    private fun correlationId(request: HttpServletRequest): String =
        request.getAttribute(RequestCorrelationFilter.CORRELATION_ID_KEY)?.toString() ?: "N/A"

    private companion object {
        val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}
