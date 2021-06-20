package khome.testing

import co.touchlab.kermit.Kermit
import io.ktor.client.statement.HttpResponse
import khome.communicating.HassApiClient
import khome.communicating.HassApiCommand
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job

internal class HassApiTestClient(
    private val mapper: ObjectMapper
) : HassApiClient {
    private val logger = Kermit()

    override fun sendCommand(command: HassApiCommand): Job {
        logger.i { "SANDBOX MODE ACTIVE" }
        mapper.toJson(command).let { serializedCommand ->
            logger.i { "Would have called hass api with message: $serializedCommand" }
        }
        return Job()
    }

    override fun emitEvent(eventType: String, eventData: Any?) {
        TODO("Not yet implemented")
    }

    override fun emitEventAsync(eventType: String, eventData: Any?): Deferred<HttpResponse> {
        TODO("Not yet implemented")
    }
}
