package com.codellyrandom.hassle.extending.entities.actuators.light

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Light(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> = Actuator(EntityId.fromPair("light".domain to objectId), serviceCommandResolver)

@Serializable
data class LightAttributes(
    @SerialName("supported_features")
    val supportedFeatures: Int,
    @SerialName("user_id")
    override val userId: UserId?,
    @SerialName("friendly_name")
    override val friendlyName: FriendlyName,
    @SerialName("last_changed")
    override val lastChanged: Instant,
    @SerialName("last_updated")
    override val lastUpdated: Instant
) : Attributes
