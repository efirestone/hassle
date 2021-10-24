package com.codellyrandom.hassle

import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.TurnOnServiceCommand
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.values.*
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
                EntityId.fromString("light.some_light"),
                ActuatorState::class,
                ActuatorAttributes::class,
                ServiceCommandResolver { entityId, _ ->
                    TurnOnServiceCommand(entityId)
                }
            )

        assert(actuator is Actuator<ActuatorState, ActuatorAttributes>)
    }
}
