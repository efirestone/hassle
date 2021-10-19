package hassemble.core.boot.statehandling

import kotlinx.serialization.Serializable

@Serializable
internal data class StatesRequest(
    val id: Int,
    val type: String = "get_states"
)
