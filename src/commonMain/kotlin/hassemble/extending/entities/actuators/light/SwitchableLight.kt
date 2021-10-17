package hassemble.extending.entities.actuators.light

import hassemble.HomeAssistantApiClient
import hassemble.communicating.ServiceCommandResolver
import hassemble.entities.devices.Actuator
import hassemble.extending.entities.SwitchableState
import hassemble.extending.entities.mapSwitchable
import hassemble.values.ObjectId

typealias SwitchableLight = Actuator<SwitchableState, LightAttributes>

@Suppress("FunctionName")
fun HomeAssistantApiClient.SwitchableLight(objectId: ObjectId): SwitchableLight =
    Light(
        objectId,
        ServiceCommandResolver { entityId, desiredState ->
            mapSwitchable(entityId, desiredState.value)
        }
    )
