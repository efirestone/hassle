package khome

import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import khome.communicating.*
import khome.core.Credentials
import khome.core.boot.EventResponseConsumer
import khome.core.boot.StateChangeEventSubscriber
import khome.core.boot.authentication.Authenticator
import khome.core.boot.servicestore.ServiceStore
import khome.core.boot.servicestore.ServiceStoreInitializer
import khome.core.boot.statehandling.EntityStateInitializer
import khome.core.boot.subscribing.HassEventSubscriber
import khome.core.mapping.ObjectMapper
import khome.coroutines.MainDispatcherFactory
import khome.entities.*
import khome.entities.devices.Actuator
import khome.entities.devices.Sensor
import khome.errorHandling.ErrorResponseData
import khome.events.EventHandlerFunction
import khome.events.EventSubscription
import khome.observability.Switchable
import khome.values.Domain
import khome.values.EntityId
import khome.values.EventType
import khome.values.Service
import kotlinx.coroutines.*
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlinx.serialization.json.Json as SerializationJson

internal typealias SensorsByApiName = MutableMap<EntityId, Sensor<*, *>>
internal typealias ActuatorsByApiName = MutableMap<EntityId, Actuator<*, *>>
internal typealias ActuatorsByEntity = MutableMap<Actuator<*, *>, EntityId>
internal typealias EventHandlerByEventType = MutableMap<EventType, EventSubscription<*>>
internal typealias HassApiCommandHistory = MutableMap<EntityId, ServiceCommand<CommandDataWithEntityId>>

fun homeAssistantApiClient(credentials: Credentials): HomeAssistantApiClient = HomeAssistantApiClientImpl(credentials)

