package com.korebit.rigel.repository

import com.korebit.rigel.model.beans.Token
import org.springframework.data.jpa.repository.JpaRepository

interface TokenRepository : JpaRepository<Token, Long> {
    fun findAllValidIsFalseOrRevokedIsFalseByConsumerId(id: Long): MutableList<Token>
}