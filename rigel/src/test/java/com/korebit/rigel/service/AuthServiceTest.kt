package com.korebit.rigel.service

import com.korebit.rigel.any
import com.korebit.rigel.dto.request.LoginRequest
import com.korebit.rigel.dto.response.TokenResponse
import com.korebit.rigel.enums.Role
import com.korebit.rigel.enums.TokenType
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.model.beans.Token
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.service.jwt.JwtService
import com.korebit.rigel.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@ExtendWith(MockitoExtension::class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var consumerRepository: ConsumerRepository

    @Mock
    private lateinit var sessionTokenService: SessionTokenService

    private val jwtService = JwtService(SECRET, 60_000, 120_000)
    private lateinit var authService: AuthService
    private lateinit var consumer: Consumer

    @BeforeEach
    fun setUp() {
        authService = AuthService(authenticationManager, jwtService, consumerRepository, sessionTokenService)

        consumer = Consumer(
            id = 1L,
            name = "Test User",
            email = "test@example.com",
            username = "testuser",
            role = Role.ADMIN,
            active = true,
        )
    }

    private fun stored(jti: String, type: TokenType) = Token(token = jti, tokenType = type, consumer = consumer)

    // ── login ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid credentials open a new session")
    fun testLoginSuccess() {
        val tokens = TokenResponse("access", "refresh")
        whenever(consumerRepository.findByEmail("test@example.com")).thenReturn(consumer)
        whenever(sessionTokenService.startSession(consumer)).thenReturn(tokens)

        assertSame(tokens, authService.login(LoginRequest(" test@example.com ", "password123")))
        verify(authenticationManager).authenticate(
            UsernamePasswordAuthenticationToken("test@example.com", "password123")
        )
    }

    @Test
    @DisplayName("Wrong credentials propagate BadCredentialsException (401) and open no session")
    fun testLoginInvalidCredentials() {
        whenever(authenticationManager.authenticate(any<Authentication>()))
            .thenThrow(BadCredentialsException("Bad credentials"))

        assertFailsWith<BadCredentialsException> {
            authService.login(LoginRequest("test@example.com", "wrong"))
        }
        verifyNoInteractions(sessionTokenService)
    }

    @Test
    @DisplayName("Missing email or password is rejected before hitting the authentication manager")
    fun testLoginMissingFields() {
        assertFailsWith<BadCredentialsException> { authService.login(LoginRequest(null, "x")) }
        assertFailsWith<BadCredentialsException> { authService.login(LoginRequest("a@b.c", "")) }
        verifyNoInteractions(authenticationManager, sessionTokenService)
    }

    @Test
    @DisplayName("Inactive user cannot log in")
    fun testLoginInactiveUser() {
        consumer.active = false
        whenever(consumerRepository.findByEmail("test@example.com")).thenReturn(consumer)

        assertFailsWith<DisabledException> {
            authService.login(LoginRequest("test@example.com", "password123"))
        }
        verifyNoInteractions(sessionTokenService)
    }

    // ── refresh ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("A valid, persisted refresh token is rotated into a new pair")
    fun testRefreshSuccess() {
        val refresh = jwtService.generateRefreshToken(consumer)
        val rotated = TokenResponse("new-access", "new-refresh")
        whenever(sessionTokenService.findActive(refresh.id, TokenType.REFRESH))
            .thenReturn(stored(refresh.id, TokenType.REFRESH))
        whenever(sessionTokenService.startSession(consumer)).thenReturn(rotated)

        assertSame(rotated, authService.refresh(refresh.value))
    }

    @Test
    @DisplayName("An access token cannot be used as refresh token")
    fun testRefreshWithAccessToken() {
        val access = jwtService.generateAccessToken(consumer).value

        assertFailsWith<BadCredentialsException> { authService.refresh(access) }
        verify(sessionTokenService, never()).startSession(any())
    }

    @Test
    @DisplayName("A revoked or unknown refresh token is rejected (replay protection)")
    fun testRefreshRevoked() {
        val refresh = jwtService.generateRefreshToken(consumer)
        whenever(sessionTokenService.findActive(refresh.id, TokenType.REFRESH)).thenReturn(null)

        assertFailsWith<BadCredentialsException> { authService.refresh(refresh.value) }
    }

    @Test
    @DisplayName("Garbage or empty refresh tokens are rejected")
    fun testRefreshGarbage() {
        assertFailsWith<BadCredentialsException> { authService.refresh("garbage") }
        assertFailsWith<BadCredentialsException> { authService.refresh("") }
        assertFailsWith<BadCredentialsException> { authService.refresh(null) }
    }

    @Test
    @DisplayName("Refreshing for a deactivated user fails and closes their sessions")
    fun testRefreshInactiveUser() {
        val refresh = jwtService.generateRefreshToken(consumer)
        consumer.active = false
        whenever(sessionTokenService.findActive(refresh.id, TokenType.REFRESH))
            .thenReturn(stored(refresh.id, TokenType.REFRESH))

        assertFailsWith<DisabledException> { authService.refresh(refresh.value) }
        verify(sessionTokenService).revokeAllSessions(consumer)
        verify(sessionTokenService, never()).startSession(any())
    }

    // ── logout ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Logout revokes every session of the token owner")
    fun testLogout() {
        val access = jwtService.generateAccessToken(consumer).value
        whenever(consumerRepository.findByEmail("test@example.com")).thenReturn(consumer)

        authService.logout("Bearer $access")

        verify(sessionTokenService).revokeAllSessions(consumer)
    }

    @Test
    @DisplayName("Logout ignores missing, malformed or refresh tokens")
    fun testLogoutIgnoresInvalidInput() {
        authService.logout(null)
        authService.logout("Basic abc")
        authService.logout("Bearer garbage")
        authService.logout("Bearer ${jwtService.generateRefreshToken(consumer).value}")

        verifyNoInteractions(sessionTokenService, consumerRepository)
    }

    private companion object {
        const val SECRET = "dGVzdC1vbmx5LWtleS1yaWdlbC1qd3QtMjU2LWJpdHMtbWluaW11bS0xMjM0NTY="
    }
}
