package com.codellyrandom.hassle.entities.devices

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.errorHandling.ObserverExceptionHandler
import com.codellyrandom.hassle.observability.*
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
    private val stateType: KClass<S>,
    private val attributesType: KClass<A>
) : Observable<Sensor<S, A>> {
    private val observers: MutableList<Observer<Sensor<S, A>>> = mutableListOf()
    private val stateAndAttributesWithHistory = StateAndAttributesWithHistory<S, A>()
    private var dirty = false

    /**
     * Represents the current attributes of the sensor entity.
     */
    var attributes: A
        get() { return stateAndAttributesWithHistory.attributes }
        set(newValue) { stateAndAttributesWithHistory.attributes = newValue }

    /**
     * Represents the current state of the sensor entity.
     */
    var measurement: S
        get() = stateAndAttributesWithHistory.state
        set(value) {
            stateAndAttributesWithHistory.state = value
            if (dirty) observers.forEach { it.update(this) }
            dirty = true
        }

    val history: List<StateAndAttributes<S, A>>
        get() = stateAndAttributesWithHistory.history.toList()

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
        measurement = mapper.fromJson(newState, stateType)
        checkNotNull(measurement.value) { "State value shall not be null. Please check your State definition " }
    }

    fun trySetAttributesFromAny(newAttributes: JsonObject) {
        attributes = mapper.fromJson(newAttributes, attributesType)
    }
}
