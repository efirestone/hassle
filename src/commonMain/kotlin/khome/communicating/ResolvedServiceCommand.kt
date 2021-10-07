package khome.communicating

import khome.values.Domain
import khome.values.Service

data class ResolvedServiceCommand(
    var domain: Domain? = null,
    val service: Service,
    val serviceData: CommandDataWithEntityId
)
