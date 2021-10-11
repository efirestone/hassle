package khome.core.boot.statehandling

import co.touchlab.kermit.Kermit
import khome.WebSocketSession
import khome.communicating.CALLER_ID
import khome.entities.ActuatorStateUpdater
import khome.entities.EntityRegistrationValidation
import khome.entities.SensorStateUpdater
import khome.values.EntityId
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal class EntityStateInitializer(
    private val session: WebSocketSession,
    private val sensorStateUpdater: SensorStateUpdater,
    private val actuatorStateUpdater: ActuatorStateUpdater,
    private val entityRegistrationValidation: EntityRegistrationValidation
) {

    private val logger = Kermit()
    private val id
        get() = CALLER_ID.incrementAndGet()

    private val statesRequest = StatesRequest(id)

    suspend fun initialize() {
        sendStatesRequest()
        logger.i { "Requested initial entity states" }
        setInitialEntityState(consumeStatesResponse())
    }

    private suspend fun sendStatesRequest() =
        session.callWebSocketApi(statesRequest)

    private suspend fun consumeStatesResponse() =
        session.consumeSingleMessage<StatesResponse>()

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
            "user_id" to stateResponse["context"]!!.jsonObject["user_id"]!!
        ).plus(attributesAsJsonObject.toMap())
    )
}
