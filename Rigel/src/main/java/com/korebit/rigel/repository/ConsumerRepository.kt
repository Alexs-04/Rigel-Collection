package com.korebit.rigel.repository

import com.korebit.rigel.model.Consumer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConsumerRepository : JpaRepository<Consumer, Long> {
    fun findByEmail(email: String): Consumer?
   // fun findByToken(token: String): Consumer?
    fun existsByUsername(username: String): Boolean
}