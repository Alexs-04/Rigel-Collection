package com.korebit.rigel.service;

import com.korebit.rigel.dto.request.LoginRequest;
import com.korebit.rigel.dto.response.TokenResponse;
import com.korebit.rigel.enums.Role;
import com.korebit.rigel.exception.EntityNotFundException;
import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.model.beans.Token;
import com.korebit.rigel.repository.ConsumerRepository;
import com.korebit.rigel.repository.TokenRepository;
import com.korebit.rigel.service.jwt.JwtService;
import com.korebit.rigel.util.SaveConsumerToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests login authentication, token generation, and token revocation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private SaveConsumerToken saveConsumerToken;

    @InjectMocks
    private AuthService authService;

    private Consumer testConsumer;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testConsumer = new Consumer();
        testConsumer.setId(1L);
        testConsumer.setName("Test User");
        testConsumer.setEmail("test@example.com");
        testConsumer.setUsername("testuser");
        testConsumer.setPassword("hashedPassword");
        testConsumer.setRole(Role.ADMIN);
        testConsumer.setActive(true);
        testConsumer.setPhoneNumber("1234567890");

        loginRequest = new LoginRequest("test@example.com", "password123");
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@example.com", "password123"));
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(testConsumer);
        when(jwtService.generateToken(testConsumer)).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(testConsumer)).thenReturn("refreshToken");
        when(tokenRepository.findAllValidIsFalseOrRevokedIsFalseByConsumerId(1L)).thenReturn(new ArrayList<>());

        TokenResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
        verify(saveConsumerToken, times(1)).saveConsumerToken(testConsumer, "jwtToken");
    }

    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void testLoginInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(consumerRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should throw exception when consumer not found")
    void testLoginConsumerNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@example.com", "password123"));
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(null);

        assertThrows(EntityNotFundException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Should throw exception when user is inactive")
    void testLoginInactiveUser() {
        testConsumer.setActive(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@example.com", "password123"));
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(testConsumer);

        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Should revoke all user tokens on login")
    void testRevokeAllUserTokensOnLogin() {
        Token token1 = new Token();
        token1.setId(1L);
        token1.setRevoked(false);
        token1.setExpired(false);

        Token token2 = new Token();
        token2.setId(2L);
        token2.setRevoked(false);
        token2.setExpired(false);

        List<Token> validTokens = new ArrayList<>();
        validTokens.add(token1);
        validTokens.add(token2);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@example.com", "password123"));
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(testConsumer);
        when(jwtService.generateToken(testConsumer)).thenReturn("jwtToken");
        when(jwtService.generateRefreshToken(testConsumer)).thenReturn("refreshToken");
        when(tokenRepository.findAllValidIsFalseOrRevokedIsFalseByConsumerId(1L)).thenReturn(validTokens);

        authService.login(loginRequest);

        verify(tokenRepository, times(1)).saveAll(any());
        assertTrue(token1.getRevoked());
        assertTrue(token1.getExpired());
        assertTrue(token2.getRevoked());
        assertTrue(token2.getExpired());
    }

    @Test
    @DisplayName("Should handle null email in login request")
    void testLoginWithNullEmail() {
        LoginRequest nullEmailRequest = new LoginRequest(null, "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(new UsernamePasswordAuthenticationToken("", "password123"));
        when(consumerRepository.findByEmail("")).thenReturn(null);

        assertThrows(EntityNotFundException.class, () -> authService.login(nullEmailRequest));
    }
}

