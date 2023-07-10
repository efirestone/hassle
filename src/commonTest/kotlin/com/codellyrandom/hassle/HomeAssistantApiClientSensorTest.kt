package com.codellyrandom.hassle

import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.entities.devices.Sensor
import com.codellyrandom.hassle.values.*
import kotlinx.datetime.Instant
import kotlin.test.Test

internal class HomeAssistantApiClientSensorTest {

    data class SensorState(
        override val value: String,
        val userId: UserId?,
        val lastChanged: Instant,
        val lastUpdated: Instant,
        val friendlyName: FriendlyName,
    ) : State<String>

    @Test
    fun `assert sensor factory creates new Sensor instance`() {
        withConnection {
            val sensor =
                Sensor(
                    EntityId.fromPair("sensor".domain to "some_sensor".objectId),
                    SensorState::class,
                )

            assert(sensor is Sensor)
        }
    }
}
