package com.codellyrandom.hassle.communicating

import kotlinx.serialization.Serializable

@Serializable
internal class ConfigEntityRegistrationListCommand(
    override var id: Int? = null,
    private val type: String = "config/entity_registry/list",
) : CommandImpl(id) {
    override fun copy(id: Int?) = ConfigEntityRegistrationListCommand(id = id)
}
