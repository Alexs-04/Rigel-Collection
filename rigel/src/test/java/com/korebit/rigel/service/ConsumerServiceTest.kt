package com.korebit.rigel.service

import com.korebit.rigel.any
import com.korebit.rigel.dto.request.UserStatusRequest
import com.korebit.rigel.dto.request.UserUpsertRequest
import com.korebit.rigel.enums.Role
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.AdditionalAnswers.returnsFirstArg
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@DisplayName("ConsumerService Tests")
class ConsumerServiceTest {

    @Mock
    private lateinit var consumerRepository: ConsumerRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var sessionTokenService: SessionTokenService

    @InjectMocks
    private lateinit var consumerService: ConsumerService

    private lateinit var testConsumer: Consumer

    @BeforeEach
    fun setUp() {
        testConsumer = Consumer(
            id = 1L,
            name = "Test User",
            email = "test@example.com",
            username = "testuser",
            password = "hashedPassword",
            role = Role.USER,
            active = true,
            phoneNumber = "1234567890",
        )
    }

    private fun request(name: String, username: String, password: String?, email: String, role: Role) =
        UserUpsertRequest(name, username, password, email, "9876543210", role)

    @Test
    @DisplayName("Should find consumer by username without exposing the password")
    fun testFindByUsername() {
        whenever(consumerRepository.findByUsername("testuser")).thenReturn(testConsumer)

        val result = consumerService.findByUsername("testuser")

        assertEquals("test@example.com", result.email)
        assertEquals(Role.USER, result.role)
    }

    @Test
    @DisplayName("Should throw when consumer not found by username")
    fun testFindByUsernameNotFound() {
        whenever(consumerRepository.findByUsername("notfound")).thenReturn(null)

        assertFailsWith<EntityNotFundException> { consumerService.findByUsername("notfound") }
    }

    @Test
    @DisplayName("Should find consumer by email")
    fun testFindByEmail() {
        whenever(consumerRepository.findByEmail("test@example.com")).thenReturn(testConsumer)

        assertEquals("testuser", consumerService.findByEmail("test@example.com").username)
    }

    @Test
    @DisplayName("Should throw when consumer not found by email")
    fun testFindByEmailNotFound() {
        whenever(consumerRepository.findByEmail("notfound@example.com")).thenReturn(null)

        assertFailsWith<EntityNotFundException> { consumerService.findByEmail("notfound@example.com") }
    }

    @Test
    @DisplayName("Should create a user with an encoded password")
    fun testCreateUser() {
        val req = request("New User", "newuser", "password123", "newuser@example.com", Role.ADMIN)

        whenever(consumerRepository.existsByUsername("newuser")).thenReturn(false)
        whenever(consumerRepository.existsByEmail("newuser@example.com")).thenReturn(false)
        whenever(passwordEncoder.encode("password123")).thenReturn("hashed")
        whenever(consumerRepository.save(any<Consumer>())).then { invocation ->
            invocation.getArgument<Consumer>(0).apply { id = 10L }
        }

        val result = consumerService.createUser(req)

        assertEquals(10L, result.id)
        assertEquals("newuser", result.username)
        assertEquals(Role.ADMIN, result.role)
        assertTrue(result.active)
        verify(passwordEncoder).encode("password123")
    }

    @Test
    @DisplayName("Should require a password when creating a user")
    fun testCreateUserWithoutPassword() {
        val req = request("New User", "newuser", "  ", "newuser@example.com", Role.USER)

        assertFailsWith<IllegalArgumentException> { consumerService.createUser(req) }
        verify(consumerRepository, never()).save(any<Consumer>())
    }

    @Test
    @DisplayName("Should throw when username already exists")
    fun testCreateUserWithExistingUsername() {
        val req = request("New User", "testuser", "password123", "newuser@example.com", Role.USER)
        whenever(consumerRepository.existsByUsername("testuser")).thenReturn(true)

        assertFailsWith<IllegalArgumentException> { consumerService.createUser(req) }
        verify(consumerRepository, never()).save(any<Consumer>())
    }

    @Test
    @DisplayName("Should throw when email already exists")
    fun testCreateUserWithExistingEmail() {
        val req = request("New User", "newuser", "password123", "test@example.com", Role.USER)
        whenever(consumerRepository.existsByUsername("newuser")).thenReturn(false)
        whenever(consumerRepository.existsByEmail("test@example.com")).thenReturn(true)

        assertFailsWith<IllegalArgumentException> { consumerService.createUser(req) }
    }

    @Test
    @DisplayName("Changing role or password must revoke the user's sessions")
    fun testUpdateUserRevokesSessions() {
        val req = request("Updated", "updateduser", "newpassword123", "updated@example.com", Role.ADMIN)

        whenever(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer))
        whenever(consumerRepository.existsByUsernameAndIdNot("updateduser", 1L)).thenReturn(false)
        whenever(consumerRepository.existsByEmailAndIdNot("updated@example.com", 1L)).thenReturn(false)
        whenever(passwordEncoder.encode("newpassword123")).thenReturn("hashedNewPassword")
        whenever(consumerRepository.save(any<Consumer>())).then(returnsFirstArg<Consumer>())

        val result = consumerService.updateUser(1L, req)

        assertEquals(Role.ADMIN, result.role)
        assertEquals("hashedNewPassword", testConsumer.password)
        verify(sessionTokenService).revokeAllSessions(testConsumer)
    }

    @Test
    @DisplayName("Updating only profile data keeps sessions open")
    fun testUpdateUserProfileOnlyKeepsSessions() {
        val req = request("Renamed", "testuser", null, "test@example.com", Role.USER)

        whenever(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer))
        whenever(consumerRepository.existsByUsernameAndIdNot("testuser", 1L)).thenReturn(false)
        whenever(consumerRepository.existsByEmailAndIdNot("test@example.com", 1L)).thenReturn(false)
        whenever(consumerRepository.save(any<Consumer>())).then(returnsFirstArg<Consumer>())

        assertEquals("Renamed", consumerService.updateUser(1L, req).name)
        verifyNoInteractions(sessionTokenService, passwordEncoder)
    }

    @Test
    @DisplayName("Deactivating a user revokes their sessions")
    fun testUpdateUserStatus() {
        whenever(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer))
        whenever(consumerRepository.save(any<Consumer>())).then(returnsFirstArg<Consumer>())

        val result = consumerService.updateUserStatus(1L, UserStatusRequest(false), "admin@example.com")

        assertFalse(result.active)
        verify(sessionTokenService).revokeAllSessions(testConsumer)
    }

    @Test
    @DisplayName("Should prevent a user from deactivating their own account")
    fun testUpdateOwnUserStatusToInactive() {
        whenever(consumerRepository.findById(1L)).thenReturn(Optional.of(testConsumer))

        assertFailsWith<IllegalArgumentException> {
            consumerService.updateUserStatus(1L, UserStatusRequest(false), "TEST@example.com")
        }
        verify(consumerRepository, never()).save(any<Consumer>())
    }

    @Test
    @DisplayName("Should filter users by search text")
    fun testGetUsers() {
        val admin = Consumer(
            id = 2L,
            name = "Admin User",
            email = "admin@example.com",
            username = "adminuser",
            role = Role.ADMIN,
        )

        whenever(consumerRepository.findAll()).thenReturn(listOf(testConsumer, admin))

        assertEquals(1, consumerService.getUsers("test").size)
        assertEquals(2, consumerService.getUsers(null).size)
    }
}
