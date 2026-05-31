package com.korebit.rigel.service

import com.korebit.rigel.dto.ConsumerDto
import com.korebit.rigel.dto.UserAdminDto
import com.korebit.rigel.dto.request.UserStatusRequest
import com.korebit.rigel.dto.request.UserUpsertRequest
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.model.beans.Token
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.dto.response.TokenResponse
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.enums.Role
import com.korebit.rigel.repository.TokenRepository
import com.korebit.rigel.service.jwt.JwtService
import com.korebit.rigel.util.SaveConsumerToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
/**
 * Manages consumer accounts, administrative user operations, and token revocation.
 */
class ConsumerService(
    private val consumerRepository: ConsumerRepository,
    private val tokenRepository: TokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val saveConsumerToken: SaveConsumerToken
) {

    @Transactional
    /**
     * Registers a new consumer and returns the issued authentication tokens.
     *
     * @param consumerDto consumer payload.
     * @return token response for the newly created account.
     */
    fun saveConsumer(consumerDto: ConsumerDto): TokenResponse {

        if (consumerRepository.existsByUsername(consumerDto.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        val consumer = Consumer(
            name = consumerDto.name,
            role = consumerDto.role ?: Role.USER,
            username = consumerDto.username,
            password = passwordEncoder.encode(consumerDto.password),
            email = consumerDto.email,
            phoneNumber = consumerDto.phoneNumber,
            active = consumerDto.active ?: true
        )

        consumerRepository.save(consumer)
        val jwtToken = jwtService.generateToken(consumer)
        val refreshToken = jwtService.generateRefreshToken(consumer)
        saveConsumerToken.saveConsumerToken(consumer, jwtToken)

        return TokenResponse(jwtToken, refreshToken)
    }

    @Transactional(readOnly = true)
    /**
     * Lists all consumers.
     *
     * @return list of consumers mapped to DTOs.
     */
    fun getAllConsumers(): List<ConsumerDto> {
        return consumerRepository.findAll().map { consumer ->
            ConsumerDto(
                consumer.name,
                consumer.role,
                consumer.username,
                "",
                consumer.email,
                consumer.phoneNumber,
                consumer.active,
            )
        }
    }

    @Transactional(readOnly = true)
    /**
     * Finds a consumer by username.
     *
     * @param username unique username.
     * @return consumer DTO when found.
     */
    fun findByUsername(username: String): ConsumerDto? {
        if (!consumerRepository.existsByUsername(username)) {
            throw EntityNotFundException("Consumer with username $username not found")
        }

        return consumerRepository.findByUsername(username)?.let { consumer ->
            ConsumerDto(
                consumer.name,
                consumer.role,
                consumer.username,
                "",
                consumer.email,
                consumer.phoneNumber,
                consumer.active,
            )
        }
    }

    @Transactional(readOnly = true)
    /**
     * Finds a consumer by email.
     *
     * @param email unique email.
     * @return consumer DTO when found.
     */
    fun findByEmail(email: String): ConsumerDto? {
        if (!consumerRepository.existsByEmail(email)) {
            throw EntityNotFundException("Consumer with email $email not found")
        }

        return consumerRepository.findByEmail(email)?.let { consumer ->
            ConsumerDto(
                consumer.name,
                consumer.role,
                consumer.username,
                "",
                consumer.email,
                consumer.phoneNumber,
                consumer.active,
            )
        }
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves users for the admin view with optional text filtering.
     *
     * @param search optional search text.
     * @return sorted user list.
     */
    fun getUsers(search: String?): List<UserAdminDto> {
        val normalizedSearch = search?.trim()?.lowercase().orEmpty()
        return consumerRepository.findAll()
            .asSequence()
            .filter { consumer ->
                normalizedSearch.isBlank() ||
                        consumer.name.lowercase().contains(normalizedSearch) ||
                        consumer.username.lowercase().contains(normalizedSearch) ||
                        consumer.email.lowercase().contains(normalizedSearch)
            }
            .map(UserAdminDto::fromConsumer)
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    @Transactional
    /**
     * Creates a new user from an administrative request.
     *
     * @param request user payload.
     * @return operation response.
     */
    fun createUser(request: UserUpsertRequest): Response {
        validateRequiredFields(request.name, request.username, request.email)
        val password = request.password?.trim().orEmpty()
        if (password.isBlank()) {
            throw IllegalArgumentException("Password is required")
        }

        if (consumerRepository.existsByUsername(request.username.trim())) {
            throw IllegalArgumentException("Username already exists")
        }
        if (consumerRepository.existsByEmail(request.email.trim())) {
            throw IllegalArgumentException("Email already exists")
        }

        val consumer = Consumer(
            name = request.name.trim(),
            username = request.username.trim(),
            password = passwordEncoder.encode(password),
            email = request.email.trim(),
            phoneNumber = request.phoneNumber.trim(),
            role = request.role,
            active = true
        )
        consumerRepository.save(consumer)

        return Response(
            success = true,
            status = 201,
            message = "Usuario ${consumer.username} creado correctamente"
        )
    }

    @Transactional
    /**
     * Updates an existing user account by id.
     *
     * @param id user id.
     * @param request update payload.
     * @return operation response.
     */
    fun updateUser(id: Long, request: UserUpsertRequest): Response {
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

        consumer.name = request.name.trim()
        consumer.username = normalizedUsername
        consumer.email = normalizedEmail
        consumer.phoneNumber = request.phoneNumber.trim()
        consumer.role = request.role
        if (!request.password.isNullOrBlank()) {
            consumer.password = passwordEncoder.encode(request.password.trim())
        }
        consumerRepository.save(consumer)

        return Response(
            success = true,
            status = 200,
            message = "Usuario ${consumer.username} actualizado correctamente"
        )
    }

    @Transactional
    /**
     * Activates or deactivates a user account.
     *
     * @param id user id.
     * @param request desired status payload.
     * @param actorEmail email of the user performing the action.
     * @return operation response.
     */
    fun updateUserStatus(id: Long, request: UserStatusRequest, actorEmail: String): Response {
        val consumer = findConsumerById(id)
        if (!request.active && consumer.email.equals(actorEmail, ignoreCase = true)) {
            throw IllegalArgumentException("No puedes desactivar tu propia cuenta")
        }

        consumer.active = request.active
        consumerRepository.save(consumer)

        if (!consumer.active) {
            revokeAllUserTokens(consumer)
        }

        val statusLabel = if (consumer.active) "activado" else "desactivado"
        return Response(
            success = true,
            status = 200,
            message = "Usuario ${consumer.username} $statusLabel correctamente"
        )
    }

    private fun findConsumerById(id: Long): Consumer {
        return consumerRepository.findById(id).orElseThrow {
            EntityNotFundException("Consumer with id $id not found")
        }
    }

    private fun validateRequiredFields(name: String, username: String, email: String) {
        if (name.trim().isEmpty()) throw IllegalArgumentException("Name is required")
        if (username.trim().isEmpty()) throw IllegalArgumentException("Username is required")
        if (email.trim().isEmpty()) throw IllegalArgumentException("Email is required")
    }

    private fun revokeAllUserTokens(consumer: Consumer) {
        val consumerId = consumer.id ?: throw IllegalArgumentException("consumerId is null")
        val validConsumerTokens: MutableList<Token> = tokenRepository
            .findAllValidIsFalseOrRevokedIsFalseByConsumerId(consumerId)

        if (validConsumerTokens.isNotEmpty()) {
            validConsumerTokens.forEach { token ->
                token.revoked = true
                token.expired = true
            }
            tokenRepository.saveAll(validConsumerTokens)
        }
    }
}