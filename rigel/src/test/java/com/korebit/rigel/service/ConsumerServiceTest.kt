package com.korebit.rigel.service;

import com.korebit.rigel.dto.UserAdminDto;
import com.korebit.rigel.dto.request.UserStatusRequest;
import com.korebit.rigel.dto.request.UserUpsertRequest;
import com.korebit.rigel.enums.Role;
import com.korebit.rigel.exception.EntityNotFundException;
import com.korebit.rigel.model.beans.Consumer;
import com.korebit.rigel.repository.ConsumerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConsumerService Tests")
class ConsumerServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SessionTokenService sessionTokenService;

    @InjectMocks
    private ConsumerService consumerService;

    private Consumer testConsumer;

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
    }

    private static UserUpsertRequest request(String name, String username, String password,
                                             String email, Role role) {
        return new UserUpsertRequest(name, username, password, email, "9876543210", role);
    }

    @Test
    @DisplayName("Should find consumer by username without exposing the password")
    void testFindByUsername() {
        when(consumerRepository.findByUsername("testuser")).thenReturn(testConsumer);

        UserAdminDto result = consumerService.findByUsername("testuser");

        assertEquals("test@example.com", result.getEmail());
        assertEquals(Role.USER, result.getRole());
    }

    @Test
    @DisplayName("Should throw when consumer not found by username")
    void testFindByUsernameNotFound() {
        when(consumerRepository.findByUsername("notfound")).thenReturn(null);

        assertThrows(EntityNotFundException.class, () -> consumerService.findByUsername("notfound"));
    }

    @Test
    @DisplayName("Should find consumer by email")
    void testFindByEmail() {
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(testConsumer);

        assertEquals("testuser", consumerService.findByEmail("test@example.com").getUsername());
    }

    @Test
    @DisplayName("Should throw when consumer not found by email")
    void testFindByEmailNotFound() {
        when(consumerRepository.findByEmail("notfound@example.com")).thenReturn(null);

        assertThrows(EntityNotFundException.class, () -> consumerService.findByEmail("notfound@example.com"));
    }

    @Test
    @DisplayName("Should create a user with an encoded password")
    void testCreateUser() {
        UserUpsertRequest req = request("New User", "newuser", "password123", "newuser@example.com", Role.ADMIN);

        when(consumerRepository.existsByUsername("newuser")).thenReturn(false);
        when(consumerRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(consumerRepository.save(any())).then(invocation -> {
            Consumer saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        UserAdminDto result = consumerService.createUser(req);

        assertEquals(10L, result.getId());
        assertEquals("newuser", result.getUsername());
        assertEquals(Role.ADMIN, result.getRole());
        assertTrue(result.getActive());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("Should require a password when creating a user")
    void testCreateUserWithoutPassword() {
        UserUpsertRequest req = request("New User", "newuser", "  ", "newuser@example.com", Role.USER);

        assertThrows(IllegalArgumentException.class, () -> consumerService.createUser(req));
        verify(consumerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when username already exists")
    void testCreateUserWithExistingUsername() {
        UserUpsertRequest req = request("New User", "testuser", "password123", "newuser@example.com", Role.USER);
        when(consumerRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> consumerService.createUser(req));
        verify(consumerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw when email already exists")
    void testCreateUserWithExistingEmail() {
        UserUpsertRequest req = request("New User", "newuser", "password123", "test@example.com", Role.USER);
        when(consumerRepository.existsByUsername("newuser")).thenReturn(false);
        when(consumerRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> consumerService.createUser(req));
    }

    @Test
    @DisplayName("Changing role or password must revoke the user's sessions")
    void testUpdateUserRevokesSessions() {
        UserUpsertRequest req = request("Updated", "updateduser", "newpassword123", "updated@example.com", Role.ADMIN);

        when(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer));
        when(consumerRepository.existsByUsernameAndIdNot("updateduser", 1L)).thenReturn(false);
        when(consumerRepository.existsByEmailAndIdNot("updated@example.com", 1L)).thenReturn(false);
        when(passwordEncoder.encode("newpassword123")).thenReturn("hashedNewPassword");
        when(consumerRepository.save(any())).then(returnsFirstArg());

        UserAdminDto result = consumerService.updateUser(1L, req);

        assertEquals(Role.ADMIN, result.getRole());
        assertEquals("hashedNewPassword", testConsumer.getPassword());
        verify(sessionTokenService).revokeAllSessions(testConsumer);
    }

    @Test
    @DisplayName("Updating only profile data keeps sessions open")
    void testUpdateUserProfileOnlyKeepsSessions() {
        UserUpsertRequest req = request("Renamed", "testuser", null, "test@example.com", Role.USER);

        when(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer));
        when(consumerRepository.existsByUsernameAndIdNot("testuser", 1L)).thenReturn(false);
        when(consumerRepository.existsByEmailAndIdNot("test@example.com", 1L)).thenReturn(false);
        when(consumerRepository.save(any())).then(returnsFirstArg());

        assertEquals("Renamed", consumerService.updateUser(1L, req).getName());
        verifyNoInteractions(sessionTokenService, passwordEncoder);
    }

    @Test
    @DisplayName("Deactivating a user revokes their sessions")
    void testUpdateUserStatus() {
        when(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer));
        when(consumerRepository.save(any())).then(returnsFirstArg());

        UserAdminDto result = consumerService.updateUserStatus(1L, new UserStatusRequest(false), "admin@example.com");

        assertFalse(result.getActive());
        verify(sessionTokenService).revokeAllSessions(testConsumer);
    }

    @Test
    @DisplayName("Should prevent a user from deactivating their own account")
    void testUpdateOwnUserStatusToInactive() {
        when(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer));

        assertThrows(IllegalArgumentException.class,
                () -> consumerService.updateUserStatus(1L, new UserStatusRequest(false), "TEST@example.com"));
        verify(consumerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should filter users by search text")
    void testGetUsers() {
        Consumer admin = new Consumer();
        admin.setId(2L);
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setUsername("adminuser");
        admin.setRole(Role.ADMIN);

        when(consumerRepository.findAll()).thenReturn(Arrays.asList(testConsumer, admin));

        assertEquals(1, consumerService.getUsers("test").size());
        assertEquals(2, consumerService.getUsers(null).size());
    }
}
