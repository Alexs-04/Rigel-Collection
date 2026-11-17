package com.korebit.rigel.service.jwt

import com.korebit.rigel.enums.TokenType
import com.korebit.rigel.model.beans.Consumer
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import java.util.Objects
import java.util.UUID
import javax.crypto.SecretKey

/**
 * Issues and parses the JWTs used by the API.
 *
 * Every token carries a random `jti` (persisted server side so it can be revoked)
 * and a `token_type` claim ([CLAIM_TOKEN_TYPE]) that distinguishes access tokens from refresh
 * tokens, so a refresh token can never be used to call the API and vice versa.
 */
@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @param:Value("\${jwt.expiration}") private val accessTokenTtlMs: Long,
    @param:Value("\${jwt.refresh}") private val refreshTokenTtlMs: Long,
) {

    private val secretKey: SecretKey = buildKey(secret)

    init {
        check(accessTokenTtlMs > 0 && refreshTokenTtlMs > 0) { "jwt.expiration and jwt.refresh must be positive" }
        check(refreshTokenTtlMs > accessTokenTtlMs) { "jwt.refresh must be greater than jwt.expiration" }
    }

    /**
     * A signed token together with the metadata needed to persist it.
     *
     * @property value     compact JWT sent to the client
     * @property id        the token `jti`, used as the persisted lookup key
     * @property expiresAt absolute expiration instant
     */
    data class IssuedToken(val value: String, val id: String, val expiresAt: Instant)

    fun generateAccessToken(consumer: Consumer): IssuedToken = buildToken(consumer, TokenType.BEARER, accessTokenTtlMs)

    fun generateRefreshToken(consumer: Consumer): IssuedToken =
        buildToken(consumer, TokenType.REFRESH, refreshTokenTtlMs)

    /**
     * Verifies signature and expiration and returns the claims.
     *
     * @throws io.jsonwebtoken.JwtException when the token is malformed, tampered or expired
     * @throws IllegalArgumentException     when the token is null or blank
     */
    fun parseClaims(token: String?): Claims =
        Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

    /**
     * Same as [parseClaims] but still returns the claims of an expired token.
     * The signature is always verified. Useful for logout, where an expired access token
     * must still identify the session to revoke.
     */
    fun parseClaimsAllowingExpired(token: String?): Claims =
        try {
            parseClaims(token)
        } catch (expired: ExpiredJwtException) {
            expired.claims
        }

    /**
     * @return the token kind declared in the claims, or `null` when absent/unknown.
     */
    fun extractTokenType(claims: Claims): TokenType? {
        val raw = claims[CLAIM_TOKEN_TYPE] ?: return null
        return try {
            TokenType.valueOf(raw.toString())
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun extractUsername(token: String?): String? = parseClaims(token).subject

    private fun buildToken(consumer: Consumer, type: TokenType, ttlMs: Long): IssuedToken {
        val consumerId = Objects.requireNonNull(consumer.id, "Consumer id is required to issue a token")
        val tokenId = UUID.randomUUID().toString()
        val issuedAt = Instant.now()
        val expiresAt = issuedAt.plusMillis(ttlMs)

        val value = Jwts.builder()
            .id(tokenId)
            .subject(consumer.email)
            .claim(CLAIM_TOKEN_TYPE, type.name)
            .claim(CLAIM_USER_ID, consumerId)
            .claim("name", consumer.name)
            .claim("role", consumer.role.name)
            .claim("active", consumer.active)
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()

        return IssuedToken(value, tokenId, expiresAt)
    }

    companion object {
        const val CLAIM_TOKEN_TYPE = "token_type"
        const val CLAIM_USER_ID = "uid"
        private const val MIN_KEY_BYTES = 32

        private fun buildKey(secret: String): SecretKey {
            check(secret.isNotBlank()) { "jwt.secret must be configured" }
            val keyBytes = Decoders.BASE64.decode(secret.trim())
            check(keyBytes.size >= MIN_KEY_BYTES) {
                "jwt.secret must be a Base64 value of at least $MIN_KEY_BYTES bytes (256 bits)"
            }
            return Keys.hmacShaKeyFor(keyBytes)
        }
    }
}
