package com.codellyrandom.hassle.extending.entities.sensors.binary

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.extending.entities.Sensor
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.domain

@Suppress("FunctionName")
internal inline fun <reified S : State<*>> HomeAssistantApiClient.BinarySensor(objectId: ObjectId): Sensor<S> =
    Sensor(EntityId.fromPair("binary_sensor".domain to objectId))
