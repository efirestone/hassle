package com.codellyrandom.hassle.core.boot.statehandling

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.WebSocketSession
import com.codellyrandom.hassle.communicating.GetStatesCommand
import com.codellyrandom.hassle.entities.ActuatorStateUpdater
import com.codellyrandom.hassle.entities.EntityRegistrationValidation
import com.codellyrandom.hassle.entities.SensorStateUpdater
import com.codellyrandom.hassle.values.EntityId
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal class EntityStateInitializer(
    private val apiClient: HomeAssistantApiClientImpl,
    private val session: WebSocketSession,
    private val sensorStateUpdater: SensorStateUpdater,
    private val actuatorStateUpdater: ActuatorStateUpdater,
    private val entityRegistrationValidation: EntityRegistrationValidation,
) {

    private val logger = Logger(config = LoggerConfig.default)

    private val statesRequest = GetStatesCommand()

    suspend fun initialize() {
        val id = sendStatesRequest()
        logger.i { "Requested initial entity states" }
        setInitialEntityState(consumeStatesResponse(id))
    }

    private suspend fun sendStatesRequest() = apiClient.send(statesRequest)

    private suspend fun consumeStatesResponse(id: Int) = session.consumeSingleMessage<StatesResponse>(id)

    private fun setInitialEntityState(stateResponse: StatesResponse) {
        if (stateResponse.success) {
            val statesByEntityId = stateResponse.result.associateBy { state ->
                session.objectMapper.fromJson(state["entity_id"]!!, EntityId::class)
            }
            entityRegistrationValidation.validate(statesByEntityId.map { it.key })
            for (state in statesByEntityId) {
                sensorStateUpdater(flattenStateAttributes(state.value), state.key)
                actuatorStateUpdater(flattenStateAttributes(state.value), state.key)
            }
        }
    }
}

internal fun flattenStateAttributes(stateResponse: JsonObject): JsonObject {
    val attributesAsJsonObject: JsonObject = stateResponse["attributes"]!!.jsonObject
    return JsonObject(
        mapOf(
            "value" to stateResponse["state"]!!,
            "last_updated" to stateResponse["last_updated"]!!,
            "last_changed" to stateResponse["last_changed"]!!,
            "user_id" to stateResponse["context"]!!.jsonObject["user_id"]!!,
        ).plus(attributesAsJsonObject.toMap()),
    )
}
