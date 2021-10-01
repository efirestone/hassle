package khome.extending.entities.actuators.mediaplayer

import khome.HassConnection
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.EntityId
import khome.values.ObjectId
import khome.values.domain

typealias MediaPlayer<S, A> = Actuator<S, A>

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> HassConnection.MediaPlayer(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>
): MediaPlayer<S, A> = Actuator(EntityId.fromPair("media_player".domain to objectId), serviceCommandResolver)
