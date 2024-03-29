package com.codellyrandom.hassle.core.boot

import co.touchlab.kermit.Logger
import com.codellyrandom.hassle.EventHandlerByEventType
import com.codellyrandom.hassle.WebSocketSession
import com.codellyrandom.hassle.core.*
import com.codellyrandom.hassle.core.boot.statehandling.flattenStateAttributes
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.entities.ActuatorStateUpdater
import com.codellyrandom.hassle.entities.SensorStateUpdater
import com.codellyrandom.hassle.errorHandling.ErrorResponseData
import com.codellyrandom.hassle.errorHandling.ErrorResponseHandlerImpl
import com.codellyrandom.hassle.values.EventType
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject

internal class EventResponseConsumer(
    private val session: WebSocketSession,
    private val objectMapper: ObjectMapper,
    private val sensorStateUpdater: SensorStateUpdater,
    private val actuatorStateUpdater: ActuatorStateUpdater,
    private val eventHandlerByEventType: EventHandlerByEventType,
    private val errorResponseHandler: (ErrorResponseData) -> Unit,
) {
    private val logger = Logger

    suspend fun consumeBlocking() = coroutineScope {
        session.consumeEachMappedToResponse { response, frameText ->
            when (response.type) {
                ResponseType.EVENT -> {
                    handleStateChangedResponse(frameText)
                    handleEventResponse(frameText)
                }
                ResponseType.RESULT -> {
                    handleSuccessResultResponse(frameText)
                    handleErrorResultResponse(frameText)
                }
            }
        }
    }

    private inline fun <reified Response> mapFrameTextToResponse(frameText: Frame.Text): Response =
        objectMapper.fromJson(frameText.readText())

    private suspend inline fun WebSocketSession.consumeEachMappedToResponse(action: (ResolverResponse, Frame.Text) -> Unit) =
        incoming.consumeEach { frame ->
            (frame as? Frame.Text)?.let { frameText -> action(mapFrameTextToResponse(frameText), frameText) }
                ?: throw IllegalStateException("Frame could not be cast to Frame.Text")
        }

    private fun handleStateChangedResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<StateChangedResponse>(frameText)
            .takeIf { it.event.eventType == "state_changed" }
            ?.let { stateChangedResponse ->
                logger.d { "State change response: $stateChangedResponse" }
                stateChangedResponse.event.data.newState.getOrNull()?.let { newState ->
                    sensorStateUpdater(
                        flattenStateAttributes(newState.jsonObject),
                        stateChangedResponse.event.data.entityId,
                    )
                    actuatorStateUpdater(
                        flattenStateAttributes(newState.jsonObject),
                        stateChangedResponse.event.data.entityId,
                    )
                }
            }

    private fun handleEventResponse(frameText: Frame.Text) {
        mapFrameTextToResponse<EventResponse>(frameText)
            .takeIf { EventType(it.event.eventType) in eventHandlerByEventType }
            ?.let { eventResponse ->
                logger.d { "Event response: $eventResponse" }
                eventHandlerByEventType[EventType(eventResponse.event.eventType)]
                    ?.invokeEventHandler(eventResponse.event.data)
                    ?: logger.w { "No event found for event type: ${eventResponse.event.eventType}" }
            }
    }

    private fun handleSuccessResultResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<ResultResponse>(frameText)
            .takeIf { resultResponse -> resultResponse.success }
            ?.let { resultResponse -> logSuccessResult(resultResponse) }

    private fun handleErrorResultResponse(frameText: Frame.Text) =
        mapFrameTextToResponse<ResultResponse>(frameText)
            .takeIf { resultResponse -> !resultResponse.success }
            ?.let { resultResponse ->
                ErrorResponseHandlerImpl(errorResponseHandler).handle(
                    ErrorResponseData(
                        commandId = resultResponse.id,
                        errorResponse = resultResponse.error!!,
                    ),
                )
            }

    private fun logSuccessResult(resultResponse: ResultResponse) =
        logger.i { "Result-Id: ${resultResponse.id} | Success: ${resultResponse.success}" }
}

private fun JsonElement.getOrNull(): JsonElement? = if (this is JsonNull) null else this
