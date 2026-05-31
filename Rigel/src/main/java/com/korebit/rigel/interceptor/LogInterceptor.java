package com.korebit.rigel.interceptor;

import com.korebit.rigel.filter.RequestCorrelationFilter;
import com.korebit.rigel.service.SystemMovementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Logs request lifecycle details and optionally records system movements for auditing.
 *
 * <p>The interceptor logs start/end events with correlation ID, handler, status, and timing
 * information. It also persists movements for error responses or mutating HTTP methods, while
 * excluding the logs endpoint to avoid recursive persistence.</p>
 */
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);
    private static final String START_TIME_ATTR = "requestStartNano";
    private static final String LOGS_PATH_PREFIX = "/logs";
    private final SystemMovementService systemMovementService;

    public LogInterceptor(SystemMovementService systemMovementService) {
        this.systemMovementService = systemMovementService;
    }

    /**
     * Stores request start time and logs a debug-level start event when enabled.
     *
     * @return true to continue request processing
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        request.setAttribute(START_TIME_ATTR, System.nanoTime());

        if (log.isDebugEnabled()) {
            log.debug(
                    "request_start correlationId={} method={} path={} user={} handler={}",
                    getCorrelationId(request),
                    request.getMethod(),
                    request.getRequestURI(),
                    getUsername(),
                    resolveHandler(handler)
            );
        }

        return true;
    }

    /**
     * Logs the end of the request and persists an audit movement when applicable.
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {

        long durationMs = resolveDurationMs(request);
        int status = response.getStatus();

        String message = "request_end correlationId={} method={} path={} status={} durationMs={} user={} handler={}";

        if (ex != null || status >= 500) {
            log.error(message,
                    getCorrelationId(request),
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMs,
                    getUsername(),
                    resolveHandler(handler),
                    ex
            );
            if (shouldPersistMovement(request, response, ex)) {
                persistMovement(request, response, durationMs);
            }
            return;
        }

        if (status >= 400) {
            log.warn(message,
                    getCorrelationId(request),
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMs,
                    getUsername(),
                    resolveHandler(handler)
            );
            if (shouldPersistMovement(request, response, ex)) {
                persistMovement(request, response, durationMs);
            }
            return;
        }

        log.info(message,
                getCorrelationId(request),
                request.getMethod(),
                request.getRequestURI(),
                status,
                durationMs,
                getUsername(),
                resolveHandler(handler)
        );

        if (shouldPersistMovement(request, response, ex)) {
            persistMovement(request, response, durationMs);
        }
    }

    /**
     * Determines whether the request should be persisted as a system movement.
     */
    private boolean shouldPersistMovement(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        String path = request.getRequestURI();
        if (path != null && path.startsWith(LOGS_PATH_PREFIX)) {
            return false;
        }

        int status = response.getStatus();
        if (ex != null || status >= 400) {
            return true;
        }

        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    /**
     * Persists the movement and guards against persistence failures.
     */
    private void persistMovement(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        try {
            systemMovementService.recordMovement(
                    getUsername(),
                    getRole(),
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    Math.max(durationMs, 0),
                    getCorrelationId(request)
            );
        } catch (RuntimeException persistenceError) {
            log.warn("movement_persist_failed correlationId={} method={} path={} reason={}",
                    getCorrelationId(request),
                    request.getMethod(),
                    request.getRequestURI(),
                    persistenceError.getMessage());
        }
    }

    /**
     * Resolves the request duration in milliseconds from the stored start time.
     */
    private long resolveDurationMs(HttpServletRequest request) {
        Object startAttr = request.getAttribute(START_TIME_ATTR);
        if (startAttr instanceof Long startTime) {
            return (System.nanoTime() - startTime) / 1_000_000;
        }
        return -1;
    }

    /**
     * Resolves a readable handler identifier for logging.
     */
    private String resolveHandler(Object handler) {
        if (handler instanceof HandlerMethod method) {
            return method.getBeanType().getSimpleName() + "#" + method.getMethod().getName();
        }
        return handler.getClass().getSimpleName();
    }

    /**
     * Returns the correlation ID stored by {@link RequestCorrelationFilter}.
     */
    private String getCorrelationId(HttpServletRequest request) {
        Object correlationId = request.getAttribute(RequestCorrelationFilter.CORRELATION_ID_KEY);
        return correlationId != null ? correlationId.toString() : "N/A";
    }

    /**
     * Returns the authenticated username or {@code anonymous}.
     */
    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return authentication.getName();
    }

    /**
     * Returns the first ROLE_* authority without the prefix, or a fallback.
     */
    private String getRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "ANONYMOUS";
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String value = authority.getAuthority();
            if (value != null && value.startsWith("ROLE_")) {
                return value.substring("ROLE_".length());
            }
        }
        return "UNKNOWN";
    }
}