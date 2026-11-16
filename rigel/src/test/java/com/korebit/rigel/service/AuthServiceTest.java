package com.korebit.rigel.service;

import com.korebit.rigel.dto.request.LoginRequest;
import com.korebit.rigel.dto.response.TokenResponse;
import com.korebit.rigel.enums.Role;
import com.korebit.rigel.enums.TokenType;
import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.model.beans.Token;
import com.korebit.rigel.repository.ConsumerRepository;
import com.korebit.rigel.service.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    private static final String SECRET = "dGVzdC1vbmx5LWtleS1yaWdlbC1qd3QtMjU2LWJpdHMtbWluaW11bS0xMjM0NTY=";

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private ConsumerRepository consumerRepository;
    @Mock
    private SessionTokenService sessionTokenService;

    private final JwtService jwtService = new JwtService(SECRET, 60_000, 120_000);
    private AuthService authService;
    private Consumer consumer;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, jwtService, consumerRepository, sessionTokenService);

        consumer = new Consumer();
        consumer.setId(1L);
        consumer.setName("Test User");
        consumer.setEmail("test@example.com");
        consumer.setUsername("testuser");
        consumer.setRole(Role.ADMIN);
        consumer.setActive(true);
    }

    private Token stored(String jti, TokenType type) {
        Token token = new Token();
        token.setToken(jti);
        token.setTokenType(type);
        token.setConsumer(consumer);
        return token;
    }

    // ── login ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Valid credentials open a new session")
    void testLoginSuccess() {
        TokenResponse tokens = new TokenResponse("access", "refresh");
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(consumer);
        when(sessionTokenService.startSession(consumer)).thenReturn(tokens);

        assertSame(tokens, authService.login(new LoginRequest(" test@example.com ", "password123")));
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("test@example.com", "password123"));
    }

    @Test
    @DisplayName("Wrong credentials propagate BadCredentialsException (401) and open no session")
    void testLoginInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new LoginRequest("test@example.com", "wrong")));
        verifyNoInteractions(sessionTokenService);
    }

    @Test
    @DisplayName("Missing email or password is rejected before hitting the authentication manager")
    void testLoginMissingFields() {
        assertThrows(BadCredentialsException.class, () -> authService.login(new LoginRequest(null, "x")));
        assertThrows(BadCredentialsException.class, () -> authService.login(new LoginRequest("a@b.c", "")));
        verifyNoInteractions(authenticationManager, sessionTokenService);
    }

    @Test
    @DisplayName("Inactive user cannot log in")
    void testLoginInactiveUser() {
        consumer.setActive(false);
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(consumer);

        assertThrows(DisabledException.class,
                () -> authService.login(new LoginRequest("test@example.com", "password123")));
        verifyNoInteractions(sessionTokenService);
    }

    // ── refresh ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("A valid, persisted refresh token is rotated into a new pair")
    void testRefreshSuccess() {
        JwtService.IssuedToken refresh = jwtService.generateRefreshToken(consumer);
        TokenResponse rotated = new TokenResponse("new-access", "new-refresh");
        when(sessionTokenService.findActive(refresh.id(), TokenType.REFRESH)).thenReturn(stored(refresh.id(), TokenType.REFRESH));
        when(sessionTokenService.startSession(consumer)).thenReturn(rotated);

        assertSame(rotated, authService.refresh(refresh.value()));
    }

    @Test
    @DisplayName("An access token cannot be used as refresh token")
    void testRefreshWithAccessToken() {
        String access = jwtService.generateAccessToken(consumer).value();

        assertThrows(BadCredentialsException.class, () -> authService.refresh(access));
        verify(sessionTokenService, never()).startSession(any());
    }

    @Test
    @DisplayName("A revoked or unknown refresh token is rejected (replay protection)")
    void testRefreshRevoked() {
        JwtService.IssuedToken refresh = jwtService.generateRefreshToken(consumer);
        when(sessionTokenService.findActive(refresh.id(), TokenType.REFRESH)).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> authService.refresh(refresh.value()));
    }

    @Test
    @DisplayName("Garbage or empty refresh tokens are rejected")
    void testRefreshGarbage() {
        assertThrows(BadCredentialsException.class, () -> authService.refresh("garbage"));
        assertThrows(BadCredentialsException.class, () -> authService.refresh(""));
        assertThrows(BadCredentialsException.class, () -> authService.refresh(null));
    }

    @Test
    @DisplayName("Refreshing for a deactivated user fails and closes their sessions")
    void testRefreshInactiveUser() {
        JwtService.IssuedToken refresh = jwtService.generateRefreshToken(consumer);
        consumer.setActive(false);
        when(sessionTokenService.findActive(refresh.id(), TokenType.REFRESH)).thenReturn(stored(refresh.id(), TokenType.REFRESH));

        assertThrows(DisabledException.class, () -> authService.refresh(refresh.value()));
        verify(sessionTokenService).revokeAllSessions(consumer);
        verify(sessionTokenService, never()).startSession(any());
    }

    // ── logout ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Logout revokes every session of the token owner")
    void testLogout() {
        String access = jwtService.generateAccessToken(consumer).value();
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(consumer);

        authService.logout("Bearer " + access);

        verify(sessionTokenService).revokeAllSessions(consumer);
    }

    @Test
    @DisplayName("Logout ignores missing, malformed or refresh tokens")
    void testLogoutIgnoresInvalidInput() {
        authService.logout(null);
        authService.logout("Basic abc");
        authService.logout("Bearer garbage");
        authService.logout("Bearer " + jwtService.generateRefreshToken(consumer).value());

        verifyNoInteractions(sessionTokenService, consumerRepository);
    }
}
