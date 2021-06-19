package khome

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
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
import khome.core.mapping.ObjectMapper
import khome.core.mapping.ObjectMapperInterface
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.modules.SerializersModule
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.dsl.module

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
}

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

    override fun configure(builder: Configuration.() -> Unit) =
        config.apply(builder)

    @OptIn(KtorExperimentalAPI::class)
    fun createApplication(): KhomeApplicationImpl {
        val mapperModule = module {
            single {
                Json {
                    isLenient = true
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        this.contextual(JsonObject::class, JsonObject.serializer())
                    }
                }
            }
            single<ObjectMapperInterface> { ObjectMapper(get()) }
        }

        val internalModule: Module =
            module {
                single<ServiceStoreInterface> { ServiceStore() }

                single {
                    val client = HttpClient(CIO) {
                        install(JsonFeature) {
                            serializer = KotlinxSerializer(
                                Json {
                                    isLenient = true
                                    ignoreUnknownKeys = true
                                }
                            )
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
