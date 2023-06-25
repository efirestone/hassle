package com.codellyrandom.hassle.extending.entities

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.values.EntityId
import kotlin.reflect.KClass

/**
 * Base factories
 */

internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Sensor(id: EntityId): Sensor<S, A> =
    (this as HomeAssistantApiClientImpl).Sensor(id, S::class, A::class)

// A non-reified interface that external callers can use to create their own custom sensor implementations.
fun <S : State<*>, A : Attributes> HomeAssistantApiClient.Sensor(
    id: EntityId,
    stateType: KClass<S>,
    attributesType: KClass<A>
): Sensor<S, A> =
    (this as HomeAssistantApiClientImpl).Sensor(id, stateType, attributesType)

internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Actuator(
    id: EntityId,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> =
    (this as HomeAssistantApiClientImpl).Actuator(id, S::class, A::class, serviceCommandResolver)
