package com.codellyrandom.hassle.values

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConfigEntry(
    @SerialName("entry_id")
    val entryId: ConfigEntryId,
    val domain: Domain,
    val title: String,
    val source: Source,
    val state: State,
    @SerialName("supports_options")
    val supportsOptions: Boolean,
    @SerialName("supports_remove_device")
    val supportsRemoveDevice: Boolean,
    @SerialName("supports_unload")
    val supportsUnload: Boolean,
    @SerialName("disabled_by")
    val disabledBy: String? = null,
    val reason: String? = null,
) {
    @Serializable
    enum class Source {
        @SerialName("import")
        IMPORT,

        @SerialName("onboarding")
        ONBOARDING,

        @SerialName("user")
        USER,
    }

    @Serializable
    enum class State {
        @SerialName("loaded")
        LOADED,
    }
}
