package com.korebit.rigel.service.email

import com.korebit.rigel.enums.Role
import com.korebit.rigel.enums.TokenType
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.model.beans.Token
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.repository.TokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class UserInvitationService(
    private val consumerRepository: ConsumerRepository,
    private val tokenRepository: TokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,

    @param:Value("\${app.mail.token-expiration-hours}")
    private val activationTokenExpirationHours: Long,
) {

    /**
     * Creates an inactive user and sends an activation email.
     * Only the admin/root should call this endpoint.
     *
     * @param name          Display name
     * @param username      Unique username
     * @param email         Email — also used to activate the account
     * @param role          Role assigned by the admin (defaults to USER)
     * @param phoneNumber   Optional phone number
     */
    @Transactional
    fun inviteUser(
        name: String,
        username: String,
        email: String,
        role: Role = Role.USER,
        phoneNumber: String = "",
    ): Consumer {
        if (consumerRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("El correo '$email' ya está registrado")
        }

        // Consumer starts inactive and without a password —
        // they set their own password when they activate the account.
        val consumer = Consumer(
            name = name,
            username = username,
            email = email,
            password = null,
            role = role,
            active = false,
            phoneNumber = phoneNumber,
        )
        consumerRepository.save(consumer)

        // Revoke any previous activation tokens for this user (shouldn't happen, but defensive)
        revokeExistingTokens(consumer, TokenType.EMAIL_VERIFICATION)

        val rawToken = generateAndSaveToken(consumer, TokenType.EMAIL_VERIFICATION, activationTokenExpirationHours)
        emailService.sendActivationEmail(email, name, rawToken)

        return consumer
    }

    /**
     * Validates an activation token, sets the user's password, and activates the account.
     *
     * @param rawToken    UUID token from the activation link
     * @param newPassword Plain-text password chosen by the user (will be encoded)
     */
    @Transactional
    fun activateAccount(rawToken: String, newPassword: String) {
        val token = resolveToken(rawToken, TokenType.EMAIL_VERIFICATION)

        val consumer = token.consumer
            ?: throw IllegalStateException("El token no tiene un usuario asociado")

        consumer.password = passwordEncoder.encode(newPassword)
        consumer.active = true
        consumerRepository.save(consumer)

        consumeToken(token)
    }

    internal fun generateAndSaveToken(
        consumer: Consumer,
        type: TokenType,
        expirationHours: Long,
    ): String {
        val rawToken = UUID.randomUUID().toString()
        val token = Token(
            token = rawToken,
            tokenType = type,
            revoked = false,
            expired = false,
            expiresAt = LocalDateTime.now().plusHours(expirationHours),
            consumer = consumer,
        )
        tokenRepository.save(token)
        return rawToken
    }

    internal fun resolveToken(rawToken: String, expectedType: TokenType): Token {
        val token = tokenRepository.findByToken(rawToken)
            ?: throw IllegalArgumentException("Token inválido")

        if (token.tokenType != expectedType) throw IllegalArgumentException("Token inválido")
        if (token.revoked || token.expired)  throw IllegalArgumentException("El enlace ya fue utilizado o ha expirado")
        if (token.expiresAt?.isBefore(LocalDateTime.now()) == true) {
            consumeToken(token)
            throw IllegalArgumentException("El enlace ha expirado. Solicita uno nuevo")
        }

        return token
    }

    internal fun consumeToken(token: Token) {
        token.revoked = true
        token.expired = true
        tokenRepository.save(token)
    }

    private fun revokeExistingTokens(consumer: Consumer, type: TokenType) {
        tokenRepository
            .findAllByConsumerAndTokenTypeAndRevokedFalse(consumer, type)
            .forEach { consumeToken(it) }
    }
}