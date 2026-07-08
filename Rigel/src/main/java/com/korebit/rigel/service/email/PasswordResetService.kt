package com.korebit.rigel.service.email

import com.korebit.rigel.enums.TokenType
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.repository.TokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasswordResetService(
    private val consumerRepository: ConsumerRepository,
    private val tokenRepository: TokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService,
    private val invitationService: UserInvitationService,

    @param:Value("\${app.mail.reset-token-expiration-hours:1}")
    private val resetTokenExpirationHours: Long,
) {

    /**
     * Initiates the password reset flow.
     *
     * Always responds the same way regardless of whether the email exists,
     * to avoid leaking which accounts are registered.
     */
    @Transactional
    fun requestPasswordReset(email: String) {
        val consumer = consumerRepository.findByEmail(email) ?: return

        // An inactive account has never been activated — it cannot reset its password
        if (!consumer.active) return

        // Revoke any pending reset tokens before generating a new one
        tokenRepository
            .findAllByConsumerAndTokenTypeAndRevokedFalse(consumer, TokenType.PASSWORD_RESET)
            .forEach { invitationService.consumeToken(it) }

        val rawToken = invitationService.generateAndSaveToken(
            consumer,
            TokenType.PASSWORD_RESET,
            resetTokenExpirationHours,
        )
        emailService.sendPasswordResetEmail(consumer.email, consumer.name, rawToken)
    }

    /**
     * Validates the reset token and updates the password.
     *
     * @param rawToken    UUID token from the reset link
     * @param newPassword New plain-text password (will be encoded)
     */
    @Transactional
    fun resetPassword(rawToken: String, newPassword: String) {
        val token = invitationService.resolveToken(rawToken, TokenType.PASSWORD_RESET)

        val consumer = token.consumer
            ?: throw IllegalStateException("El token no tiene un usuario asociado")

        consumer.password = passwordEncoder.encode(newPassword)
        consumerRepository.save(consumer)

        invitationService.consumeToken(token)
    }
}