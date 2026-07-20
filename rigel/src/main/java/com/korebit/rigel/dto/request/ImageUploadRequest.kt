package com.korebit.rigel.dto.request

data class ImageUploadRequest(
    val imageBase64: String? = null,
    val fileName: String? = null,
)

