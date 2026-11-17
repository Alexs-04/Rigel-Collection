package com.korebit.rigel.repository

import com.korebit.rigel.enums.TokenType
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.model.beans.Token
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository : JpaRepository<Token, Long> {
    fun findByToken(token: String): Token?

    fun findByTokenAndTokenType(token: String, tokenType: TokenType): Token?

    fun findAllByConsumerIdAndTokenTypeInAndRevokedFalse(
        consumerId: Long,
        tokenTypes: Collection<TokenType>,
    ): List<Token>

    fun findAllByConsumerAndTokenTypeAndRevokedFalse(
        consumer: Consumer,
        tokenType: TokenType
    ): List<Token>
}
