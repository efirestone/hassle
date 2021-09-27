package khome

import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.ActuatorImpl
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.UserId
import khome.values.domain
import khome.values.objectId
import khome.values.service
import kotlinx.datetime.Instant
import kotlin.test.Test

internal class KhomeApplicationActuatorTest {
    data class ActuatorState(override val value: String) : State<String>
    data class ActuatorAttributes(
        override val userId: UserId?,
        override val lastChanged: Instant,
        override val lastUpdated: Instant,
        override val friendlyName: FriendlyName
    ) : Attributes

    @Test
    fun `assert actuator factory creates new Actuator instance`() {
        withApplication {
            val actuator =
                Actuator<ActuatorState, ActuatorAttributes>(
                    EntityId.fromPair("light".domain to "some_light".objectId),
                    ActuatorState::class,
                    ActuatorAttributes::class,
                    ServiceCommandResolver {
                        DefaultResolvedServiceCommand(
                            null,
                            "turn_on".service,
                            EntityIdOnlyServiceData()
                        )
                    }
                )

            assert(actuator is ActuatorImpl)
        }

    }
}
