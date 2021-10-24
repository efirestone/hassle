package com.codellyrandom.hassle.core.boot.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AuthRequest(
    val type: String = "auth",
    @SerialName("access_token")
    val accessToken: String
)
