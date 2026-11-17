package com.korebit.rigel.service

import com.korebit.rigel.dto.request.LoginRequest
import com.korebit.rigel.dto.response.TokenResponse
import com.korebit.rigel.enums.TokenType
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.service.jwt.JwtService
import io.jsonwebtoken.JwtException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Authentication workflows: login, refresh-token rotation and logout.
 *
 * Every failure is reported as a Spring Security [org.springframework.security.core.AuthenticationException]
 * so the API answers `401` consistently.
 */
@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val consumerRepository: ConsumerRepository,
    private val sessionTokenService: SessionTokenService,
) {

    /**
     * Validates credentials and opens a new session, revoking any previous one.
     *
     * @throws BadCredentialsException when the credentials are missing or wrong.
     * @throws DisabledException when the account is inactive.
     */
    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        val email = request.email?.trim().orEmpty()
        val password = request.password.orEmpty()
        if (email.isEmpty() || password.isEmpty()) {
            throw BadCredentialsException(INVALID_CREDENTIALS)
        }

        authenticationManager.authenticate(UsernamePasswordAuthenticationToken(email, password))

        val consumer = consumerRepository.findByEmail(email)
            ?: throw BadCredentialsException(INVALID_CREDENTIALS)
        if (!consumer.active) {
            throw DisabledException(INACTIVE_USER)
        }
        return sessionTokenService.startSession(consumer)
    }

    /**
     * Exchanges a valid refresh token for a new token pair. The used refresh token is
     * revoked (rotation), so replaying it fails.
     *
     * @throws BadCredentialsException when the refresh token is invalid, expired, revoked or not a refresh token.
     * @throws DisabledException when the account was deactivated meanwhile.
     */
    @Transactional
    fun refresh(refreshToken: String?): TokenResponse {
        if (refreshToken.isNullOrBlank()) throw BadCredentialsException(INVALID_REFRESH)

        val claims = try {
            jwtService.parseClaims(refreshToken)
        } catch (_: JwtException) {
            throw BadCredentialsException(INVALID_REFRESH)
        } catch (_: IllegalArgumentException) {
            throw BadCredentialsException(INVALID_REFRESH)
        }

        if (jwtService.extractTokenType(claims) != TokenType.REFRESH) {
            throw BadCredentialsException(INVALID_REFRESH)
        }

        val stored = sessionTokenService.findActive(claims.id, TokenType.REFRESH)
            ?: throw BadCredentialsException(INVALID_REFRESH)
        val consumer = stored.consumer ?: throw BadCredentialsException(INVALID_REFRESH)

        if (!consumer.email.equals(claims.subject, ignoreCase = true)) {
            throw BadCredentialsException(INVALID_REFRESH)
        }
        if (!consumer.active) {
            sessionTokenService.revokeAllSessions(consumer)
            throw DisabledException(INACTIVE_USER)
        }

        return sessionTokenService.startSession(consumer)
    }

    /**
     * Revokes every session of the user that owns the given access token. Idempotent:
     * unknown, malformed or already revoked tokens are ignored. Expired access tokens are
     * accepted (signature is still verified) so the refresh token can be revoked too.
     */
    @Transactional
    fun logout(authorizationHeader: String?) {
        val token = authorizationHeader
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.substring(BEARER_PREFIX.length)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return

        val claims = try {
            jwtService.parseClaimsAllowingExpired(token)
        } catch (_: JwtException) {
            return
        } catch (_: IllegalArgumentException) {
            return
        }

        if (jwtService.extractTokenType(claims) != TokenType.BEARER) return
        val consumer = consumerRepository.findByEmail(claims.subject ?: return) ?: return
        sessionTokenService.revokeAllSessions(consumer)
    }

    companion object {
        const val BEARER_PREFIX = "Bearer "
        const val INVALID_CREDENTIALS = "Credenciales inválidas"
        const val INVALID_REFRESH = "La sesión expiró. Inicia sesión nuevamente"
        const val INACTIVE_USER = "Usuario inactivo. Contacta a un administrador"
    }
}
