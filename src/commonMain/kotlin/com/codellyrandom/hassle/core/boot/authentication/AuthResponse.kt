package com.codellyrandom.hassle.core.boot.authentication

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AuthResponse(
    val type: String,
    val message: String? = null,
    @SerialName("ha_version")
    val haVersion: String,
)
