package com.codellyrandom.hassle

import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.TurnOnServiceCommand
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.values.EntityId
import com.codellyrandom.hassle.values.FriendlyName
import com.codellyrandom.hassle.values.UserId
import kotlinx.datetime.Instant
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

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
            Actuator<ActuatorState, ActuatorSettableState>(
                EntityId.fromString("light.some_light"),
                typeOf<ActuatorState>(),
                ServiceCommandResolver { entityId, _ ->
                    TurnOnServiceCommand(entityId)
                },
            )

        assertEquals(actuator.entityId, EntityId.fromString("light.some_light"))
    }
}
