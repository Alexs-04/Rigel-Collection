package com.korebit.rigel.interceptor

import com.korebit.rigel.filter.RequestCorrelationFilter
import com.korebit.rigel.service.SystemMovementService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class LogInterceptor(
    private val systemMovementService: SystemMovementService,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        request.setAttribute(START_TIME_ATTR, System.nanoTime())

        if (log.isDebugEnabled) {
            log.debug(
                "request_start correlationId={} method={} path={} user={} handler={}",
                getCorrelationId(request),
                request.method,
                request.requestURI,
                getUsername(),
                resolveHandler(handler),
            )
        }

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        val durationMs = resolveDurationMs(request)
        val status = response.status

        val message = "request_end correlationId={} method={} path={} status={} durationMs={} user={} handler={}"
        val arguments = arrayOf<Any?>(
            getCorrelationId(request),
            request.method,
            request.requestURI,
            status,
            durationMs,
            getUsername(),
            resolveHandler(handler),
        )

        when {
            // The trailing exception is picked up by SLF4J as the throwable to log.
            ex != null || status >= 500 -> log.error(message, *arguments, ex)
            status >= 400 -> log.warn(message, *arguments)
            else -> log.info(message, *arguments)
        }

        if (shouldPersistMovement(request, response, ex)) {
            persistMovement(request, response, durationMs)
        }
    }

    private fun shouldPersistMovement(
        request: HttpServletRequest,
        response: HttpServletResponse,
        ex: Exception?,
    ): Boolean {
        val path = request.requestURI
        if (path != null && path.startsWith(LOGS_PATH_PREFIX)) return false

        if (ex != null || response.status >= 400) return true

        return request.method.uppercase() in MUTATING_METHODS
    }

    private fun persistMovement(request: HttpServletRequest, response: HttpServletResponse, durationMs: Long) {
        try {
            systemMovementService.recordMovement(
                getUsername(),
                getRole(),
                request.method,
                request.requestURI,
                response.status,
                maxOf(durationMs, 0),
                getCorrelationId(request),
            )
        } catch (persistenceError: RuntimeException) {
            log.warn(
                "movement_persist_failed correlationId={} method={} path={} reason={}",
                getCorrelationId(request),
                request.method,
                request.requestURI,
                persistenceError.message,
            )
        }
    }

    private fun resolveDurationMs(request: HttpServletRequest): Long {
        val startTime = request.getAttribute(START_TIME_ATTR) as? Long ?: return -1
        return (System.nanoTime() - startTime) / 1_000_000
    }

    private fun resolveHandler(handler: Any): String =
        if (handler is HandlerMethod) {
            "${handler.beanType.simpleName}#${handler.method.name}"
        } else {
            handler.javaClass.simpleName
        }

    private fun getCorrelationId(request: HttpServletRequest): String =
        request.getAttribute(RequestCorrelationFilter.CORRELATION_ID_KEY)?.toString() ?: "N/A"

    private fun currentAuthentication(): Authentication? =
        SecurityContextHolder.getContext().authentication
            ?.takeIf { it.isAuthenticated && it !is AnonymousAuthenticationToken }

    private fun getUsername(): String = currentAuthentication()?.name ?: "anonymous"

    private fun getRole(): String {
        val authentication = currentAuthentication() ?: return "ANONYMOUS"
        return authentication.authorities
            .mapNotNull { it.authority }
            .firstOrNull { it.startsWith(ROLE_PREFIX) }
            ?.removePrefix(ROLE_PREFIX)
            ?: "UNKNOWN"
    }

    private companion object {
        val log = LoggerFactory.getLogger(LogInterceptor::class.java)
        const val START_TIME_ATTR = "requestStartNano"
        const val LOGS_PATH_PREFIX = "/logs"
        const val ROLE_PREFIX = "ROLE_"
        val MUTATING_METHODS = setOf("POST", "PUT", "PATCH", "DELETE")
    }
}
