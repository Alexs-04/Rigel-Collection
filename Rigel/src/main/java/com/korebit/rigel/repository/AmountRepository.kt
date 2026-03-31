package com.korebit.rigel.repository

import com.korebit.rigel.model.beans.Amount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AmountRepository : JpaRepository<Amount, Long>

