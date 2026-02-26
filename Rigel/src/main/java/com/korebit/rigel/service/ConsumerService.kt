package com.korebit.rigel.service

import com.korebit.rigel.dto.ConsumerDto
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

    fun saveConsumer(consumerDto: ConsumerDto): TokenResponse {

        if (consumerRepository.existsByUsername(consumerDto.username)) {
            throw IllegalArgumentException("Username already exists")
        }

        val consumer = Consumer(
            name = consumerDto.name,
            role = consumerDto.role,
            username = consumerDto.username,
            password = passwordEncoder.encode(consumerDto.password),
            email = consumerDto.email,
            phoneNumber = consumerDto.phoneNumber
        )

        consumerRepository.save(consumer)
        val jwtToken = jwtService.generateRefreshToken(consumer)
        val refreshToken = jwtService.generateRefreshToken(consumer)
        saveConsumerToken.saveConsumerToken(consumer, jwtToken)

        return TokenResponse(jwtToken, refreshToken)
    }
}