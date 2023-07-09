package com.codellyrandom.hassle

import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlin.test.Test

internal class HomeAssistantApiClientSensorTest {

    data class SensorState(override val value: String) : State<String>

    data class SensorAttributes(
        override val userId: UserId?,
        override val lastChanged: Instant,
        override val lastUpdated: Instant,
        override val friendlyName: FriendlyName,
    ) : Attributes

    @Test
    fun `assert sensor factory creates new Sensor instance`() {
        withConnection {
            val sensor =
                Sensor(
                    EntityId.fromPair("sensor".domain to "some_sensor".objectId),
                    SensorState::class,
                    SensorAttributes::class,
                )

            assert(sensor is Sensor)
        }
    }
}
