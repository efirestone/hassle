package khome.core

import kotlinx.serialization.Serializable

/**
 * A data class representing home assistant's error response details
 *
 * @property code error code returning from home assistant
 * @property message error message returning from home assistant
 */
@Serializable
data class ErrorResponse(val code: String, val message: String)
