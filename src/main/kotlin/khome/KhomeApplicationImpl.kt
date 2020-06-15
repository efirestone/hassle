package khome

import io.ktor.client.statement.HttpResponse
import io.ktor.util.KtorExperimentalAPI
import khome.communicating.CommandDataWithEntityId
import khome.communicating.HassApi
import khome.communicating.ServiceCommandImpl
import khome.communicating.ServiceCommandResolver
import khome.communicating.SubscribeEventCommand
import khome.core.Attributes
import khome.core.ResultResponse
import khome.core.State
import khome.core.boot.EventResponseConsumer
import khome.core.boot.HassApiInitializer
import khome.core.boot.StateChangeEventSubscriber
import khome.core.boot.authentication.Authenticator
import khome.core.boot.servicestore.ServiceStoreInitializer
import khome.core.boot.statehandling.EntityStateInitializer
import khome.core.koin.KhomeComponent
import khome.core.mapping.ObjectMapper
import khome.entities.ActuatorStateUpdater
import khome.entities.EntityId
import khome.entities.EntityRegistrationValidation
import khome.entities.SensorStateUpdater
import khome.entities.devices.Actuator
import khome.entities.devices.ActuatorImpl
import khome.entities.devices.Sensor
import khome.entities.devices.SensorImpl
import khome.errorHandling.AsyncEventHandlerExceptionHandler
import khome.errorHandling.AsyncObserverExceptionHandler
import khome.errorHandling.ErrorResponseData
import khome.errorHandling.ErrorResponseHandlerImpl
import khome.errorHandling.EventHandlerExceptionHandler
import khome.errorHandling.ObserverExceptionHandler
import khome.events.AsyncEventHandlerImpl
import khome.events.EventHandlerImpl
import khome.events.EventSubscription
import khome.observability.AsyncObserverImpl
import khome.observability.HistorySnapshot
import khome.observability.ObserverImpl
import khome.observability.StateAndAttributes
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.koin.core.get
import org.koin.core.inject
import kotlin.reflect.KClass

typealias StateAndAttributesHistorySnapshot<S, A> = HistorySnapshot<S, A, StateAndAttributes<S, A>>

internal typealias SensorsByApiName = MutableMap<EntityId, SensorImpl<*, *>>
internal typealias ActuatorsByApiName = MutableMap<EntityId, ActuatorImpl<*, *>>
internal typealias ActuatorsByEntity = MutableMap<ActuatorImpl<*, *>, EntityId>
internal typealias EventHandlerByEventType = MutableMap<String, EventSubscription>
internal typealias ErrorResponseHandler = MutableList<Switchable>

@Suppress("FunctionName")
interface KhomeApplication {
    fun runBlocking()

    fun <S : State<*>, SA : Attributes> Sensor(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>
    ): Sensor<S, SA>

    fun <S : State<*>, SA : Attributes> Actuator(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>,
        serviceCommandResolver: ServiceCommandResolver<S>
    ): Actuator<S, SA>

    fun <S, A> Observer(f: (snapshot: StateAndAttributesHistorySnapshot<S, A>, Switchable) -> Unit): Switchable
    fun <S, A> AsyncObserver(f: suspend CoroutineScope.(snapshot: StateAndAttributesHistorySnapshot<S, A>, Switchable) -> Unit): Switchable

    fun overwriteObserverExceptionHandler(f: (Throwable) -> Unit)

    fun attachEventHandler(eventType: String, eventHandler: Switchable, eventDataType: KClass<*>)
    fun <ED> EventHandler(f: (ED, Switchable) -> Unit): Switchable
    fun <ED> AsyncEventHandler(f: suspend CoroutineScope.(ED, Switchable) -> Unit): Switchable

    fun overwriteEventHandlerExceptionHandler(f: (Throwable) -> Unit)

    fun emitEvent(eventType: String, eventData: Any? = null)
    fun emitEventAsync(eventType: String, eventData: Any? = null): Deferred<HttpResponse>

