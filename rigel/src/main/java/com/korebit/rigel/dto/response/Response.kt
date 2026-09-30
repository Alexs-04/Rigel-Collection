package com.korebit.rigel.dto.response

@Deprecated("Limit functions, now use EntityResponse provided by Spring Framework")
data class Response(
    val message: String,
    val status: Int,
    val success: Boolean
)