package hassemble.core.boot.subscribing

import co.touchlab.kermit.Kermit
import hassemble.EventHandlerByEventType
import hassemble.WebSocketSession
import hassemble.communicating.HassApiClient
import hassemble.communicating.SubscribeEventCommand
import hassemble.core.ResultResponse

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
