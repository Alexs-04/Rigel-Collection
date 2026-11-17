package com.korebit.rigel.service

import com.korebit.rigel.dto.response.TokenResponse
import com.korebit.rigel.enums.TokenType
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.model.beans.Token
import com.korebit.rigel.repository.TokenRepository
import com.korebit.rigel.service.jwt.JwtService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Single owner of the server-side session state (persisted access and refresh tokens).
 *
 * Tokens are stored by their `jti`, never the full JWT, so revocation works with a short,
 * indexed lookup key.
 */
@Service
class SessionTokenService(
    private val tokenRepository: TokenRepository,
    private val jwtService: JwtService,
) {

    /**
     * Revokes every open session of the consumer and issues a fresh access/refresh pair.
     */
    @Transactional
    fun startSession(consumer: Consumer): TokenResponse {
        revokeAllSessions(consumer)

        val access = jwtService.generateAccessToken(consumer)
        val refresh = jwtService.generateRefreshToken(consumer)

        tokenRepository.saveAll(
            listOf(
                toEntity(access, TokenType.BEARER, consumer),
                toEntity(refresh, TokenType.REFRESH, consumer),
            )
        )
        return TokenResponse(access.value, refresh.value)
    }

    /**
     * @return the persisted token when it exists, matches the expected type and is neither
     * revoked nor expired; `null` otherwise.
     */
    @Transactional(readOnly = true)
    fun findActive(tokenId: String?, type: TokenType): Token? {
        if (tokenId.isNullOrBlank()) return null
        val token = tokenRepository.findByTokenAndTokenType(tokenId, type) ?: return null
        if (token.revoked || token.expired) return null
        if (token.expiresAt?.isBefore(LocalDateTime.now()) == true) return null
        return token
    }

    @Transactional(readOnly = true)
    fun isActive(tokenId: String?, type: TokenType): Boolean = findActive(tokenId, type) != null

    /**
     * Invalidates every access and refresh token of the consumer (logout everywhere).
     */
    @Transactional
    fun revokeAllSessions(consumer: Consumer) {
        val consumerId = consumer.id ?: return
        val openTokens = tokenRepository.findAllByConsumerIdAndTokenTypeInAndRevokedFalse(consumerId, SESSION_TYPES)
        if (openTokens.isEmpty()) return

        openTokens.forEach { token ->
            token.revoked = true
            token.expired = true
        }
        tokenRepository.saveAll(openTokens)
    }

    private fun toEntity(issued: JwtService.IssuedToken, type: TokenType, consumer: Consumer) = Token(
        token = issued.id,
        tokenType = type,
        revoked = false,
        expired = false,
        expiresAt = LocalDateTime.ofInstant(issued.expiresAt, ZoneId.systemDefault()),
        consumer = consumer,
    )

    companion object {
        private val SESSION_TYPES = listOf(TokenType.BEARER, TokenType.REFRESH)
    }
}
