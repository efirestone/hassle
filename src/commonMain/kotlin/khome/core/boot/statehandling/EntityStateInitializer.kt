package khome.core.boot.statehandling

import co.touchlab.kermit.Kermit
import khome.KhomeSession
import khome.communicating.CALLER_ID
import khome.core.koin.KhomeComponent
import khome.entities.ActuatorStateUpdater
import khome.entities.EntityRegistrationValidation
import khome.entities.SensorStateUpdater
import khome.values.EntityId
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

interface EntityStateInitializer {
    suspend fun initialize()
}

internal class EntityStateInitializerImpl(
    val khomeSession: KhomeSession,
    private val sensorStateUpdater: SensorStateUpdater,
    private val actuatorStateUpdater: ActuatorStateUpdater,
    private val entityRegistrationValidation: EntityRegistrationValidation
) : EntityStateInitializer, KhomeComponent {

    private val logger = Kermit()
    private val id
        get() = CALLER_ID.incrementAndGet()

    private val statesRequest = StatesRequest(id)

    override suspend fun initialize() {
        sendStatesRequest()
        logger.i { "Requested initial entity states" }
        setInitialEntityState(consumeStatesResponse())
    }

    private suspend fun sendStatesRequest() =
        khomeSession.callWebSocketApi(statesRequest)

    private suspend fun consumeStatesResponse() =
        khomeSession.consumeSingleMessage<StatesResponse>()

    private fun setInitialEntityState(stateResponse: StatesResponse) {
        if (stateResponse.success) {
            val statesByEntityId = stateResponse.result.associateBy { state ->
                khomeSession.objectMapper.fromJson(state["entity_id"]!!, EntityId::class)
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
