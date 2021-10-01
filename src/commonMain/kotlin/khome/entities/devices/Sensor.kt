package khome.entities.devices

import khome.HassConnection
import khome.core.mapping.ObjectMapper
import khome.core.observing.CircularArray
import khome.entities.Attributes
import khome.entities.State
import khome.errorHandling.ObserverExceptionHandler
import khome.observability.*
import khome.observability.ObservableDelegateNoInitial
import khome.observability.Observer
import khome.observability.ObserverImpl
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass

/**
 * An Sensor holding entity state and attributes
 *
 * In Hassemble, the Sensor is the immutable representation of an entity in home assistant.
 *
 * @param S the type of the state object that represents all state values of the entity. Has to implement the [State] interface.
 * @param A the type of the attributes object that represents all attribute values of the entity. Has to implement the [Attributes] interface.
 */
class Sensor<S : State<*>, A : Attributes>(
    private val connection: HassConnection,
    private val mapper: ObjectMapper,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Observable<Sensor<S, A>>, WithHistory<StateAndAttributes<S, A>>, WithAttributes<A> {
    private val observers: MutableList<Observer<Sensor<S, A>>> = mutableListOf()

    /**
     * Represents the current attributes of the entity in Khome.
     */
    override lateinit var attributes: A
    private val _history = CircularArray<StateAndAttributes<S, A>>(10)

    /**
     * Represents the current state object of the entity in Khome.
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
