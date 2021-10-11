package khome

import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ResolvedServiceCommand
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.values.*
import kotlinx.datetime.Instant
import kotlin.test.Test

internal class HomeAssistantApiClientActuatorTest {
    data class ActuatorState(override val value: String) : State<String>
    data class ActuatorAttributes(
        override val userId: UserId?,
        override val lastChanged: Instant,
        override val lastUpdated: Instant,
        override val friendlyName: FriendlyName
    ) : Attributes

    @Test
    fun `assert actuator factory creates new Actuator instance`() = withConnection {
        val actuator =
            Actuator<ActuatorState, ActuatorAttributes>(
                EntityId.fromPair("light".domain to "some_light".objectId),
                ActuatorState::class,
                ActuatorAttributes::class,
                ServiceCommandResolver {
                    ResolvedServiceCommand(
                        null,
                        "turn_on".service,
                        EntityIdOnlyServiceData()
                    )
                }
            )

        assert(actuator is Actuator<ActuatorState, ActuatorAttributes>)
    }
}
