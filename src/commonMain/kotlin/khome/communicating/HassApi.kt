package khome.communicating

import co.touchlab.kermit.Kermit
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import khome.WebSocketSession
import khome.communicating.CommandType.SUBSCRIBE_EVENTS
import khome.core.mapping.ObjectMapper
import khome.values.EntityId
import khome.values.EventType
import kotlinx.atomicfu.atomic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KType
import kotlin.reflect.typeOf

internal val CALLER_ID = atomic(0)

interface CommandDataWithEntityId {
    var entityId: EntityId
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

internal class HassApiClientImpl(
    private val session: WebSocketSession,
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient
) : HassApiClient {
    private val logger = Kermit()

    override suspend fun <SD> sendCommand(command: HassApiCommand<SD>, parameterType: KType) {
        command.id = CALLER_ID.getAndIncrement() // has to be called within single thread to prevent race conditions
        objectMapper.toJsonWithParameter(command, parameterType).let { serializedCommand ->
            session.callWebSocketApi(serializedCommand)
                .also { logger.i { "Called hass api with message: $serializedCommand" } }
        }
    }

    override suspend fun sendCommand2(command: ServiceCommand2) {
        command.id = CALLER_ID.getAndIncrement() // has to be called within single thread to prevent race conditions
        objectMapper.toJson(command).let { serializedCommand ->
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
    // Tell a Home Assistant service to perform a command.
    suspend fun <SD> sendCommand(command: HassApiCommand<SD>, parameterType: KType)

    suspend fun sendCommand2(command: ServiceCommand2)

    // Emit an event, such as a sensor change event.
    suspend fun emitEvent(eventType: String, eventData: Any?)
}

@OptIn(ExperimentalStdlibApi::class)
internal suspend inline fun <reified SD : Any> HassApiClient.sendCommand(command: HassApiCommand<SD>) =
    sendCommand(command, typeOf<SD>())