class HomeAssistantApiClientImpl(
    private val credentials: Credentials
) : HomeAssistantApiClient {

    private val logger = Kermit()
    val mapper: ObjectMapper = ObjectMapper()
    private val hassClient: HassClient = HassClient(
        credentials,
        objectMapper = mapper
    )
    private val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(
                SerializationJson {
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }

        defaultRequest {
            host = credentials.host
            port = credentials.port
            header("Authorization", "Bearer ${credentials.accessToken}")
            header("Content-Type", "application/json")
        }
    }
    private var hassApi: HassApiClient? = null

    private val sensorsByApiName: SensorsByApiName = mutableMapOf()
    private val actuatorsByApiName: ActuatorsByApiName = mutableMapOf()
    private val actuatorsByEntity: ActuatorsByEntity = mutableMapOf()
    private val hassApiCommandHistory: HassApiCommandHistory = mutableMapOf()

    private val eventSubscriptionsByEventType: EventHandlerByEventType = mutableMapOf()

    override var observerExceptionHandler: (Throwable) -> Unit = { exception ->
        logger.e(exception) { "Caught exception in observer" }
    }

    var eventHandlerExceptionHandlerFunction: (Throwable) -> Unit = { exception ->
        logger.e(exception) { "Caught exception in event handler" }
    }

    var errorResponseHandlerFunction: (ErrorResponseData) -> Unit = { errorResponseData ->
        logger.e { "CommandId: ${errorResponseData.commandId} - errorCode: ${errorResponseData.errorResponse.code} | message: ${errorResponseData.errorResponse.message}" }
    }

    override fun <S : State<*>, A : Attributes> Sensor(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>
    ): Sensor<S, A> =
        Sensor<S, A>(this, mapper, stateType, attributesType)
            .also { registerSensor(id, it) }

    override fun <S : State<*>, A : Attributes> Actuator(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>,
        serviceCommandResolver: ServiceCommandResolver<S>
    ): Actuator<S, A> =
        Actuator<S, A>(
            this,
            mapper,
            serviceCommandResolver,
            stateType,
            attributesType
        ).also { registerActuator(id, it) }

    @Suppress("UNCHECKED_CAST")
    override fun <ED> attachEventHandler(
        eventType: EventType,
        eventDataType: KClass<*>,
        eventHandler: EventHandlerFunction<ED>
    ): Switchable =
        eventSubscriptionsByEventType[eventType]?.attachEventHandler(
            eventHandler as EventHandlerFunction<Any?>
        )
            ?: registerEventSubscription<ED>(eventType, eventDataType).attachEventHandler(eventHandler)

    override fun setEventHandlerExceptionHandler(f: (Throwable) -> Unit) {
        eventHandlerExceptionHandlerFunction = f
    }

    override suspend fun emitEvent(eventType: String, eventData: Any?) {
        // TODO: Reconnect if API is null
        hassApi!!.emitEvent(eventType, eventData)
    }

    override fun setErrorResponseHandler(errorResponseHandler: (ErrorResponseData) -> Unit) {
        errorResponseHandlerFunction = errorResponseHandler
    }

    override suspend fun <PB : Any> callService(
        domain: Domain,
        service: Service,
        parameterBag: PB,
        parameterBagType: KType
    ) {
        ServiceCommand(
            domain = domain,
            service = service,
            serviceData = parameterBag
        ).also { hassApi?.sendCommand(it, parameterBagType) } // TODO: Reconnect if no session available
    }

    private fun registerSensor(entityId: EntityId, sensor: Sensor<*, *>) {
        check(!sensorsByApiName.containsKey(entityId)) { "Sensor with id: $entityId already exists." }
        sensorsByApiName[entityId] = sensor
        logger.i { "Registered Sensor with id: $entityId" }
    }

    private fun registerActuator(entityId: EntityId, actuator: Actuator<*, *>) {
        check(!actuatorsByApiName.containsKey(entityId)) { "Actuator with id: $entityId already exists." }
        actuatorsByApiName[entityId] = actuator
        actuatorsByEntity[actuator] = entityId
        logger.i { "Registered Actuator with id: $entityId" }
    }

    private fun <ED> registerEventSubscription(eventType: EventType, eventDataType: KClass<*>) =
        EventSubscription<ED>(this, mapper, eventDataType).also { eventSubscriptionsByEventType[eventType] = it }

    internal suspend fun <S : State<*>, SA : Attributes> enqueueStateChange(
        actuator: Actuator<S, SA>,
        command: ServiceCommand<CommandDataWithEntityId>
    ) {
        val entityId = actuatorsByEntity[actuator] ?: throw RuntimeException("Entity not registered: $actuator")
        command.apply {
            if (domain == null) domain = entityId.domain
            serviceData?.entityId = entityId
        }
        hassApiCommandHistory[entityId] = command
        // TODO: Reconnect if API is null
        hassApi?.sendCommand(command)
    }

    fun launch() =
        MainScope().launch {
            hassClient.startSession {
                val hassApi = HassApiClientImpl(this, mapper, httpClient)
                this@HomeAssistantApiClientImpl.hassApi = hassApi
                val serviceStore = ServiceStore()
                val authenticator = Authenticator(this, credentials)
                val serviceStoreInitializer = ServiceStoreInitializer(this, serviceStore)
                val hassEventSubscriber = HassEventSubscriber(
                    this,
                    eventSubscriptionsByEventType,
                    hassApi
                )

                val entityStateInitializer = EntityStateInitializer(
                    this,
                    SensorStateUpdater(sensorsByApiName),
                    ActuatorStateUpdater(actuatorsByApiName),
                    EntityRegistrationValidation(actuatorsByApiName, sensorsByApiName)
                )

                val stateChangeEventSubscriber = StateChangeEventSubscriber(this)
                val eventResponseConsumer = EventResponseConsumer(
                    this,
                    mapper,
                    SensorStateUpdater(sensorsByApiName),
                    ActuatorStateUpdater(actuatorsByApiName),
                    eventSubscriptionsByEventType,
                    errorResponseHandlerFunction
                )

                authenticator.authenticate()
                serviceStoreInitializer.initialize()
                hassEventSubscriber.subscribe()
                entityStateInitializer.initialize()
                stateChangeEventSubscriber.subscribe()
                eventResponseConsumer.consumeBlocking()
            }
        }
}

internal class MainScope : CoroutineScope {
    private val dispatcher = MainDispatcherFactory.create()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = dispatcher + job
}
