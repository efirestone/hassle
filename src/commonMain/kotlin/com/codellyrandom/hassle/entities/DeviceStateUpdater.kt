package com.codellyrandom.hassle.entities

import co.touchlab.kermit.Kermit
import com.codellyrandom.hassle.ActuatorsByApiName
import com.codellyrandom.hassle.SensorsByApiName
import com.codellyrandom.hassle.values.EntityId
import kotlinx.serialization.json.JsonObject

internal class ActuatorStateUpdater(private val actuatorsByApiName: ActuatorsByApiName) {
    private val logger = Kermit()

    operator fun invoke(newActualState: JsonObject, entityId: EntityId) {
        actuatorsByApiName[entityId]?.let { entity ->
            entity.trySetAttributesFromAny(newAttributes = newActualState)
            entity.trySetActualStateFromAny(newState = newActualState)
            logger.d { "Updated state for entity: $entityId with: $newActualState" }
        }
    }
}

internal class SensorStateUpdater(private val sensorsByApiName: SensorsByApiName) {
    private val logger = Kermit()

    operator fun invoke(newActualState: JsonObject, entityId: EntityId) {
        sensorsByApiName[entityId]?.let { entity ->
            entity.trySetAttributesFromAny(newAttributes = newActualState)
            entity.trySetActualStateFromAny(newState = newActualState)
            logger.d { "Updated state for entity: $entityId with: $newActualState" }
        }
    }
}
