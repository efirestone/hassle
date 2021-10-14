package khome

import khome.communicating.Command
import khome.communicating.ServiceCommandResolver
import khome.core.mapping.ObjectMapper
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.entities.devices.Sensor
import khome.errorHandling.ErrorResponseData
import khome.events.EventHandlerFunction
import khome.observability.Switchable
import khome.values.EntityId
import khome.values.EventType
import kotlin.reflect.KClass

internal class TestHomeAssistantApiClient : HomeAssistantApiClient {
    val callServiceRequests = mutableListOf<String>()

    private val mapper = ObjectMapper()

    @Suppress("FunctionName")
    override fun <S : State<*>, A : Attributes> Sensor(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>
    ): Sensor<S, A> {
        TODO("Not yet implemented")
    }

    @Suppress("FunctionName")
    override fun <S : State<*>, A : Attributes> Actuator(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>,
        serviceCommandResolver: ServiceCommandResolver<S>
    ): Actuator<S, A> {
        TODO("Not yet implemented")
    }

    override var observerExceptionHandler: (Throwable) -> Unit = {}

    override fun connect() {}

    override fun <ED> attachEventHandler(
        eventType: EventType,
        eventDataType: KClass<*>,
        eventHandler: EventHandlerFunction<ED>
    ): Switchable {
        TODO("Not yet implemented")
    }

    override fun setEventHandlerExceptionHandler(f: (Throwable) -> Unit) {}

    override suspend fun emitEvent(eventType: String, eventData: Any?) {}

    override fun setErrorResponseHandler(errorResponseHandler: (ErrorResponseData) -> Unit) {}

    override suspend fun send(command: Command) {
        val json = mapper.toJson(command)
        callServiceRequests.add(json)
    }
}
