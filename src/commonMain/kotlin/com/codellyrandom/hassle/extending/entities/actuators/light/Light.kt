package com.codellyrandom.hassle.extending.entities.actuators.light

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.domain

internal inline fun <reified S : State<*>, reified Settable : Any> HomeAssistantApiClient.Light(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<Settable>,
): Actuator<S, Settable> = Actuator(EntityId.fromPair("light".domain to objectId), serviceCommandResolver)
