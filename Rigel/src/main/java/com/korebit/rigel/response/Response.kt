package com.korebit.rigel.response

data class Response(
    val message: String,
    val status: Int,
    val success: Boolean
)