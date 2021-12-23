package com.codellyrandom.hassle

import co.touchlab.kermit.Logger
import co.touchlab.kermit.LoggerConfig
import com.codellyrandom.hassle.communicating.Command
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.core.Credentials
import com.codellyrandom.hassle.core.boot.EventResponseConsumer
import com.codellyrandom.hassle.core.boot.StateChangeEventSubscriber
import com.codellyrandom.hassle.core.boot.authentication.Authenticator
import com.codellyrandom.hassle.core.boot.servicestore.ServiceStoreImpl
import com.codellyrandom.hassle.core.boot.servicestore.ServiceStoreInitializer
import com.codellyrandom.hassle.core.boot.statehandling.EntityStateInitializer
import com.codellyrandom.hassle.core.boot.subscribing.HassEventSubscriber
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.entities.*
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.errorHandling.ErrorResponseData
import com.codellyrandom.hassle.events.EventHandlerFunction
import com.codellyrandom.hassle.events.EventSubscription
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.EventType
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlin.collections.set
import kotlin.reflect.KClass
import kotlinx.serialization.json.Json as SerializationJson

internal typealias SensorsByApiName = MutableMap<EntityId, Sensor<*, *>>
internal typealias ActuatorsByApiName = MutableMap<EntityId, Actuator<*, *>>
internal typealias ActuatorsByEntity = MutableMap<Actuator<*, *>, EntityId>
internal typealias EventHandlerByEventType = MutableMap<EventType, EventSubscription<*>>
internal typealias HassApiCommandHistory = MutableMap<EntityId, Command>

fun homeAssistantApiClient(credentials: Credentials, coroutineScope: CoroutineScope): HomeAssistantApiClient =
    HomeAssistantApiClientImpl(credentials, coroutineScope)

internal class HomeAssistantApiClientImpl(
    private val credentials: Credentials,
    coroutineScope: CoroutineScope,
) : HomeAssistantApiClient {

    private val logger = Logger(config = LoggerConfig.default)
    val mapper: ObjectMapper = ObjectMapper()
    private val connection: Connection = Connection(
        credentials,
        coroutineScope,
        mapper,
        { connectionExceptionHandler(it) }
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

    private val commandId = atomic(0)
    private var session: WebSocketSession? = null

    private val sensorsByApiName: SensorsByApiName = mutableMapOf()
    private val actuatorsByApiName: ActuatorsByApiName = mutableMapOf()
    private val actuatorsByEntity: ActuatorsByEntity = mutableMapOf()
    private val hassApiCommandHistory: HassApiCommandHistory = mutableMapOf()

    private val eventSubscriptionsByEventType: EventHandlerByEventType = mutableMapOf()

    override var connectionExceptionHandler: (Throwable) -> Unit = { exception ->
        logger.e(exception) { "Caught exception while connecting" }
    }

    override var observerExceptionHandler: (Throwable) -> Unit = { exception ->
        logger.e(exception) { "Caught exception in observer" }
    }

    var eventHandlerExceptionHandlerFunction: (Throwable) -> Unit = { exception ->
        logger.e(exception) { "Caught exception in event handler" }
    }

    var errorResponseHandlerFunction: (ErrorResponseData) -> Unit = { errorResponseData ->
        logger.e { "CommandId: ${errorResponseData.commandId} - errorCode: ${errorResponseData.errorResponse.code} | message: ${errorResponseData.errorResponse.message}" }
    }

    /**
     * [Sensor] factory function
     *
     * Creates a fresh instance of a sensor entity.
     *
     * @param S the type of the state that represents all state values of the entity. Has to implement the [State] interface.
     * @param A the type of the attributes that represents all attribute values of the entity. Has to implement the [Attributes] interface.
     * @param id the corresponding [EntityId] for the sensor.
     * @param stateType the type param [S] as [KClass].
     * @param attributesType the type param [A] as [KClass].
     *
     * @return [Sensor]
     */
    @Suppress("FunctionName")
    fun <S : State<*>, A : Attributes> Sensor(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>
    ): Sensor<S, A> =
        Sensor<S, A>(this, mapper, stateType, attributesType)
            .also { registerSensor(id, it) }

    /**
     * [Actuator] factory function
     *
     * Creates a fresh instance of a actuator entity.
     *
     * @param S the type of the state that represents all state values of the entity. Has to implement the [State] interface.
     * @param A the type of the attributes that represents all attribute values of the entity. Has to implement the [Attributes] interface.
     * @param id the corresponding [EntityId] for the sensor.
     * @param stateType the type param [S] as [KClass].
     * @param attributesType the type param [A] as [KClass].
     * @param serviceCommandResolver the serviceCommandResolver instance. @See [ServiceCommandResolver] for more.
     *
     * @return [Actuator]
     */
    @Suppress("FunctionName")
    fun <S : State<*>, A : Attributes> Actuator(
        id: EntityId,
        stateType: KClass<*>,
        attributesType: KClass<*>,
        serviceCommandResolver: ServiceCommandResolver<S>
    ): Actuator<S, A> =
        Actuator<S, A>(
            id,
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
        httpClient.post<HttpResponse> {
            url { encodedPath = "/api/events/$eventType" }
            body = eventData ?: EmptyContent
        }
    }

    override fun setErrorResponseHandler(errorResponseHandler: (ErrorResponseData) -> Unit) {
        errorResponseHandlerFunction = errorResponseHandler
    }

    /**
     * Tell a Home Assistant service to perform a command.
     *
     * @param command the command to send
     * @return The ID of the command that was sent.
     */
    internal suspend fun send(command: Command): Int {
        val id = commandId.incrementAndGet()
        val commandWithId = command.copy(id) // has to be called within single thread to prevent race conditions
        mapper.toJson(commandWithId).let { serializedCommand ->
            // TODO: Reconnect if session is missing
            session!!.callWebSocketApi(serializedCommand)
                .also { logger.i { "Called hass api with message: $serializedCommand" } }
        }
        return id
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

    override fun connect() =
        connection.connect c@{
            if (this@HomeAssistantApiClientImpl.session != null) {
                // We already have a session, or are connecting
                return@c
            }

            this@HomeAssistantApiClientImpl.session = this
            val serviceStore = ServiceStoreImpl()
            val authenticator = Authenticator(this, credentials)
            val serviceStoreInitializer = ServiceStoreInitializer(
                this@HomeAssistantApiClientImpl,
                this,
                serviceStore
            )
            val hassEventSubscriber = HassEventSubscriber(
                this,
                eventSubscriptionsByEventType,
                this@HomeAssistantApiClientImpl
            )

            val entityStateInitializer = EntityStateInitializer(
                this@HomeAssistantApiClientImpl,
                this,
                SensorStateUpdater(sensorsByApiName),
                ActuatorStateUpdater(actuatorsByApiName),
                EntityRegistrationValidation(actuatorsByApiName, sensorsByApiName)
            )

            val stateChangeEventSubscriber = StateChangeEventSubscriber(
                this@HomeAssistantApiClientImpl,
                this
            )
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
