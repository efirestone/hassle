package com.codellyrandom.hassle

import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.TurnOnServiceCommand
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Actuator
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlin.test.Test

internal class HomeAssistantApiClientActuatorTest {
    class ActuatorState(
        override val value: String,
        val userId: UserId?,
        val lastChanged: Instant,
        val lastUpdated: Instant,
        val friendlyName: FriendlyName,
    ) : State<String>
    data class ActuatorSettableState(
        val value: String,
    )

    @Test
    fun `assert actuator factory creates new Actuator instance`() = withConnection {
        val actuator =
            Actuator(
                EntityId.fromString("light.some_light"),
                ActuatorState::class,
                ServiceCommandResolver<ActuatorSettableState> { entityId, _ ->
                    TurnOnServiceCommand(entityId)
                },
            )

        assert(actuator is Actuator<ActuatorState, ActuatorSettableState>)
    }
}
