package com.korebit.rigel.repository

import com.korebit.rigel.model.beans.Consumer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConsumerRepository : JpaRepository<Consumer, Long> {
    fun findByEmail(email: String): Consumer?
    fun existsByUsername(username: String): Boolean
    fun existsByUsernameAndIdNot(username: String, id: Long): Boolean

    fun findByUsername(username: String): Consumer?

    fun existsByEmail(email: String): Boolean
    fun existsByEmailAndIdNot(email: String, id: Long): Boolean
}