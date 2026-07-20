package com.korebit.rigel.dto.response

data class ImageUploadResponse(
    val cloudinaryPublicId: String? = null,
    val imageUrl: String? = null,
    val message: String? = null,
    val success: Boolean = true,
)

