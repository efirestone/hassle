package khome.testing

import co.touchlab.kermit.Kermit
import khome.ActuatorsByApiName
import khome.ActuatorsByEntity
import khome.HassAPiCommandHistory
import khome.SensorsByApiName
import khome.communicating.HassApiClient
import khome.core.boot.statehandling.flattenStateAttributes
import khome.core.koin.KhomeKoinContext
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.fromJson
import khome.entities.ActuatorStateUpdater
import khome.entities.SensorStateUpdater
import khome.entities.devices.Actuator
import khome.values.EntityId
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.dsl.module

internal class KhomeTestApplicationImpl(
    private val sensorsByApiName: SensorsByApiName,
    private val actuatorsByApiName: ActuatorsByApiName,
    private val actuatorsByEntity: ActuatorsByEntity,
    private val mapper: ObjectMapperInterface,
    private val hassAPiCommandHistory: HassAPiCommandHistory
) : KhomeTestApplication {

    private val logger = Kermit()

    init {
        val testClient = module {
            single<HassApiClient> { HassApiTestClient(get()) }
        }

        KhomeKoinContext.addModule(testClient)
    }

    override fun setStateAndAttributes(json: String) {
        val stateJson = mapper.fromJson<JsonObject>(json)
        val entityIdFromState = checkNotNull(stateJson["entity_id"])
        val entityId = EntityId.fromString(entityIdFromState.jsonPrimitive.content)

        actuatorsByApiName[entityId]?.also {
            ActuatorStateUpdater(actuatorsByApiName).invoke(flattenStateAttributes(stateJson), entityId)
            logger.i { "Set actuator state for $entityId" }
        }

        sensorsByApiName[entityId]?.also {
            SensorStateUpdater(sensorsByApiName).invoke(flattenStateAttributes(stateJson), entityId)
            logger.i { "Set sensor state for $entityId" }
        }
    }

    override fun lastApiCommandFrom(entity: Actuator<*, *>): String =
        actuatorsByEntity[entity]?.let { entityId ->
            hassAPiCommandHistory[entityId]?.let { command ->
                mapper.toJson(command)
            } ?: throw IllegalStateException("No command found for actuator with id: $entityId")
        } ?: throw IllegalStateException("No actuator found.")

    fun reset() {
        hassAPiCommandHistory.clear()
    }
}