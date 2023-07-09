package com.codellyrandom.hassle.extending.entities.actuators.climate

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.Actuator
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.domain

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.ClimateControl(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>,
): Actuator<S, A> = Actuator(EntityId.fromPair("climate".domain to objectId), serviceCommandResolver)
