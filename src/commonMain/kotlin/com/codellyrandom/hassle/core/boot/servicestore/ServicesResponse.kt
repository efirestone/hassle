package com.codellyrandom.hassle.core.boot.servicestore

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ServicesResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val result: Map<String, JsonObject> = mapOf(),
)
