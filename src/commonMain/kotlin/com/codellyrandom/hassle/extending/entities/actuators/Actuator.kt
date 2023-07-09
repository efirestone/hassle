@file:Suppress("unused")

package com.codellyrandom.hassle.extending.entities.actuators

import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.extending.entities.SwitchableSettableState
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.observability.Switchable

fun <S : State<*>, SV> Actuator<S, *>.stateValueChangedFrom(values: Pair<SV, SV>) =
    history[1].value == values.first && state.value == values.second

fun <S : State<*>, SV> Actuator<S, *>.stateValueChangedFrom(values: Triple<SV, SV, SV>) =
    history[2].value == values.first && history[1].value == values.second && state.value == values.third

val <S : State<SwitchableValue>> Actuator<S, *>.isOn
    get() = state.value == SwitchableValue.ON

val <S : State<SwitchableValue>> Actuator<S, *>.isOff
    get() = state.value == SwitchableValue.OFF

suspend fun Actuator<*, SwitchableSettableState>.turnOn() {
    setDesiredState(SwitchableSettableState(SwitchableValue.ON))
}

suspend fun Actuator<*, SwitchableSettableState>.turnOff() {
    setDesiredState(SwitchableSettableState(SwitchableValue.OFF))
}

inline fun <S : State<*>, Settable : Any, SV> Actuator<S, Settable>.onStateValueChangedFrom(
    values: Pair<SV, SV>,
    crossinline f: Actuator<S, Settable>.(Switchable) -> Unit,
) = attachObserver {
    if (stateValueChangedFrom(values)) {
        f(this, it)
    }
}

inline fun <S : State<*>, Settable : Any, SV> Actuator<S, Settable>.onStateValueChangedFrom(
    values: Triple<SV, SV, SV>,
    crossinline f: Actuator<S, Settable>.(Switchable) -> Unit,
) = attachObserver {
    if (stateValueChangedFrom(values)) {
        f(this, it)
    }
}

inline fun <S : State<SwitchableValue>> Actuator<S, *>.onTurnedOn(
    crossinline f: Actuator<S, *>.(Switchable) -> Unit,
) = onStateValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

inline fun <S : State<SwitchableValue>> Actuator<S, *>.onTurnedOff(
    crossinline f: Actuator<S, *>.(Switchable) -> Unit,
) = onStateValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
