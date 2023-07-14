package com.codellyrandom.hassle

import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.errorHandling.ErrorResponseData
import com.codellyrandom.hassle.events.EventHandlerFunction
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.EventType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * The client object for interacting with the Home Assistant WebSocket API.
 */
interface HomeAssistantApiClient {

    /**
     * The action that gets executed when an exception occurs while connecting to Home Assistant.
     *
     * A default handler is provided by the main implementation of this interface.
     */
    var connectionExceptionHandler: (Throwable) -> Unit

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
        eventDataType: KType,
        eventHandler: EventHandlerFunction<ED>,
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
     * Sends a JSON command over the Home Assistant websocket and
     * awaits the response.
     *
     * @return The JSON text of the response.
     */
    suspend fun <C : Command, T : Any> await(command: C, commandType: KType, resultType: KType): T

    /**
     * Attaches an error response handler to the API client.
     *
     * @param errorResponseHandler the handler to be attached.
     */
    fun setErrorResponseHandler(errorResponseHandler: (ErrorResponseData) -> Unit)

    // A non-reified interface that external callers can use to create their own custom sensor implementations.
    fun <S : State<*>> Sensor(
        id: EntityId,
        stateType: KType,
    ): Sensor<S>
}

suspend inline fun <reified C : Command, reified T : Any> HomeAssistantApiClient.await(command: C): T =
    await(command, typeOf<C>(), typeOf<T>())
