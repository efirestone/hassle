package khome.communicating

import khome.values.Domain
import khome.values.EntityId
import khome.values.MediaContentId
import khome.values.Service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ServiceCommand2 {
    var id: Int? = null
}

@SerialName("call_service")
@Serializable
sealed class CallServiceCommand(
    var domain: Domain,
    val service: Service
) : ServiceCommand2() {
    @Serializable
    class Target(
        @SerialName("entity_id")
        val entityId: EntityId
    )
}

@SerialName("call_service")
@Serializable
class PlayMediaServiceCommand(
    @SerialName("service_data")
    val serviceData: ServiceData,
    val target: Target
) : CallServiceCommand(
    Domain("media_player"),
    Service("play_media")
) {
    @Serializable
    class ServiceData(
        @SerialName("media_content_id")
        val mediaContentId: MediaContentId
    )

    constructor(mediaContentId: MediaContentId, target: EntityId) : this(
        ServiceData(mediaContentId),
        Target(target)
    )
}
