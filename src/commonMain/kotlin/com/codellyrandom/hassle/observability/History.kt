package com.codellyrandom.hassle.observability

import com.codellyrandom.hassle.core.observing.CircularArray
import com.codellyrandom.hassle.entities.State

internal class History<S : State<*>> {
    val history: CircularArray<S> = CircularArray(10)

    var state: S
        get() = history.lastOrNull() ?: throw IllegalStateException("No state available yet")
        set(newState) {
            history.add(newState)
        }
}
