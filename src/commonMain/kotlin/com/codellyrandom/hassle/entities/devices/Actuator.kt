package com.codellyrandom.hassle.entities.devices

import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.communicating.ServiceCommand
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.errorHandling.ObserverExceptionHandler
import com.codellyrandom.hassle.observability.*
import com.codellyrandom.hassle.values.EntityId
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass

/**
 * An Actuator holding entity state and attributes
 *
 * An Actuator is the mutable representation of an entity in Home Assistant.
 *
 * @param S the type of the state object that represents all mutable state values of the entity.
 * Has to implement the [State] interface.
 */
class Actuator<S : State<*>, Settable : Any> internal constructor(
    val entityId: EntityId,
    private val connection: HomeAssistantApiClientImpl,
    private val mapper: ObjectMapper,
    private val resolver: ServiceCommandResolver<Settable>,
    private val stateType: KClass<S>,
) : Observable<Actuator<S, Settable>> {
    private val observers: MutableList<Observer<Actuator<S, Settable>>> = mutableListOf()
    private val stateWithHistory = History<S>()
    private var dirty = false

    /**
     * Represents the current state of the entity.
     * Holds all state values that can be mutated directly.
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
     * Number of observers attached to the actuator.
     */
    val observerCount: Int
        get() = observers.size

    /**
     * Set this property to a desired version of the state to mutate it in home assistant.
     * The setter of this property will intercept the setting, and translates the new (desired) state
     * into a service command that mutates the state in home assistant.
     */
    suspend fun setDesiredState(settableState: Settable) {
        val command = resolver.resolve(entityId, settableState)
        connection.send(command)
    }

    fun trySetStateFromAny(newState: JsonObject) {
        state = mapper.fromJson(newState, stateType)
        checkNotNull(state.value) { "State value shall not be null. Please check your State definition  " }
    }

    /**
     * Sends a service command over the Websocket API to home assistant
     *
     * @param command the command to send
     */
    internal suspend fun send(command: ServiceCommand) = connection.send(command)

    override fun attachObserver(observer: ObserverFunction<Actuator<S, Settable>>): Switchable =
        ObserverImpl(
            observer,
            ObserverExceptionHandler(connection.observerExceptionHandler),
        ).also { observers.add(it) }
}
