package com.codellyrandom.hassle.entities

/**
 * The State interface
 *
 * Defines the minimum structure of a valid state object.
 *
 * @param T the type of the actual state value.
 */
interface State<T> {
    val value: T
}
