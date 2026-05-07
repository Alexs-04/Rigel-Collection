package com.korebit.rigel.service;

import com.korebit.rigel.dto.ConsumerDto;
import com.korebit.rigel.dto.request.UserStatusRequest;
import com.korebit.rigel.dto.request.UserUpsertRequest;
import com.korebit.rigel.dto.response.Response;
import com.korebit.rigel.enums.Role;
import com.korebit.rigel.exception.EntityNotFundException;
import com.korebit.rigel.model.beans.Consumer;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConsumerService.
 * Tests user creation, retrieval, update, and status management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumerService Tests")
class ConsumerServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private SaveConsumerToken saveConsumerToken;

    @InjectMocks
    private ConsumerService consumerService;

    private Consumer testConsumer;
    private ConsumerDto testConsumerDto;

    @BeforeEach
    void setUp() {
        testConsumer = new Consumer();
        testConsumer.setId(1L);
        testConsumer.setName("Test User");
        testConsumer.setEmail("test@example.com");
        testConsumer.setUsername("testuser");
        testConsumer.setPassword("hashedPassword");
        testConsumer.setRole(Role.USER);
        testConsumer.setActive(true);
        testConsumer.setPhoneNumber("1234567890");

        testConsumerDto = new ConsumerDto(
            "Test User",
            Role.USER,
            "testuser",
            "password123",
            "test@example.com",
            "1234567890",
            true
        );
    }

    @Test
    @DisplayName("Should retrieve all consumers")
    void testGetAllConsumers() {
        Consumer consumer2 = new Consumer();
        consumer2.setId(2L);
        consumer2.setName("Test User 2");
        consumer2.setEmail("test2@example.com");
        consumer2.setUsername("testuser2");
        consumer2.setRole(Role.ADMIN);
        consumer2.setActive(true);

        when(consumerRepository.findAll()).thenReturn(Arrays.asList(testConsumer, consumer2));

        var result = consumerService.getAllConsumers();

        assertEquals(2, result.size());
        verify(consumerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find consumer by username")
    void testFindByUsername() {
        when(consumerRepository.existsByUsername("testuser")).thenReturn(true);
        when(consumerRepository.findByUsername("testuser")).thenReturn(testConsumer);

        ConsumerDto result = consumerService.findByUsername("testuser");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw exception when consumer not found by username")
    void testFindByUsernameNotFound() {
        when(consumerRepository.existsByUsername("notfound")).thenReturn(false);

        assertThrows(EntityNotFundException.class, () -> consumerService.findByUsername("notfound"));
    }

    @Test
    @DisplayName("Should find consumer by email")
    void testFindByEmail() {
        when(consumerRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(testConsumer);

        ConsumerDto result = consumerService.findByEmail("test@example.com");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw exception when consumer not found by email")
    void testFindByEmailNotFound() {
        when(consumerRepository.existsByEmail("notfound@example.com")).thenReturn(false);

        assertThrows(EntityNotFundException.class, () -> consumerService.findByEmail("notfound@example.com"));
    }

    @Test
    @DisplayName("Should create a new user")
    void testCreateUser() {
        UserUpsertRequest request = new UserUpsertRequest(
            "New User",
            "newuser",
            "newuser@example.com",
            "1234567890",
            "password123",
            Role.USER
        );

        when(consumerRepository.existsByUsername("newuser")).thenReturn(false);
        when(consumerRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(consumerRepository.save(any())).thenReturn(testConsumer);

        Response response = consumerService.createUser(request);

        assertTrue(response.getSuccess());
        assertEquals(201, response.getStatus());
        verify(consumerRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testCreateUserWithExistingUsername() {
        UserUpsertRequest request = new UserUpsertRequest(
            "New User",
            "testuser",
            "newuser@example.com",
            "1234567890",
            "password123",
            Role.USER
        );

        when(consumerRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> consumerService.createUser(request));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testCreateUserWithExistingEmail() {
        UserUpsertRequest request = new UserUpsertRequest(
            "New User",
            "newuser",
            "test@example.com",
            "1234567890",
            "password123",
            Role.USER
        );

        when(consumerRepository.existsByUsername("newuser")).thenReturn(false);
        when(consumerRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> consumerService.createUser(request));
    }

    @Test
    @DisplayName("Should update a user")
    void testUpdateUser() {
        UserUpsertRequest request = new UserUpsertRequest(
            "Updated User",
            "updateduser",
            "updated@example.com",
            "9876543210",
            "newpassword123",
            Role.ADMIN
        );

        when(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer));
        when(consumerRepository.existsByUsernameAndIdNot("updateduser", 1L)).thenReturn(false);
        when(consumerRepository.existsByEmailAndIdNot("updated@example.com", 1L)).thenReturn(false);
        when(passwordEncoder.encode("newpassword123")).thenReturn("hashedNewPassword");
        when(consumerRepository.save(any())).thenReturn(testConsumer);

        Response response = consumerService.updateUser(1L, request);

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        verify(consumerRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should update user status to inactive")
    void testUpdateUserStatus() {
        UserStatusRequest request = new UserStatusRequest(false);

        when(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer));
        when(tokenRepository.findAllValidIsFalseOrRevokedIsFalseByConsumerId(1L)).thenReturn(List.of());

        Response response = consumerService.updateUserStatus(1L, request, "admin@example.com");

        assertTrue(response.getSuccess());
        assertEquals(200, response.getStatus());
        assertFalse(testConsumer.getActive());
        verify(consumerRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should prevent user from deactivating their own account")
    void testUpdateOwnUserStatusToInactive() {
        UserStatusRequest request = new UserStatusRequest(false);

        when(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer));

        assertThrows(IllegalArgumentException.class,
            () -> consumerService.updateUserStatus(1L, request, "test@example.com"));
    }

    @Test
    @DisplayName("Should get users with search filter")
    void testGetUsers() {
        Consumer consumer2 = new Consumer();
        consumer2.setId(2L);
        consumer2.setName("Admin User");
        consumer2.setEmail("admin@example.com");
        consumer2.setUsername("adminuser");
        consumer2.setRole(Role.ADMIN);
        consumer2.setActive(true);

        when(consumerRepository.findAll()).thenReturn(Arrays.asList(testConsumer, consumer2));

        var result = consumerService.getUsers("test");

        assertEquals(1, result.size());
    }
}

