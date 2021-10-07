package khome.testing

import co.touchlab.kermit.Kermit
import khome.communicating.HassApiClient
import khome.communicating.HassApiCommand
import khome.core.mapping.ObjectMapper
import kotlin.reflect.KType

internal class HassApiTestClient(
    private val mapper: ObjectMapper
) : HassApiClient {
    private val logger = Kermit()

    override suspend fun <SD> sendCommand(command: HassApiCommand<SD>, parameterType: KType) {
        logger.i { "SANDBOX MODE ACTIVE" }
        mapper.toJson(command).let { serializedCommand ->
            logger.i { "Would have called hass api with message: $serializedCommand" }
        }
    }

    override suspend fun emitEvent(eventType: String, eventData: Any?) {
        TODO("Not yet implemented")
    }
}
