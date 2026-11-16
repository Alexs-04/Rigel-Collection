package com.korebit.rigel.service.jwt;

import com.korebit.rigel.enums.Role;
import com.korebit.rigel.enums.TokenType;
import com.korebit.rigel.model.beans.Consumer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private static final String SECRET = "dGVzdC1vbmx5LWtleS1yaWdlbC1qd3QtMjU2LWJpdHMtbWluaW11bS0xMjM0NTY=";

    private JwtService jwtService;
    private Consumer consumer;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 60_000, 120_000);

        consumer = new Consumer();
        consumer.setId(7L);
        consumer.setName("José Núñez");
        consumer.setEmail("test@example.com");
        consumer.setUsername("testuser");
        consumer.setRole(Role.ADMIN);
        consumer.setActive(true);
    }

    @Test
    @DisplayName("Access token carries subject, role, user id, a random jti and token_type=BEARER")
    void testAccessTokenClaims() {
        JwtService.IssuedToken issued = jwtService.generateAccessToken(consumer);
        Claims claims = jwtService.parseClaims(issued.value());

        assertEquals("test@example.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
        assertEquals("José Núñez", claims.get("name"));
        assertEquals(7, ((Number) claims.get(JwtService.CLAIM_USER_ID)).intValue());
        assertEquals(issued.id(), claims.getId());
        assertEquals(TokenType.BEARER, jwtService.extractTokenType(claims));
        assertNotEquals(issued.id(), jwtService.generateAccessToken(consumer).id(), "jti must be unique per token");
    }

    @Test
    @DisplayName("Refresh token is typed REFRESH and lives longer than the access token")
    void testRefreshToken() {
        JwtService.IssuedToken access = jwtService.generateAccessToken(consumer);
        JwtService.IssuedToken refresh = jwtService.generateRefreshToken(consumer);

        assertEquals(TokenType.REFRESH, jwtService.extractTokenType(jwtService.parseClaims(refresh.value())));
        assertTrue(refresh.expiresAt().isAfter(access.expiresAt()));
    }

    @Test
    @DisplayName("A token signed with another key is rejected")
    void testForeignSignature() {
        String forged = Jwts.builder()
                .subject("test@example.com")
                .claim(JwtService.CLAIM_TOKEN_TYPE, "BEARER")
                .signWith(Keys.hmacShaKeyFor("another-secret-key-that-is-32-bytes-long!!".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(JwtException.class, () -> jwtService.parseClaims(forged));
    }

    @Test
    @DisplayName("An expired token is rejected, but its claims remain readable for logout")
    void testExpiredToken() {
        // Build an already-expired token signed with the same key (no sleeping in tests)
        String expired = Jwts.builder()
                .subject("test@example.com")
                .id("expired-jti")
                .claim(JwtService.CLAIM_TOKEN_TYPE, "BEARER")
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(SECRET)))
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtService.parseClaims(expired));
        assertEquals("expired-jti", jwtService.parseClaimsAllowingExpired(expired).getId());
    }

    @Test
    @DisplayName("Malformed tokens are rejected")
    void testMalformedToken() {
        assertThrows(JwtException.class, () -> jwtService.parseClaims("not-a-jwt"));
        assertThrows(IllegalArgumentException.class, () -> jwtService.parseClaims(""));
    }

    @Test
    @DisplayName("Issuing a token for a consumer without id fails fast")
    void testGenerateTokenWithNullConsumerId() {
        consumer.setId(null);
        assertThrows(NullPointerException.class, () -> jwtService.generateAccessToken(consumer));
    }

    @Test
    @DisplayName("Weak or missing secrets are rejected at startup")
    void testWeakSecretRejected() {
        assertThrows(IllegalStateException.class, () -> new JwtService("c2hvcnQ=", 60_000, 120_000));
        assertThrows(IllegalStateException.class, () -> new JwtService(" ", 60_000, 120_000));
        assertThrows(IllegalStateException.class, () -> new JwtService(SECRET, 60_000, 60_000));
    }
}
