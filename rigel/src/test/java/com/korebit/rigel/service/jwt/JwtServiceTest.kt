package com.korebit.rigel.service.jwt

import com.korebit.rigel.enums.Role
import com.korebit.rigel.enums.TokenType
import com.korebit.rigel.model.beans.Consumer
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private lateinit var jwtService: JwtService
    private lateinit var consumer: Consumer

    @BeforeEach
    fun setUp() {
        jwtService = JwtService(SECRET, 60_000, 120_000)

        consumer = Consumer(
            id = 7L,
            name = "José Núñez",
            email = "test@example.com",
            username = "testuser",
            role = Role.ADMIN,
            active = true,
        )
    }

    @Test
    @DisplayName("Access token carries subject, role, user id, a random jti and token_type=BEARER")
    fun testAccessTokenClaims() {
        val issued = jwtService.generateAccessToken(consumer)
        val claims = jwtService.parseClaims(issued.value)

        assertEquals("test@example.com", claims.subject)
        assertEquals("ADMIN", claims["role"])
        assertEquals("José Núñez", claims["name"])
        assertEquals(7, (claims[JwtService.CLAIM_USER_ID] as Number).toInt())
        assertEquals(issued.id, claims.id)
        assertEquals(TokenType.BEARER, jwtService.extractTokenType(claims))
        assertNotEquals(issued.id, jwtService.generateAccessToken(consumer).id, "jti must be unique per token")
    }

    @Test
    @DisplayName("Refresh token is typed REFRESH and lives longer than the access token")
    fun testRefreshToken() {
        val access = jwtService.generateAccessToken(consumer)
        val refresh = jwtService.generateRefreshToken(consumer)

        assertEquals(TokenType.REFRESH, jwtService.extractTokenType(jwtService.parseClaims(refresh.value)))
        assertTrue(refresh.expiresAt.isAfter(access.expiresAt))
    }

    @Test
    @DisplayName("A token signed with another key is rejected")
    fun testForeignSignature() {
        val forged = Jwts.builder()
            .subject("test@example.com")
            .claim(JwtService.CLAIM_TOKEN_TYPE, "BEARER")
            .signWith(Keys.hmacShaKeyFor("another-secret-key-that-is-32-bytes-long!!".toByteArray(Charsets.UTF_8)))
            .compact()

        assertFailsWith<JwtException> { jwtService.parseClaims(forged) }
    }

    @Test
    @DisplayName("An expired token is rejected, but its claims remain readable for logout")
    fun testExpiredToken() {
        // Build an already-expired token signed with the same key (no sleeping in tests)
        val expired = Jwts.builder()
            .subject("test@example.com")
            .id("expired-jti")
            .claim(JwtService.CLAIM_TOKEN_TYPE, "BEARER")
            .expiration(Date(System.currentTimeMillis() - 1_000))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
            .compact()

        assertFailsWith<ExpiredJwtException> { jwtService.parseClaims(expired) }
        assertEquals("expired-jti", jwtService.parseClaimsAllowingExpired(expired).id)
    }

    @Test
    @DisplayName("Malformed tokens are rejected")
    fun testMalformedToken() {
        assertFailsWith<JwtException> { jwtService.parseClaims("not-a-jwt") }
        assertFailsWith<IllegalArgumentException> { jwtService.parseClaims("") }
    }

    @Test
    @DisplayName("Issuing a token for a consumer without id fails fast")
    fun testGenerateTokenWithNullConsumerId() {
        consumer.id = null
        assertFailsWith<NullPointerException> { jwtService.generateAccessToken(consumer) }
    }

    @Test
    @DisplayName("Weak or missing secrets are rejected at startup")
    fun testWeakSecretRejected() {
        assertFailsWith<IllegalStateException> { JwtService("c2hvcnQ=", 60_000, 120_000) }
        assertFailsWith<IllegalStateException> { JwtService(" ", 60_000, 120_000) }
        assertFailsWith<IllegalStateException> { JwtService(SECRET, 60_000, 60_000) }
    }

    private companion object {
        const val SECRET = "dGVzdC1vbmx5LWtleS1yaWdlbC1qd3QtMjU2LWJpdHMtbWluaW11bS0xMjM0NTY="
    }
}
