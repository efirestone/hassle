package com.codellyrandom.hassle.values

import DeviceId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class EntityRegistration(
    @SerialName("config_entry_id")
    val configEntryId: ConfigEntryId,
    @SerialName("device_id")
    val deviceId: DeviceId,
    @SerialName("entity_id")
    val entityId: EntityId,
    val id: String,
    @SerialName("original_name")
    val originalName: String,
    val platform: String,
    @SerialName("unique_id")
    val uniqueId: String,
)
