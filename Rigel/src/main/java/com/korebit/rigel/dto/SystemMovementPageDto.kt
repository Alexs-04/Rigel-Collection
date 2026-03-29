package com.korebit.rigel.dto

import java.io.Serializable

data class SystemMovementPageDto(
    val items: List<SystemMovementDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
) : Serializable

