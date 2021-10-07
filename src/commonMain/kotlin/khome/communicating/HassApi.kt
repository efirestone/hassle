package khome.communicating

import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import khome.HassSession
import khome.communicating.CommandType.CALL_SERVICE
import khome.communicating.CommandType.SUBSCRIBE_EVENTS
import khome.core.mapping.ObjectMapper
import khome.values.Domain
import khome.values.EntityId
import khome.values.EventType
import khome.values.Service
import kotlinx.atomicfu.atomic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal val CALLER_ID = atomic(0)

@Serializable
internal enum class CommandType {
    @SerialName("call_service")
    CALL_SERVICE,

    @SerialName("subscribe_events")
    SUBSCRIBE_EVENTS,

    @SerialName("get_services")
    GET_SERVICES,

    @SerialName("get_states")
    GET_STATES
}

interface CommandDataWithEntityId {
    var entityId: EntityId
}

internal interface HassApiCommand<SD> {
    val type: CommandType
    var id: Int?
}

@Serializable
internal class SubscribeEventCommand(private val eventType: EventType) : HassApiCommand<Unit> {
    override val type: CommandType = SUBSCRIBE_EVENTS
    override var id: Int? = null
}

abstract class DesiredServiceData : CommandDataWithEntityId {
    @SerialName("entity_id")
    override lateinit var entityId: EntityId
}

@Serializable
class EntityIdOnlyServiceData : DesiredServiceData()

@Serializable
internal data class ServiceCommand<SD>(
    var domain: Domain? = null,
    val service: Service,
    override var id: Int? = null,
    @SerialName("service_data")
    val serviceData: SD? = null,
    override val type: CommandType = CALL_SERVICE
) : HassApiCommand<SD>

internal class HassApiClientImpl(
    private val session: HassSession,
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient
) : HassApiClient {
    private val logger = Kermit()

    override suspend fun <SD> sendCommand(command: HassApiCommand<SD>, parameterType: KType) {
        command.id = CALLER_ID.getAndIncrement() // has to be called within single thread to prevent race conditions
        objectMapper.toJson(command, parameterType).let { serializedCommand ->
            session.callWebSocketApi(serializedCommand)
                .also { logger.i { "Called hass api with message: $serializedCommand" } }
        }
    }

    override suspend fun emitEvent(eventType: String, eventData: Any?) {
        httpClient.post<HttpResponse> {
            url { encodedPath = "/api/events/$eventType" }
            body = eventData ?: EmptyContent
        }
    }
}

internal interface HassApiClient {
    suspend fun <SD> sendCommand(command: HassApiCommand<SD>, parameterType: KType)
    suspend fun emitEvent(eventType: String, eventData: Any?)
}

@OptIn(ExperimentalStdlibApi::class)
internal suspend inline fun <reified SD : Any> HassApiClient.sendCommand(command: HassApiCommand<SD>) =
    sendCommand(command, typeOf<SD>())
