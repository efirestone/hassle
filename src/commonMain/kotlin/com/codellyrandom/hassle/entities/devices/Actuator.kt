package com.codellyrandom.hassle.entities.devices

import com.codellyrandom.hassle.HomeAssistantApiClientImpl
import com.codellyrandom.hassle.communicating.ServiceCommand
import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.entities.Attributes
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
 * @param A the type of the attributes object that represents all immutable attribute values of the entity.
 * Has to implement the [Attributes] interface.
 */
class Actuator<S : State<*>, A : Attributes> internal constructor(
    val entityId: EntityId,
    private val connection: HomeAssistantApiClientImpl,
    private val mapper: ObjectMapper,
    private val resolver: ServiceCommandResolver<S>,
    private val stateType: KClass<S>,
    private val attributesType: KClass<A>
) : Observable<Actuator<S, A>> {
    private val observers: MutableList<Observer<Actuator<S, A>>> = mutableListOf()
    private val stateAndAttributesWithHistory = StateAndAttributesWithHistory<S, A>()
    private var dirty = false

    /**
     * Represents the current attributes of the entity.
     * Holds all state attributes that can not directly be mutated.
     */
    var attributes: A
        get() { return stateAndAttributesWithHistory.attributes }
        set(newValue) { stateAndAttributesWithHistory.attributes = newValue }

    /**
     * Represents the current state of the entity.
     * Holds all state values that can be mutated directly.
     */
    var actualState: S
        get() = stateAndAttributesWithHistory.state
        set(value) {
            stateAndAttributesWithHistory.state = value
            if (dirty) observers.forEach { it.update(this) }
            dirty = true
        }

    val history: List<StateAndAttributes<S, A>>
        get() = stateAndAttributesWithHistory.history.toList()

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
    suspend fun setDesiredState(state: S) {
        val command = resolver.resolve(entityId, state)
        connection.send(command)
    }

    fun trySetActualStateFromAny(newState: JsonObject) {
        actualState = mapper.fromJson(newState, stateType)
        checkNotNull(actualState.value) { "State value shall not be null. Please check your State definition  " }
    }

    fun trySetAttributesFromAny(newAttributes: JsonObject) {
        attributes = mapper.fromJson(newAttributes, attributesType)
    }

    /**
     * Sends a service command over the Websocket API to home assistant
     *
     * @param command the command to send
     */
    internal suspend fun send(command: ServiceCommand) = connection.send(command)

    override fun attachObserver(observer: ObserverFunction<Actuator<S, A>>): Switchable =
        ObserverImpl(
            observer,
            ObserverExceptionHandler(connection.observerExceptionHandler)
        ).also { observers.add(it) }
}
