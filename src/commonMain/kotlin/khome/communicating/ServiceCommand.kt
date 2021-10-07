package khome.communicating

import khome.values.Domain
import khome.values.Service
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ServiceCommand<SD>(
    var domain: Domain? = null,
    val service: Service,
    override var id: Int? = null,
    @SerialName("service_data")
    val serviceData: SD? = null,
    override val type: CommandType = CommandType.CALL_SERVICE
) : HassApiCommand<SD>
