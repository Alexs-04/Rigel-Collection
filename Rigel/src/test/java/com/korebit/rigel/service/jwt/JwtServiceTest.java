package com.korebit.rigel.service.jwt;

import com.korebit.rigel.enums.Role;
import com.korebit.rigel.model.beans.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService.
 * Tests JWT generation, validation, and claim extraction.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private Consumer testConsumer;

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
    }

    @Test
    @DisplayName("Should generate a valid JWT token")
    void testGenerateToken() {
        String token = jwtService.generateToken(testConsumer);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("Should generate a valid refresh token")
    void testGenerateRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(testConsumer);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isBlank());
        assertTrue(refreshToken.contains("."));
    }

    @Test
    @DisplayName("Should extract username from token")
    void testExtractUsername() {
        String token = jwtService.generateToken(testConsumer);
        String username = jwtService.extractUsername(token);

        assertEquals(testConsumer.getEmail(), username);
    }

    @Test
    @DisplayName("Should extract username from token successfully")
    void testExtractUsernameSuccess() {
        String token = jwtService.generateToken(testConsumer);
        String username = jwtService.extractUsername(token);

        assertEquals(testConsumer.getEmail(), username);
    }

    @Test
    @DisplayName("Should throw exception for null consumer ID")
    void testGenerateTokenWithNullConsumerId() {
        Consumer consumerWithNullId = new Consumer();
        consumerWithNullId.setEmail("test@example.com");
        consumerWithNullId.setName("Test");

        assertThrows(NullPointerException.class, () -> jwtService.generateToken(consumerWithNullId));
    }
}

