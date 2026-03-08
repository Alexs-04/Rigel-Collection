package com.korebit.rigel.interceptor;

import com.korebit.rigel.filter.RequestCorrelationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);
    private static final String START_TIME_ATTR = "requestStartNano";

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
}