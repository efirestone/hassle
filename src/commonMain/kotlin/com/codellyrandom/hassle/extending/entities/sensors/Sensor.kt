package com.codellyrandom.hassle.extending.entities.sensors

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.extending.entities.Sensor
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.observability.Switchable
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.ObjectId
import com.codellyrandom.hassle.values.domain

internal inline fun <reified S : State<*>> HomeAssistantApiClient.Sensor(objectId: ObjectId): Sensor<S> =
    Sensor(EntityId.fromPair("sensor".domain to objectId))

fun <S : State<*>, SV> Sensor<S>.measurementValueChangedFrom(values: Pair<SV, SV>) =
    history[1].value == values.first && state.value == values.second

fun <S : State<*>, SV> Sensor<S>.measurementValueChangedFrom(values: Triple<SV, SV, SV>) =
    history[2].value == values.first && history[1].value == values.second && state.value == values.third

val <S : State<SwitchableValue>> Sensor<S>.isOn
    get() = state.value == SwitchableValue.ON

val <S : State<SwitchableValue>> Sensor<S>.isOff
    get() = state.value == SwitchableValue.OFF

inline fun <S : State<*>, SV> Sensor<S>.onMeasurementValueChangedFrom(
    values: Pair<SV, SV>,
    crossinline f: Sensor<S>.(Switchable) -> Unit,
) = attachObserver {
    if (measurementValueChangedFrom(values)) {
        f(this, it)
    }
}

inline fun <S : State<*>, SV> Sensor<S>.onMeasurementValueChangedFrom(
    values: Triple<SV, SV, SV>,
    crossinline f: Sensor<S>.(Switchable) -> Unit,
) = attachObserver {
    if (measurementValueChangedFrom(values)) {
        f(this, it)
    }
}

inline fun <S : State<SwitchableValue>> Sensor<S>.onTurnedOn(
    crossinline f: Sensor<S>.(Switchable) -> Unit,
) = onMeasurementValueChangedFrom(SwitchableValue.OFF to SwitchableValue.ON, f)

inline fun <S : State<SwitchableValue>> Sensor<S>.onTurnedOff(
    crossinline f: Sensor<S>.(Switchable) -> Unit,
) = onMeasurementValueChangedFrom(SwitchableValue.ON to SwitchableValue.OFF, f)
