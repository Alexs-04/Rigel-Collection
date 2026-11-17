package com.korebit.rigel.service.jwt;

import com.korebit.rigel.enums.TokenType;
import com.korebit.rigel.model.beans.Consumer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Issues and parses the JWTs used by the API.
 *
 * <p>Every token carries a random {@code jti} (persisted server side so it can be revoked)
 * and a {@value #CLAIM_TOKEN_TYPE} claim that distinguishes access tokens from refresh tokens,
 * so a refresh token can never be used to call the API and vice versa.</p>
 */
@Service
public class JwtService {

    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String CLAIM_USER_ID = "uid";
    private static final int MIN_KEY_BYTES = 32;

    private final SecretKey secretKey;
    private final long accessTokenTtlMs;
    private final long refreshTokenTtlMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long accessTokenTtlMs,
            @Value("${jwt.refresh}") long refreshTokenTtlMs
    ) {
        this.secretKey = buildKey(secret);
        if (accessTokenTtlMs <= 0 || refreshTokenTtlMs <= 0) {
            throw new IllegalStateException("jwt.expiration and jwt.refresh must be positive");
        }
        if (refreshTokenTtlMs <= accessTokenTtlMs) {
            throw new IllegalStateException("jwt.refresh must be greater than jwt.expiration");
        }
        this.accessTokenTtlMs = accessTokenTtlMs;
        this.refreshTokenTtlMs = refreshTokenTtlMs;
    }

    /**
     * A signed token together with the metadata needed to persist it.
     *
     * @param value     compact JWT sent to the client
     * @param id        the token {@code jti}, used as the persisted lookup key
     * @param expiresAt absolute expiration instant
     */
    public record IssuedToken(String value, String id, Instant expiresAt) {
    }

    public IssuedToken generateAccessToken(final Consumer consumer) {
        return buildToken(consumer, TokenType.BEARER, accessTokenTtlMs);
    }

    public IssuedToken generateRefreshToken(final Consumer consumer) {
        return buildToken(consumer, TokenType.REFRESH, refreshTokenTtlMs);
    }

    /**
     * Verifies signature and expiration and returns the claims.
     *
     * @throws JwtException             when the token is malformed, tampered or expired
     * @throws IllegalArgumentException when the token is null or blank
     */
    public Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Same as {@link #parseClaims(String)} but still returns the claims of an expired token.
     * The signature is always verified. Useful for logout, where an expired access token
     * must still identify the session to revoke.
     */
    public Claims parseClaimsAllowingExpired(final String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException expired) {
            return expired.getClaims();
        }
    }

    /**
     * @return the token kind declared in the claims, or {@code null} when absent/unknown.
     */
    public TokenType extractTokenType(final Claims claims) {
        Object raw = claims.get(CLAIM_TOKEN_TYPE);
        if (raw == null) {
            return null;
        }
        try {
            return TokenType.valueOf(raw.toString());
        } catch (IllegalArgumentException unknown) {
            return null;
        }
    }

    public String extractUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    private IssuedToken buildToken(final Consumer consumer, final TokenType type, final long ttlMs) {
        Objects.requireNonNull(consumer.getId(), "Consumer id is required to issue a token");
        final String tokenId = UUID.randomUUID().toString();
        final Instant issuedAt = Instant.now();
        final Instant expiresAt = issuedAt.plusMillis(ttlMs);

        final String value = Jwts.builder()
                .id(tokenId)
                .subject(consumer.getEmail())
                .claim(CLAIM_TOKEN_TYPE, type.name())
                .claim(CLAIM_USER_ID, consumer.getId())
                .claim("name", consumer.getName())
                .claim("role", consumer.getRole().name())
                .claim("active", consumer.getActive())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        return new IssuedToken(value, tokenId, expiresAt);
    }

    private static SecretKey buildKey(final String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret must be configured");
        }
        final byte[] keyBytes = Decoders.BASE64.decode(secret.trim());
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret must be a Base64 value of at least " + MIN_KEY_BYTES + " bytes (256 bits)");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
