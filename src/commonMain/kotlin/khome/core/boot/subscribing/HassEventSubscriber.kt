package khome.core.boot.subscribing

import co.touchlab.kermit.Kermit
import khome.EventHandlerByEventType
import khome.WebSocketSession
import khome.communicating.HassApiClient
import khome.communicating.SubscribeEventCommand
import khome.core.ResultResponse

internal class HassEventSubscriber(
    private val session: WebSocketSession,
    private val subscriptions: EventHandlerByEventType,
    private val hassApi: HassApiClient
) {
    private val logger = Kermit()

    suspend fun subscribe() {
        subscriptions.forEach { entry ->
            SubscribeEventCommand(eventType = entry.key).also { command -> hassApi.send(command) }
            session.consumeSingleMessage<ResultResponse>()
                .takeIf { resultResponse -> resultResponse.success }
                ?.let { logger.i { "Subscribed to event: ${entry.key}" } }
        }
    }
}
