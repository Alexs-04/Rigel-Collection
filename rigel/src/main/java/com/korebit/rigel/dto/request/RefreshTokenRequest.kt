package com.korebit.rigel.dto.request

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class RefreshTokenRequest(
    @param:JsonProperty("refresh_token")
    @param:JsonAlias("refreshToken")
    val refreshToken: String? = null,
)
