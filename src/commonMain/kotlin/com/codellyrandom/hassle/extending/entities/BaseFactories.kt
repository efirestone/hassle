package com.codellyrandom.hassle.extending.entities

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.values.EntityId

/**
 * Base factories
 */

internal inline fun <reified S : State<*>> HomeAssistantApiClient.Sensor(id: EntityId): Sensor<S> =
    Sensor(id, S::class)

internal inline fun <reified S : State<*>, reified Settable : Any> HomeAssistantApiClient.Actuator(
    id: EntityId,
    serviceCommandResolver: ServiceCommandResolver<Settable>,
): Actuator<S, Settable> =
    (this as HomeAssistantApiClientImpl).Actuator(id, S::class, serviceCommandResolver)
