package com.korebit.rigel.service

import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.model.beans.Token
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.repository.TokenRepository
import com.korebit.rigel.dto.request.LoginRequest
import com.korebit.rigel.dto.response.TokenResponse
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.service.jwt.JwtService
import com.korebit.rigel.util.SaveConsumerToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val consumerRepository: ConsumerRepository,
    private val tokenRepository: TokenRepository,
    private val saveConsumerToken: SaveConsumerToken
) {

    fun login(request: LoginRequest): TokenResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email ?: "",
                request.password ?: ""
            )
        )

        val consumer = consumerRepository.findByEmail(request.email ?: "")
            ?: throw EntityNotFundException("No consumer found")
        val jwtToken = jwtService.generateToken(consumer)
        val refreshToken = jwtService.generateRefreshToken(consumer)

        revokeAllUserTokens(consumer)
        saveConsumerToken.saveConsumerToken(consumer, jwtToken)
        return TokenResponse(jwtToken, refreshToken)
    }

    private fun revokeAllUserTokens(consumer: Consumer) {
        val validConsumerTokens: MutableList<Token> = tokenRepository
            .findAllValidIsFalseOrRevokedIsFalseByConsumerId(
                consumer.id
                    ?: throw IllegalArgumentException("consumerId is null")
            )

        if (!validConsumerTokens.isEmpty()) {
            for (token in validConsumerTokens) {
                token.revoked = true
                token.expired = true
            }
            tokenRepository.saveAll(validConsumerTokens)
        }
    }

    private fun refreshToken (authHeader: String) {
        if (!authHeader.startsWith("Bearer ")) {
            throw IllegalArgumentException("Invalid Bearer Token")
        }
    }
}