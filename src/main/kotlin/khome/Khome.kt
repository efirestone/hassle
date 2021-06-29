package khome

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import io.fluidsonic.time.LocalTime
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.util.*
import khome.core.Configuration
import khome.core.DefaultConfiguration
import khome.core.boot.EventResponseConsumer
import khome.core.boot.EventResponseConsumerImpl
import khome.core.boot.HassApiInitializer
import khome.core.boot.HassApiInitializerImpl
import khome.core.boot.StateChangeEventSubscriber
import khome.core.boot.StateChangeEventSubscriberImpl
import khome.core.boot.authentication.Authenticator
import khome.core.boot.authentication.AuthenticatorImpl
import khome.core.boot.servicestore.ServiceStore
import khome.core.boot.servicestore.ServiceStoreInitializer
import khome.core.boot.servicestore.ServiceStoreInitializerImpl
import khome.core.boot.servicestore.ServiceStoreInterface
import khome.core.boot.statehandling.EntityStateInitializer
import khome.core.boot.statehandling.EntityStateInitializerImpl
import khome.core.boot.subscribing.HassEventSubscriber
import khome.core.boot.subscribing.HassEventSubscriberImpl
import khome.core.clients.RestApiClient
import khome.core.clients.WebSocketClient
import khome.core.koin.KhomeComponent
import khome.core.koin.KhomeKoinContext
import khome.core.mapping.GsonTypeAdapterBridge
import khome.core.mapping.KhomeTypeAdapter
import khome.core.mapping.ObjectMapper
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.adapter.default.InstantTypeAdapter
import khome.core.mapping.adapter.default.LocalDateAdapter
import khome.core.mapping.adapter.default.LocalDateTimeAdapter
import khome.core.mapping.adapter.default.LocalTimeAdapter
import khome.core.mapping.adapter.default.RegexTypeAdapter
import khome.values.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.reflect.KClass

internal typealias TypeAdapters = MutableMap<KClass<*>, TypeAdapter<*>>

private const val NAME = "NAME"
private const val HOST = "HOST"
private const val PORT = "PORT"
private const val ACCESS_TOKEN = "ACCESS_TOKEN"
private const val SECURE = "SECURE"

/**
 * The lambda with [Khome] as receiver to configure Khome
 */
typealias KhomeBuilder = Khome.() -> Unit

/**
 * The main entry point to start your application
 *
 * @param init The type safe builder function to access the receiver
 * @return [KhomeApplication]
 */

fun khomeApplication(init: KhomeBuilder = {}): KhomeApplication =
    KhomeImpl().apply(init).createApplication()

/**
 * The main application interface.
 * Serves all the tools necessary for the application to run.
 *
 * @author Dennis Schröder
 */
interface Khome {
    /**
     * Configure your Khome instance. See all available properties in
     * the [Configuration] data class.
     *
     * @param builder Lambda with [Configuration] receiver to configure Khome.
     */
    fun configure(builder: Configuration.() -> Unit): Configuration

    fun <T : Any, P : Any> registerTypeAdapter(
        adapter: KhomeTypeAdapter<T>,
        valueObjectType: KClass<T>,
        primitiveType: KClass<P>
    )
}

inline fun <reified T : Any, reified P : Any> Khome.registerTypeAdapter(adapter: KhomeTypeAdapter<T>) =
    registerTypeAdapter(adapter, T::class, P::class)

private class KhomeImpl : Khome, KhomeComponent {

    init {
        KhomeKoinContext.startKoinApplication()
        val module = module {
            single<Configuration> {
                DefaultConfiguration(
                    name = getProperty(NAME, "Khome"),
                    host = getProperty(HOST, "localhost"),
                    port = getProperty(PORT, "8123").toInt(),
                    accessToken = getProperty(ACCESS_TOKEN, "<some-fancy-access-token>"),
                    secure = getProperty(SECURE, "false").toBoolean()
                )
            }
        }
        KhomeKoinContext.addModule(module)
    }

    private val config: Configuration by inject()
    private val typeAdapters: TypeAdapters = mutableMapOf()

    override fun configure(builder: Configuration.() -> Unit) =
        config.apply(builder)

    override fun <T : Any, P : Any> registerTypeAdapter(
        adapter: KhomeTypeAdapter<T>,
        valueObjectType: KClass<T>,
        primitiveType: KClass<P>
    ) {
        typeAdapters[valueObjectType] = GsonTypeAdapterBridge(adapter, primitiveType)
    }

