package com.codellyrandom.hassle.extending.entities

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.values.EntityId
import kotlin.reflect.typeOf

/**
 * Base factories
 */

internal inline fun <reified S : State<*>> HomeAssistantApiClient.Sensor(id: EntityId): Sensor<S> =
    Sensor(id, typeOf<S>())

internal inline fun <reified S : State<*>, reified Settable : Any> HomeAssistantApiClient.Actuator(
    id: EntityId,
    serviceCommandResolver: ServiceCommandResolver<Settable>,
): Actuator<S, Settable> =
    (this as HomeAssistantApiClientImpl).Actuator(id, typeOf<S>(), serviceCommandResolver)
