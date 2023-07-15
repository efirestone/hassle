package com.codellyrandom.hassle

import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.errorHandling.ErrorResponseData
import com.codellyrandom.hassle.events.EventHandlerFunction
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.EventType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KType

class TestHomeAssistantApiClient(
    override var connectionExceptionHandler: (Throwable) -> Unit = {},
    override var observerExceptionHandler: (Throwable) -> Unit = {},
    private val objectMapper: ObjectMapper = ObjectMapper(),
) : HomeAssistantApiClient {
    private val responsesByCommandType = mutableMapOf<String, String>()

    override fun <ED> attachEventHandler(
        eventType: EventType,
        eventDataType: KType,
        eventHandler: EventHandlerFunction<ED>,
    ): Switchable {
        TODO("Not yet implemented")
    }

    override fun connect() {}

    override fun setEventHandlerExceptionHandler(f: (Throwable) -> Unit) {}

    override suspend fun emitEvent(eventType: String, eventData: Any?) {}

    override suspend fun <C : Command, T : Any> await(command: C, commandType: KType, resultType: KType): T {
        val commandJson = Json.parseToJsonElement(objectMapper.toJson(command, commandType))
        val commandName = commandJson.jsonObject["type"]?.jsonPrimitive?.content
        val responseJsonString = responsesByCommandType[commandName]

        requireNotNull(responseJsonString) { "No test response is registered for the command type $commandName" }

        // Remove the command wrapper shell
        val responseJson = Json.parseToJsonElement(responseJsonString)
            .jsonArray.first()
            .jsonObject["result"]

        requireNotNull(responseJson) { "No result is present in registered test response for the command type $commandName" }

        return objectMapper.fromJson(responseJson, resultType)
    }

    override fun setErrorResponseHandler(errorResponseHandler: (ErrorResponseData) -> Unit) {}

    override fun <S : State<*>> Sensor(id: EntityId, stateType: KType): Sensor<S> {
        TODO("Not yet implemented")
    }

    fun setResponse(jsonString: String, forCommandType: String) {
        responsesByCommandType[forCommandType] = jsonString
    }
}