    @OptIn(KtorExperimentalAPI::class)
    fun createApplication(): KhomeApplicationImpl {
        registerDefaultTypeAdapter()
        val mapperModule = module {
            single {
                GsonBuilder().apply {
                    setPrettyPrinting()
                    setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    typeAdapters.forEach { adapter ->
                        registerTypeAdapter(adapter.key.java, adapter.value.nullSafe())
                    }
                }.create()!!
            }
            single<ObjectMapperInterface> { ObjectMapper(get()) }
        }

        val internalModule: Module =
            module {

                single<ServiceStoreInterface> { ServiceStore() }

                single {
                    val client = HttpClient(CIO) {
                        install(JsonFeature) {
                            serializer = GsonSerializer {
                                setPrettyPrinting()
                                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                typeAdapters.forEach { adapter ->
                                    registerTypeAdapter(adapter.key.java, adapter.value.nullSafe())
                                }
                                create()!!
                            }
                        }

                        val config = get<Configuration>()

                        defaultRequest {
                            host = config.host
                            port = config.port
                            header("Authorization", "Bearer ${config.accessToken}")
                            header("Content-Type", "application/json")
                        }
                    }
                    RestApiClient(client)
                }
                single<HassClient> {
                    HassClientImpl(
                        get(),
                        WebSocketClient(HttpClient(CIO).config { install(WebSockets) }),
                        get()
                    )
                }
                single<Authenticator> { parameters -> AuthenticatorImpl(parameters.get(), get()) }
                single<ServiceStoreInitializer> { parameters ->
                    ServiceStoreInitializerImpl(
                        parameters.get(),
                        get()
                    )
                }
                single<HassApiInitializer> { parameters -> HassApiInitializerImpl(parameters.get()) }
                single<HassEventSubscriber> { parameters ->
                    HassEventSubscriberImpl(
                        parameters.get(),
                        parameters.get(),
                        get()
                    )
                }

                single<EntityStateInitializer> { parameters ->
                    EntityStateInitializerImpl(
                        parameters.get(),
                        parameters.get(),
                        parameters.get(),
                        parameters.get()
                    )
                }

                single<StateChangeEventSubscriber> { parameters ->
                    StateChangeEventSubscriberImpl(
                        parameters.get()
                    )
                }

                single<EventResponseConsumer> { parameters ->
                    EventResponseConsumerImpl(
                        khomeSession = parameters.get(),
                        sensorStateUpdater = parameters.get(),
                        actuatorStateUpdater = parameters.get(),
                        objectMapper = get(),
                        eventHandlerByEventType = parameters.get(),
                        errorResponseHandler = parameters.get()
                    )
                }
            }

        KhomeKoinContext.addModule(mapperModule, internalModule)
        return KhomeApplicationImpl()
    }
}

internal fun Khome.registerDefaultTypeAdapter() {
    registerTypeAdapter<Instant, String>(InstantTypeAdapter())
    registerTypeAdapter<LocalDateTime, String>(LocalDateTimeAdapter())
    registerTypeAdapter<LocalDate, String>(LocalDateAdapter())
    registerTypeAdapter<LocalTime, String>(LocalTimeAdapter())
    registerTypeAdapter<Regex, String>(RegexTypeAdapter())
    registerTypeAdapter<EntityId, String>(EntityId)
    registerTypeAdapter<UserId, String>(UserId)
    registerTypeAdapter<Temperature, Double>(Temperature)
    registerTypeAdapter<Domain, String>(Domain)
    registerTypeAdapter<ObjectId, String>(ObjectId)
    registerTypeAdapter<Service, String>(Service)
    registerTypeAdapter<Device, String>(Device)
    registerTypeAdapter<Brightness, Int>(Brightness)
    registerTypeAdapter<RGBColor, Array<Int>>(RGBColor)
    registerTypeAdapter<HSColor, Array<Double>>(HSColor)
    registerTypeAdapter<XYColor, Array<Double>>(XYColor)
    registerTypeAdapter<ColorTemperature, Int>(ColorTemperature)
    registerTypeAdapter<ColorName, String>(ColorName)
    registerTypeAdapter<PowerConsumption, Double>(PowerConsumption)
    registerTypeAdapter<Icon, String>(Icon)
    registerTypeAdapter<PresetMode, String>(PresetMode)
    registerTypeAdapter<HvacMode, String>(HvacMode)
    registerTypeAdapter<FriendlyName, String>(FriendlyName)
    registerTypeAdapter<Option, String>(Option)
    registerTypeAdapter<Mode, String>(Mode)
    registerTypeAdapter<Min, Double>(Min)
    registerTypeAdapter<Max, Double>(Max)
    registerTypeAdapter<Step, Double>(Step)
    registerTypeAdapter<Initial, Double>(Initial)
    registerTypeAdapter<UnitOfMeasurement, String>(UnitOfMeasurement)
    registerTypeAdapter<PersonId, String>(PersonId)
    registerTypeAdapter<Azimuth, Double>(Azimuth)
    registerTypeAdapter<Elevation, Double>(Elevation)
    registerTypeAdapter<EventType, String>(EventType)
    registerTypeAdapter<MediaContentId, String>(MediaContentId)
    registerTypeAdapter<MediaTitle, String>(MediaTitle)
    registerTypeAdapter<EntityPicture, String>(EntityPicture)
    registerTypeAdapter<Artist, String>(Artist)
    registerTypeAdapter<AlbumName, String>(AlbumName)
    registerTypeAdapter<MediaDuration, Double>(MediaDuration)
    registerTypeAdapter<AppId, String>(AppId)
    registerTypeAdapter<AppName, String>(AppName)
    registerTypeAdapter<VolumeLevel, Double>(VolumeLevel)
    registerTypeAdapter<Mute, Boolean>(Mute)
    registerTypeAdapter<MediaPosition, Double>(MediaPosition)
    registerTypeAdapter<MediaSource, String>(MediaSource)
    registerTypeAdapter<Rising, Boolean>(Rising)
    registerTypeAdapter<Zone, String>(Zone)
    registerTypeAdapter<Position, Int>(Position)
}
