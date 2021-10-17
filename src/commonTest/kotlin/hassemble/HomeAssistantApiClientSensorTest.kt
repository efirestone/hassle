package hassemble

import hassemble.entities.Attributes
import hassemble.entities.State
import hassemble.entities.devices.Sensor
import hassemble.values.EntityId
import hassemble.values.FriendlyName
import hassemble.values.UserId
import hassemble.values.domain
import hassemble.values.objectId
import kotlinx.datetime.Instant
import kotlin.test.Test

internal class HomeAssistantApiClientSensorTest {

    data class SensorState(override val value: String) : State<String>

    data class SensorAttributes(
        override val userId: UserId?,
        override val lastChanged: Instant,
        override val lastUpdated: Instant,
        override val friendlyName: FriendlyName
    ) : Attributes

    @Test
    fun `assert sensor factory creates new Sensor instance`() {
        withConnection {
            val sensor =
                Sensor<SensorState, SensorAttributes>(
                    EntityId.fromPair("sensor".domain to "some_sensor".objectId),
                    SensorState::class,
                    SensorAttributes::class
                )

            assert(sensor is Sensor)
        }
    }
}
