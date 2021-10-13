package khome.entities.devices

import khome.HomeAssistantApiClientImpl
import khome.communicating.ServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.core.mapping.ObjectMapper
import khome.core.observing.CircularArray
import khome.entities.Attributes
import khome.entities.State
import khome.errorHandling.ObserverExceptionHandler
import khome.observability.*
import khome.values.EntityId
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass

/**
 * An Actuator holding entity state and attributes
 *
 * In Khome, the Actuator is the mutable representation of an entity in home assistant.
 *
 * @param S the type of the state object that represents all mutable state values of the entity. Has to implement the [State] interface.
 * @param A the type of the attributes object that represents all immutable attribute values of the entity. Has to implement the [Attributes] interface.
 */
class Actuator<S : State<*>, A : Attributes>(
    val entityId: EntityId,
    private val connection: HomeAssistantApiClientImpl,
    private val mapper: ObjectMapper,
    private val resolver: ServiceCommandResolver<S>,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Observable<Actuator<S, A>>, WithHistory<StateAndAttributes<S, A>>, WithAttributes<A> {
    private val observers: MutableList<Observer<Actuator<S, A>>> = mutableListOf()

    /**
     * Represents the current attributes of the entity in Khome
     * Holds all state attributes that can not directly be mutated.
     */
    override lateinit var attributes: A

    private val _history = CircularArray<StateAndAttributes<S, A>>(10)

    /**
     * Represents the current state object of the entity in Khome.
     * Holds all state values that can be mutated directly.
     */
    var actualState: S by ObservableDelegateNoInitial(this, observers, _history)

    override val history: List<StateAndAttributes<S, A>>
        get() = _history.toList()

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
        @Suppress("UNCHECKED_CAST")
        actualState = mapper.fromJson(newState, stateType) as S
        checkNotNull(actualState.value) { "State value shall not be null. Please check your State definition  " }
    }

    fun trySetAttributesFromAny(newAttributes: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        attributes = mapper.fromJson(newAttributes, attributesType) as A
    }

    /**
     * Sends a service command over the Websocket API to home assistant
     *
     * @param command the command to send
     */
    suspend fun send(command: ServiceCommand) = connection.send(command)

    override fun attachObserver(observer: ObserverFunction<Actuator<S, A>>): Switchable =
        ObserverImpl(
            observer,
            ObserverExceptionHandler(connection.observerExceptionHandler)
        ).also { observers.add(it) }
}
