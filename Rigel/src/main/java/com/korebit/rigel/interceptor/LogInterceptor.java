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

@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);
    private static final String START_TIME_ATTR = "requestStartNano";
    private static final String LOGS_PATH_PREFIX = "/logs";
    private final SystemMovementService systemMovementService;

    public LogInterceptor(SystemMovementService systemMovementService) {
        this.systemMovementService = systemMovementService;
    }

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

    private long resolveDurationMs(HttpServletRequest request) {
        Object startAttr = request.getAttribute(START_TIME_ATTR);
        if (startAttr instanceof Long startTime) {
            return (System.nanoTime() - startTime) / 1_000_000;
        }
        return -1;
    }

    private String resolveHandler(Object handler) {
        if (handler instanceof HandlerMethod method) {
            return method.getBeanType().getSimpleName() + "#" + method.getMethod().getName();
        }
        return handler.getClass().getSimpleName();
    }

    private String getCorrelationId(HttpServletRequest request) {
        Object correlationId = request.getAttribute(RequestCorrelationFilter.CORRELATION_ID_KEY);
        return correlationId != null ? correlationId.toString() : "N/A";
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return authentication.getName();
    }

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