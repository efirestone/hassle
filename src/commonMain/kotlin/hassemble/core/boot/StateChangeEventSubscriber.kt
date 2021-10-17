package hassemble.core.boot

import co.touchlab.kermit.Kermit
import hassemble.WebSocketSession
import hassemble.communicating.CALLER_ID
import hassemble.core.ResultResponse

internal class StateChangeEventSubscriber(
    private val session: WebSocketSession
) {

    private val logger = Kermit()
    private val id
        get() = CALLER_ID.incrementAndGet()

    suspend fun subscribe() {
        sendEventListenerRequest()
        consumeResultResponse().let { resultResponse ->
            when (resultResponse.success) {
                false -> logger.e { "Could not subscribe to state change events" }
                true -> logger.i { "Successfully started listening to state changes" }
            }
        }
    }

    private val eventListenerRequest =
        EventListeningRequest(id = id, eventType = "state_changed")

    private suspend fun sendEventListenerRequest() =
        session.callWebSocketApi(eventListenerRequest)

    private suspend fun consumeResultResponse() =
        session.consumeSingleMessage<ResultResponse>()
}
