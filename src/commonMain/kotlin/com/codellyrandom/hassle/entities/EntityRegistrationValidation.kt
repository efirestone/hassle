package com.codellyrandom.hassle.entities

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import com.codellyrandom.hassle.ActuatorsByApiName
import com.codellyrandom.hassle.SensorsByApiName
import com.codellyrandom.hassle.values.EntityId

internal class EntityRegistrationValidation(
    private val actuatorsByApiName: ActuatorsByApiName,
    private val sensorsByApiName: SensorsByApiName,
) {
    private val logger = Logger(config = LoggerConfig.default)

    private val failedIds = mutableListOf<EntityId>()

    fun validate(entityIds: List<EntityId>) {
        runActuatorsCheck(entityIds)
        runSensorsCheck(entityIds)

        if (failedIds.isEmpty()) {
            runSuccessProtocol()
            return
        }

        runFailureProtocol()
    }

    private fun runActuatorsCheck(entityIds: List<EntityId>) {
        actuatorsByApiName.forEach { entry ->
            if (entry.key !in entityIds) failedIds.add(entry.key)
        }
    }

    private fun runSensorsCheck(entityIds: List<EntityId>) {
        sensorsByApiName.forEach { entry ->
            if (entry.key !in entityIds) failedIds.add(entry.key)
        }
    }

    private fun runSuccessProtocol() {
        logger.i { "Entity registration validation succeeded" }
    }

    private fun runFailureProtocol() {
        logger.e {
            """


            ################ ERROR #################

            Entity registration validation failed!
            Could not register the following entities:

            ${failedIds.joinToString("\n")}

            These entities could not be found in your
            homeassistant instance. Please check your
            definitions.

            ################ ERROR #################

            """.trimIndent()
        }

        throw EntityRegistrationValidationException("Entity registration validation failed!")
    }
}