    fun ErrorResponseHandler(f: (ErrorResponseData) -> Unit): Switchable
    fun attachErrorResponseHandler(errorResponseHandler: Switchable)

    fun <PB> callService(domain: String, service: Enum<*>, parameterBag: PB)
}

@OptIn(
    ExperimentalStdlibApi::class,
    KtorExperimentalAPI::class,
    ObsoleteCoroutinesApi::class,
    ExperimentalCoroutinesApi::class
)
internal class KhomeApplicationImpl : KhomeApplication {
    private val logger = KotlinLogging.logger { }
    private val koin = object : KhomeComponent {}
    private val hassClient: HassClient by koin.inject()
    private val hassApi: HassApi by koin.inject()
    private val mapper: ObjectMapper by koin.inject()

    private val sensorsByApiName: SensorsByApiName = mutableMapOf()
    private val actuatorsByApiName: ActuatorsByApiName = mutableMapOf()
    private val actuatorsByEntity: ActuatorsByEntity = mutableMapOf()

    private val eventSubscriptionsByEventType: EventHandlerByEventType = mutableMapOf()
    private val errorResponseSubscriptions: ErrorResponseHandler = mutableListOf()

    private var observerExceptionHandlerFunction: (Throwable) -> Unit = { exception ->
        logger.error(exception) { "Caught exception in observer" }
    }

    private var eventHandlerExceptionHandlerFunction: (Throwable) -> Unit = { exception ->
        logger.error(exception) { "Caught exception in event handler" }
    }

    init {
        val defaultErrorResponseHandler = ErrorResponseHandler { errorResponseData ->
            logger.error { "CommandId: ${errorResponseData.commandId} -  errorCode: ${errorResponseData.errorResponse.code} ${errorResponseData.errorResponse.message}" }
        }

        attachErrorResponseHandler(defaultErrorResponseHandler)
    }

    override fun <S : State<*>, A : Attributes> Sensor(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>
    ): Sensor<S, A> =
        SensorImpl<S, A>(mapper, stateValueType, attributesValueType).also { registerSensor(id, it) }

    override fun <S : State<*>, A : Attributes> Actuator(
        id: EntityId,
        stateValueType: KClass<*>,
        attributesValueType: KClass<*>,
        serviceCommandResolver: ServiceCommandResolver<S>
    ): Actuator<S, A> =
        ActuatorImpl<S, A>(
            this,
            mapper,
            serviceCommandResolver,
            stateValueType,
            attributesValueType
        ).also { registerActuator(id, it) }

    override fun <S, A> Observer(f: (snapshot: StateAndAttributesHistorySnapshot<S, A>, Switchable) -> Unit): Switchable =
        ObserverImpl(f, ObserverExceptionHandler(observerExceptionHandlerFunction))

    override fun <S, A> AsyncObserver(f: suspend CoroutineScope.(snapshot: StateAndAttributesHistorySnapshot<S, A>, Switchable) -> Unit): Switchable =
        AsyncObserverImpl(f, AsyncObserverExceptionHandler(observerExceptionHandlerFunction))

    override fun overwriteObserverExceptionHandler(f: (Throwable) -> Unit) {
        observerExceptionHandlerFunction = f
    }

    override fun attachEventHandler(eventType: String, eventHandler: Switchable, eventDataType: KClass<*>) {
        eventSubscriptionsByEventType[eventType]?.attachEventHandler(eventHandler)
            ?: registerEventSubscription(eventType, eventDataType).attachEventHandler(eventHandler)
    }

    override fun <ED> EventHandler(f: (ED, Switchable) -> Unit): Switchable =
        EventHandlerImpl(f, EventHandlerExceptionHandler(eventHandlerExceptionHandlerFunction))

    override fun <ED> AsyncEventHandler(f: suspend CoroutineScope.(ED, Switchable) -> Unit): Switchable =
        AsyncEventHandlerImpl(f, AsyncEventHandlerExceptionHandler(eventHandlerExceptionHandlerFunction))

