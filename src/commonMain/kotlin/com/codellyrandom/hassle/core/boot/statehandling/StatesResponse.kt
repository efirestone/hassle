package com.codellyrandom.hassle.core.boot.statehandling

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
class StatesResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val result: Array<JsonObject>
)
