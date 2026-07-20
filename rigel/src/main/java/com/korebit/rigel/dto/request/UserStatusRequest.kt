package com.korebit.rigel.dto.request

import java.io.Serializable

data class UserStatusRequest(
    val active: Boolean,
) : Serializable