    override fun overwriteEventHandlerExceptionHandler(f: (Throwable) -> Unit) {
        eventHandlerExceptionHandlerFunction = f
    }

    override fun emitEvent(eventType: String, eventData: Any?) {
        hassApi.emitEvent(eventType, eventData)
    }

    override fun emitEventAsync(eventType: String, eventData: Any?): Deferred<HttpResponse> =
        hassApi.emitEventAsync(eventType, eventData)

    override fun ErrorResponseHandler(f: (ErrorResponseData) -> Unit): Switchable =
        ErrorResponseHandlerImpl(f)

    override fun attachErrorResponseHandler(errorResponseHandler: Switchable) {
        errorResponseSubscriptions.add(errorResponseHandler)
    }

    override fun <PB> callService(domain: String, service: Enum<*>, parameterBag: PB) {
        ServiceCommandImpl<PB>(
            domain = domain,
            service = service.name,
            serviceData = parameterBag
        ).also { hassApi.sendHassApiCommand(it) }
    }

    private fun registerSensor(entityId: EntityId, sensor: SensorImpl<*, *>) {
        check(!sensorsByApiName.containsKey(entityId)) { "Sensor with id: $entityId already exists." }
        sensorsByApiName[entityId] = sensor
    }

    private fun registerActuator(entityId: EntityId, actuator: ActuatorImpl<*, *>) {
        check(!actuatorsByApiName.containsKey(entityId)) { "Actuator with id: $entityId already exists." }
        actuatorsByApiName[entityId] = actuator
        actuatorsByEntity[actuator] = entityId
    }

    private fun registerEventSubscription(eventType: String, eventDataType: KClass<*>) =
        EventSubscription(mapper, eventDataType).also { eventSubscriptionsByEventType[eventType] = it }

    internal fun <S : State<*>, SA : Attributes> enqueueStateChange(
        actuator: ActuatorImpl<S, SA>,
        commandImpl: ServiceCommandImpl<CommandDataWithEntityId>
    ) {
        val entityId = actuatorsByEntity[actuator] ?: throw RuntimeException("Entity not registered: $actuator")
        commandImpl.apply {
            domain = entityId.domain
            serviceData?.entityId = entityId
        }
        hassApi.sendHassApiCommand(commandImpl)
    }

    override fun runBlocking() =
        runBlocking {
            hassClient.startSession {

                Authenticator(
                    khomeSession = this,
                    configuration = get()
                ).runStartSequenceStep()

                ServiceStoreInitializer(
                    khomeSession = this,
                    serviceStore = get()
                ).runStartSequenceStep()

                HassApiInitializer(khomeSession = this).runStartSequenceStep()

                eventSubscriptionsByEventType.forEach { entry ->
                    SubscribeEventCommand(entry.key).also { command -> hassApi.sendHassApiCommand(command) }
                    consumeSingleMessage<ResultResponse>()
                        .takeIf { resultResponse -> resultResponse.success }
                        ?.let { logger.info { "Subscribed to event: ${entry.key}" } }
                }

                EntityStateInitializer(
                    khomeSession = this,
                    sensorStateUpdater = SensorStateUpdater(sensorsByApiName),
                    actuatorStateUpdater = ActuatorStateUpdater(actuatorsByApiName),
                    entityRegistrationValidation = EntityRegistrationValidation(actuatorsByApiName, sensorsByApiName)
                ).runStartSequenceStep()

                StateChangeEventSubscriber(khomeSession = this).runStartSequenceStep()

                EventResponseConsumer(
                    khomeSession = this,
                    objectMapper = get(),
                    sensorStateUpdater = SensorStateUpdater(sensorsByApiName),
                    actuatorStateUpdater = ActuatorStateUpdater(actuatorsByApiName),
                    eventHandlerByEventType = eventSubscriptionsByEventType,
                    errorResponseHandler = errorResponseSubscriptions
                ).runStartSequenceStep()
            }
        }
}