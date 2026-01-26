package com.korebit.rigel.util

import com.korebit.rigel.model.Consumer
import com.korebit.rigel.model.Token
import com.korebit.rigel.repository.TokenRepository
import org.springframework.stereotype.Component

@Component
class SaveConsumerToken(
    private val tokenRepository: TokenRepository
) {
    fun saveConsumerToken(consumer: Consumer, token: String) {
        val token = Token(token = token, consumer = consumer)
        tokenRepository.save(token)
    }
}