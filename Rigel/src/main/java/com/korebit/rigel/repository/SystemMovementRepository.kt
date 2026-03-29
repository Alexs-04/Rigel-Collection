package com.korebit.rigel.repository

import com.korebit.rigel.model.beans.SystemMovement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface SystemMovementRepository : JpaRepository<SystemMovement, Long>, JpaSpecificationExecutor<SystemMovement>

