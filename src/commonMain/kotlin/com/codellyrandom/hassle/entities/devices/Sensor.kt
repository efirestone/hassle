package com.codellyrandom.hassle.entities.devices

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.core.observing.CircularArray
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.errorHandling.ObserverExceptionHandler
import com.codellyrandom.hassle.observability.*
import com.codellyrandom.hassle.observability.ObservableDelegateNoInitial
import com.codellyrandom.hassle.observability.Observer
import com.codellyrandom.hassle.observability.ObserverImpl
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass

/**
 * A Sensor holding entity state and attributes
 *
 * In Hassle, the Sensor is the immutable representation of an entity in home assistant.
 *
 * @param S the type of the state object that represents all state values of the entity. Has to implement the [State] interface.
 * @param A the type of the attributes object that represents all attribute values of the entity. Has to implement the [Attributes] interface.
 */
class Sensor<S : State<*>, A : Attributes>(
    private val connection: HomeAssistantApiClient,
    private val mapper: ObjectMapper,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Observable<Sensor<S, A>>, WithHistory<StateAndAttributes<S, A>>, WithAttributes<A> {
    private val observers: MutableList<Observer<Sensor<S, A>>> = mutableListOf()

    /**
     * Represents the current attributes of the sensor entity.
     */
    override lateinit var attributes: A
    private val _history = CircularArray<StateAndAttributes<S, A>>(10)

    /**
     * Represents the current state of the sensor entity.
     */
    var measurement: S by ObservableDelegateNoInitial(this, observers, _history)

    override val history: List<StateAndAttributes<S, A>>
        get() = _history.toList()

    /**
     * Number of observers attached to the sensor.
     */
    val observerCount: Int
        get() = observers.size

    override fun attachObserver(observer: ObserverFunction<Sensor<S, A>>): Switchable =
        ObserverImpl(
            observer,
            ObserverExceptionHandler(connection.observerExceptionHandler)
        ).also { observers.add(it) }

    fun trySetActualStateFromAny(newState: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        measurement = mapper.fromJson(newState, stateType) as S
        checkNotNull(measurement.value) { "State value shall not be null. Please check your State definition " }
    }

    fun trySetAttributesFromAny(newAttributes: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        attributes = mapper.fromJson(newAttributes, attributesType) as A
    }
}
