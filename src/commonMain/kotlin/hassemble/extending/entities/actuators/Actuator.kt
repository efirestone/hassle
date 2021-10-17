@file:Suppress("unused")

package hassemble.extending.entities.actuators

import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.SwitchableState
import hassemble.extending.entities.SwitchableValue
import hassemble.observability.Switchable

fun <S : State<*>, A : Attributes, SV> Actuator<S, A>.stateValueChangedFrom(values: Pair<SV, SV>) =
    history[1].state.value == values.first && actualState.value == values.second

fun <S : State<*>, A : Attributes, SV> Actuator<S, A>.stateValueChangedFrom(values: Triple<SV, SV, SV>) =
    history[2].state.value == values.first && history[1].state.value == values.second && actualState.value == values.third

val Actuator<SwitchableState, *>.isOn
    get() = actualState.value == SwitchableValue.ON

val Actuator<SwitchableState, *>.isOff
    get() = actualState.value == SwitchableValue.OFF

suspend fun <A : Attributes> Actuator<SwitchableState, A>.turnOn() {
    setDesiredState(SwitchableState(SwitchableValue.ON))
}

suspend fun <A : Attributes> Actuator<SwitchableState, A>.turnOff() {
    setDesiredState(SwitchableState(SwitchableValue.OFF))
}

inline fun <S : State<*>, A : Attributes, SV> Actuator<S, A>.onStateValueChangedFrom(
    values: Pair<SV, SV>,
    crossinline f: Actuator<S, A>.(Switchable) -> Unit
) = attachObserver {
    if (stateValueChangedFrom(values))
        f(this, it)
}

inline fun <S : State<*>, A : Attributes, SV> Actuator<S, A>.onStateValueChangedFrom(
    values: Triple<SV, SV, SV>,
    crossinline f: Actuator<S, A>.(Switchable) -> Unit
) = attachObserver {
    if (stateValueChangedFrom(values))
        f(this, it)
}

inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurnedOn(
    crossinline f: Actuator<SwitchableState, A>.(Switchable) -> Unit
) = onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

inline fun <A : Attributes> Actuator<SwitchableState, A>.onTurnedOff(
    crossinline f: Actuator<SwitchableState, A>.(Switchable) -> Unit
) = onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
