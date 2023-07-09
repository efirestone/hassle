package com.codellyrandom.hassle.observability

import com.codellyrandom.hassle.core.observing.CircularArray
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State

internal class StateAndAttributesWithHistory<S : State<*>, A : Attributes> {
    val history: CircularArray<StateAndAttributes<S, A>> = CircularArray(10)

    lateinit var attributes: A

    var state: S
        get() = history.lastOrNull()?.state ?: throw IllegalStateException("No state available yet")
        set(newState) {
            history.add(StateAndAttributes(newState, attributes))
        }
}

class StateAndAttributes<S, A>(
    val state: S,
    val attributes: A,
)
