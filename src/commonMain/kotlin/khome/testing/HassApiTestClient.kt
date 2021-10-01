package khome.testing

import co.touchlab.kermit.Kermit
import khome.communicating.HassApiClient
import khome.communicating.HassApiCommand
import khome.core.mapping.ObjectMapper

internal class HassApiTestClient(
    private val mapper: ObjectMapper
) : HassApiClient {
    private val logger = Kermit()

    override suspend fun sendCommand(command: HassApiCommand) {
        logger.i { "SANDBOX MODE ACTIVE" }
        mapper.toJson(command).let { serializedCommand ->
            logger.i { "Would have called hass api with message: $serializedCommand" }
        }
    }

    override suspend fun emitEvent(eventType: String, eventData: Any?) {
        TODO("Not yet implemented")
    }
}
