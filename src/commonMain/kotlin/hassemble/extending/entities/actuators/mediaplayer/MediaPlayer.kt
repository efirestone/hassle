package hassemble.extending.entities.actuators.mediaplayer

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.Actuator
import hassemble.values.EntityId
import hassemble.values.ObjectId
import hassemble.values.domain

typealias MediaPlayer<S, A> = Actuator<S, A>

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.MediaPlayer(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>
): MediaPlayer<S, A> = Actuator(EntityId.fromPair("media_player".domain to objectId), serviceCommandResolver)
