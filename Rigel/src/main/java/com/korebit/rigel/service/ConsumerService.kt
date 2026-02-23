package com.korebit.rigel.service

import com.korebit.rigel.dto.ConsumerRequest
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.dto.response.TokenResponse
import com.korebit.rigel.service.jwt.JwtService
import com.korebit.rigel.util.SaveConsumerToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class ConsumerService(
    private val consumerRepository: ConsumerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val saveConsumerToken: SaveConsumerToken
) {

    fun saveConsumer(consumerRequest: ConsumerRequest): TokenResponse {

        if (consumerRepository.existsByUsername(consumerRequest.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        val consumer = Consumer(
            name = consumerRequest.name,
            role = consumerRequest.role,
            username = consumerRequest.username,
            password = passwordEncoder.encode(consumerRequest.password),
            email = consumerRequest.email,
            phoneNumber = consumerRequest.phoneNumber
        )

        consumerRepository.save(consumer)
        val jwtToken = jwtService.generateRefreshToken(consumer)
        val refreshToken = jwtService.generateRefreshToken(consumer)
        saveConsumerToken.saveConsumerToken(consumer, jwtToken)

        return TokenResponse(jwtToken, refreshToken)
    }
}