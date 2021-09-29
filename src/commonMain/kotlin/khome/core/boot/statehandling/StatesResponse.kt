package khome.core.boot.statehandling

import khome.core.MessageInterface
import kotlinx.serialization.json.JsonObject

class StatesResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val result: Array<JsonObject>
) : MessageInterface
