package com.korebit.rigel.util

import com.korebit.rigel.enums.TokenType
import com.korebit.rigel.service.SessionTokenService
import com.korebit.rigel.service.jwt.JwtService
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Stateless JWT authentication filter.
 *
 * A request is authenticated only when **all** of these hold:
 * 1. the `Authorization` header carries a `Bearer` token with a valid signature that is not expired;
 * 2. the token is an *access* token (refresh tokens are rejected here);
 * 3. its `jti` is persisted, not revoked and not expired;
 * 4. the user still exists and is enabled.
 *
 * Otherwise the request continues anonymously and Spring Security answers `401`
 * for protected endpoints. Requests under `/auth/` are skipped.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService,
    private val sessionTokenService: SessionTokenService,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.servletPath.startsWith("/auth/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (SecurityContextHolder.getContext().authentication == null) {
            authenticate(request)
        }
        filterChain.doFilter(request, response)
    }

    private fun authenticate(request: HttpServletRequest) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (header == null || !header.startsWith(BEARER_PREFIX)) return

        val claims = try {
            jwtService.parseClaims(header.substring(BEARER_PREFIX.length).trim())
        } catch (_: JwtException) {
            return
        } catch (_: IllegalArgumentException) {
            return
        }

        if (jwtService.extractTokenType(claims) != TokenType.BEARER) return

        val username = claims.subject
        if (username == null || !sessionTokenService.isActive(claims.id, TokenType.BEARER)) return

        val userDetails = try {
            userDetailsService.loadUserByUsername(username)
        } catch (_: UsernameNotFoundException) {
            return
        }
        if (!userDetails.isEnabled || !userDetails.isAccountNonLocked || !userDetails.isAccountNonExpired) return

        val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "
    }
}
