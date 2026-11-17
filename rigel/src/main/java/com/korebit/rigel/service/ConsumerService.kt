package com.korebit.rigel.service

import com.korebit.rigel.dto.UserAdminDto
import com.korebit.rigel.dto.request.UserStatusRequest
import com.korebit.rigel.dto.request.UserUpsertRequest
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.repository.ConsumerRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Administrative management of user accounts.
 *
 * Account creation is only available to administrators (see `SecurityConfig`); there is no
 * public self-registration. Any change that alters what a token asserts (role, email,
 * password, active flag) revokes the user's open sessions.
 */
@Service
class ConsumerService(
    private val consumerRepository: ConsumerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val sessionTokenService: SessionTokenService,
) {

    @Transactional(readOnly = true)
    fun findByUsername(username: String): UserAdminDto {
        val consumer = consumerRepository.findByUsername(username.trim())
            ?: throw EntityNotFundException("Consumer with username $username not found")
        return UserAdminDto.fromEntity(consumer)
    }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): UserAdminDto {
        val consumer = consumerRepository.findByEmail(email.trim())
            ?: throw EntityNotFundException("Consumer with email $email not found")
        return UserAdminDto.fromEntity(consumer)
    }

    /**
     * Lists users sorted by name, optionally filtered by name, username or email.
     */
    @Transactional(readOnly = true)
    fun getUsers(search: String? = null): List<UserAdminDto> {
        val normalizedSearch = search?.trim()?.lowercase().orEmpty()
        return consumerRepository.findAll()
            .asSequence()
            .filter { consumer ->
                normalizedSearch.isBlank() ||
                    consumer.name.lowercase().contains(normalizedSearch) ||
                    consumer.username.lowercase().contains(normalizedSearch) ||
                    consumer.email.lowercase().contains(normalizedSearch)
            }
            .map(UserAdminDto::fromEntity)
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    @Transactional
    fun createUser(request: UserUpsertRequest): UserAdminDto {
        validateRequiredFields(request.name, request.username, request.email)
        val password = request.password?.trim().orEmpty()
        if (password.isBlank()) {
            throw IllegalArgumentException("Password is required")
        }

        val username = request.username.trim()
        val email = request.email.trim()
        if (consumerRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username already exists")
        }
        if (consumerRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email already exists")
        }

        val consumer = Consumer(
            name = request.name.trim(),
            username = username,
            password = passwordEncoder.encode(password),
            email = email,
            phoneNumber = request.phoneNumber.trim(),
            role = request.role,
            active = true
        )
        return UserAdminDto.fromEntity(consumerRepository.save(consumer))
    }

    @Transactional
    fun updateUser(id: Long, request: UserUpsertRequest): UserAdminDto {
        val consumer = findConsumerById(id)
        validateRequiredFields(request.name, request.username, request.email)

        val normalizedUsername = request.username.trim()
        val normalizedEmail = request.email.trim()
        if (consumerRepository.existsByUsernameAndIdNot(normalizedUsername, id)) {
            throw IllegalArgumentException("Username already exists")
        }
        if (consumerRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw IllegalArgumentException("Email already exists")
        }

        val newPassword = request.password?.trim()?.takeIf { it.isNotEmpty() }
        val invalidatesSessions = newPassword != null ||
            consumer.role != request.role ||
            !consumer.email.equals(normalizedEmail, ignoreCase = true)

        consumer.name = request.name.trim()
        consumer.username = normalizedUsername
        consumer.email = normalizedEmail
        consumer.phoneNumber = request.phoneNumber.trim()
        consumer.role = request.role
        if (newPassword != null) {
            consumer.password = passwordEncoder.encode(newPassword)
        }
        val saved = consumerRepository.save(consumer)

        if (invalidatesSessions) {
            sessionTokenService.revokeAllSessions(saved)
        }
        return UserAdminDto.fromEntity(saved)
    }

    /**
     * Activates or deactivates an account. Deactivation revokes the user's sessions immediately.
     *
     * @param actorEmail email of the administrator performing the action (cannot deactivate themselves).
     */
    @Transactional
    fun updateUserStatus(id: Long, request: UserStatusRequest, actorEmail: String): UserAdminDto {
        val consumer = findConsumerById(id)
        if (!request.active && consumer.email.equals(actorEmail, ignoreCase = true)) {
            throw IllegalArgumentException("No puedes desactivar tu propia cuenta")
        }

        consumer.active = request.active
        val saved = consumerRepository.save(consumer)

        if (!saved.active) {
            sessionTokenService.revokeAllSessions(saved)
        }
        return UserAdminDto.fromEntity(saved)
    }

    private fun findConsumerById(id: Long): Consumer {
        return consumerRepository.findById(id).orElseThrow {
            EntityNotFundException("Consumer with id $id not found")
        }
    }

    private fun validateRequiredFields(name: String, username: String, email: String) {
        if (name.isBlank()) throw IllegalArgumentException("Name is required")
        if (username.isBlank()) throw IllegalArgumentException("Username is required")
        if (email.isBlank()) throw IllegalArgumentException("Email is required")
    }
}
