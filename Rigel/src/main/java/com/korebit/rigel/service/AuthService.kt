package com.korebit.rigel.service

import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.model.beans.Token
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.repository.TokenRepository
import com.korebit.rigel.dto.request.LoginRequest
import com.korebit.rigel.dto.response.TokenResponse
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.service.jwt.JwtService
import com.korebit.rigel.util.SaveConsumerToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

/**
 * Handles authentication workflows such as login and token lifecycle management.
 *
 * This service validates credentials, checks user status, issues JWT/refresh tokens,
 * and revokes previously active tokens for the same consumer.
 */
@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val consumerRepository: ConsumerRepository,
    private val tokenRepository: TokenRepository,
    private val saveConsumerToken: SaveConsumerToken
) {

    /**
     * Authenticates a consumer using email and password and returns fresh tokens.
     *
     * The method also invalidates previous active tokens to enforce single active
     * session semantics for the consumer.
     *
     * @param request login payload with user credentials.
     * @return a [TokenResponse] containing a new JWT token and refresh token.
     * @throws EntityNotFundException when no consumer exists for the provided email.
     * @throws IllegalArgumentException when the consumer is inactive.
     */
    fun login(request: LoginRequest): TokenResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email ?: "",
                request.password ?: ""
            )
        )

        val consumer = consumerRepository.findByEmail(request.email ?: "")
            ?: throw EntityNotFundException("No consumer found")
        if (!consumer.active) {
            throw IllegalArgumentException("Usuario inactivo. Contacta a un administrador")
        }
        val jwtToken = jwtService.generateToken(consumer)
        val refreshToken = jwtService.generateRefreshToken(consumer)

        revokeAllUserTokens(consumer)
        saveConsumerToken.saveConsumerToken(consumer, jwtToken)
        return TokenResponse(jwtToken, refreshToken)
    }

    /**
     * Revokes and expires all currently active tokens for the given consumer.
     *
     * @param consumer authenticated consumer whose tokens must be invalidated.
     * @throws IllegalArgumentException when the consumer id is null.
     */
    private fun revokeAllUserTokens(consumer: Consumer) {
        val validConsumerTokens: MutableList<Token> = tokenRepository
            .findAllValidIsFalseOrRevokedIsFalseByConsumerId(
                consumer.id
                    ?: throw IllegalArgumentException("consumerId is null")
            )

        if (!validConsumerTokens.isEmpty()) {
            for (token in validConsumerTokens) {
                token.revoked = true
                token.expired = true
            }
            tokenRepository.saveAll(validConsumerTokens)
        }
    }

    /**
     * Validates the expected Bearer token format in an authorization header.
     *
     * @param authHeader raw Authorization header value.
     * @throws IllegalArgumentException when the header does not start with `Bearer `.
     */
    private fun refreshToken(authHeader: String) {
        if (!authHeader.startsWith("Bearer ")) {
            throw IllegalArgumentException("Invalid Bearer Token")
        }
    }
}