package com.korebit.rigel.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class TokenResponse(
    @param:JsonProperty("access_token")
    @get:JsonProperty("access_token")
    val accessToken: String,
    @param:JsonProperty("refresh_token")
    @get:JsonProperty("refresh_token")
    val refreshToken: String,
) : Serializable
