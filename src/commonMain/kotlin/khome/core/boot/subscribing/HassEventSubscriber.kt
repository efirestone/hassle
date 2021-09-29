package khome.core.boot.subscribing

import co.touchlab.kermit.Kermit
import khome.EventHandlerByEventType
import khome.KhomeSession
import khome.communicating.HassApiClient
import khome.communicating.SubscribeEventCommand
import khome.core.ResultResponse

interface HassEventSubscriber {
    suspend fun subscribe()
}

internal class HassEventSubscriberImpl(
    private val khomeSession: KhomeSession,
    private val subscriptions: EventHandlerByEventType,
    private val hassApi: HassApiClient
) : HassEventSubscriber {
    private val logger = Kermit()

    override suspend fun subscribe() {
        subscriptions.forEach { entry ->
            SubscribeEventCommand(entry.key).also { command -> hassApi.sendCommand(command) }
            khomeSession.consumeSingleMessage<ResultResponse>()
                .takeIf { resultResponse -> resultResponse.success }
                ?.let { logger.i { "Subscribed to event: ${entry.key}" } }
        }
    }
}
