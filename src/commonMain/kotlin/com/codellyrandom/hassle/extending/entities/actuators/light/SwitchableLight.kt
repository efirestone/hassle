package com.codellyrandom.hassle.extending.entities.actuators.light

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableState
import com.codellyrandom.hassle.extending.entities.mapSwitchable
import com.codellyrandom.hassle.values.ObjectId

typealias SwitchableLight = Actuator<SwitchableState, LightAttributes>

fun HomeAssistantApiClient.SwitchableLight(objectId: ObjectId): SwitchableLight =
    Light(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            mapSwitchable(entityId, desiredState.value)
        },
    )
