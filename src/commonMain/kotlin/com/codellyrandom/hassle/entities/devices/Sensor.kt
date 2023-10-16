package com.codellyrandom.hassle.entities.devices

import com.codellyrandom.hassle.HomeAssistantApiClient
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.errorHandling.ObserverExceptionHandler
import com.codellyrandom.hassle.observability.*
import com.codellyrandom.hassle.values.EntityId
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KType

/**
 * A Sensor holding entity state and attributes
 *
 * In Hassle, the Sensor is the immutable representation of an entity in home assistant.
 *
 * @param S the type of the state object that represents all state values of the entity. Has to implement the [State] interface.
 */
class Sensor<S : State<*>>(
    val entityId: EntityId,
    private val connection: HomeAssistantApiClient,
    private val mapper: ObjectMapper,
    private val stateType: KType,
) : Observable<Sensor<S>> {
    private val observers: MutableList<Observer<Sensor<S>>> = mutableListOf()
    private val stateWithHistory = History<S>()
    private var dirty = false

    /**
     * Represents the current state of the sensor entity.
     */
    var state: S
        get() = stateWithHistory.state
        set(value) {
            stateWithHistory.state = value
            if (dirty) observers.forEach { it.update(this) }
            dirty = true
        }

    val history: List<S>
        get() = stateWithHistory.history.toList()

    /**
     * Number of observers attached to the sensor.
     */
    val observerCount: Int
        get() = observers.size

    override fun attachObserver(observer: ObserverFunction<Sensor<S>>): Switchable =
        ObserverImpl(
            observer,
            ObserverExceptionHandler(connection.observerExceptionHandler),
        ).also { observers.add(it) }

    fun trySetStateFromAny(newState: JsonObject) {
        state = mapper.fromJson(newState, stateType)
        checkNotNull(state.value) { "State value shall not be null. Please check your State definition " }
    }
}
