package hassemble.extending.entities.sensors

import hassemble.HomeAssistantApiClient
import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Sensor
import hassemble.extending.entities.Sensor
import hassemble.extending.entities.SwitchableState
import hassemble.extending.entities.SwitchableValue
import hassemble.observability.Switchable
import hassemble.values.EntityId
import hassemble.values.ObjectId
import hassemble.values.domain

@Suppress("FunctionName")
internal inline fun <reified S : State<*>, reified A : Attributes> HomeAssistantApiClient.Sensor(objectId: ObjectId): Sensor<S, A> =
    Sensor(EntityId.fromPair("sensor".domain to objectId))

fun <S : State<*>, A : Attributes, SV> Sensor<S, A>.measurementValueChangedFrom(values: Pair<SV, SV>) =
    history[1].state.value == values.first && measurement.value == values.second

fun <S : State<*>, A : Attributes, SV> Sensor<S, A>.measurementValueChangedFrom(values: Triple<SV, SV, SV>) =
    history[2].state.value == values.first && history[1].state.value == values.second && measurement.value == values.third

val Sensor<SwitchableState, *>.isOn
    get() = measurement.value == SwitchableValue.ON

val Sensor<SwitchableState, *>.isOff
    get() = measurement.value == SwitchableValue.OFF

inline fun <S : State<*>, A : Attributes, SV> Sensor<S, A>.onMeasurementValueChangedFrom(
    values: Pair<SV, SV>,
    crossinline f: Sensor<S, A>.(Switchable) -> Unit
) = attachObserver {
    if (measurementValueChangedFrom(values))
        f(this, it)
}

inline fun <S : State<*>, A : Attributes, SV> Sensor<S, A>.onMeasurementValueChangedFrom(
    values: Triple<SV, SV, SV>,
    crossinline f: Sensor<S, A>.(Switchable) -> Unit
) = attachObserver {
    if (measurementValueChangedFrom(values))
        f(this, it)
}

inline fun <A : Attributes> Sensor<SwitchableState, A>.onTurnedOn(
    crossinline f: Sensor<SwitchableState, A>.(Switchable) -> Unit
) = onMeasurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

inline fun <A : Attributes> Sensor<SwitchableState, A>.onTurnedOff(
    crossinline f: Sensor<SwitchableState, A>.(Switchable) -> Unit
) = onMeasurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
