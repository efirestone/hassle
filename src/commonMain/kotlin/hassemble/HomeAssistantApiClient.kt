package hassemble

import hassemble.errorHandling.ErrorResponseData
import hassemble.events.EventHandlerFunction
import hassemble.observability.Switchable
import hassemble.values.EventType
import kotlin.reflect.KClass

/**
 * The client object for interacting with the Home Assistant WebSocket API.
 */
interface HomeAssistantApiClient {
    /**
     * The action that gets executed when the observer action executes with an exception.
     *
     * A default handler is provided by the main implementation of this interface.
     */
    var observerExceptionHandler: (Throwable) -> Unit

    /**
     * Attaches an [EventHandlerFunction] to the API client and starts the Home Assistant event subscription.
     */
    fun <ED> attachEventHandler(
        eventType: EventType,
        eventDataType: KClass<*>,
        eventHandler: EventHandlerFunction<ED>
    ): Switchable

    fun connect()

    /**
     * Overwrites the default event handler exception handler action.
     *
     * @param f the action that gets executed when the event handler action executes with an exception.
     */
    fun setEventHandlerExceptionHandler(f: (Throwable) -> Unit)

    /**
     * Emits a Home Assistant event with optional event data.
     *
     * @param eventType the type of event to emit.
     * @param eventData the data to be sent with the event (optional).
     */
    suspend fun emitEvent(eventType: String, eventData: Any? = null)

    /**
     * Attaches an error response handler to the API client.
     *
     * @param errorResponseHandler the handler to be attached.
     */
    fun setErrorResponseHandler(errorResponseHandler: (ErrorResponseData) -> Unit)
}
