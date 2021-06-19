package khome

import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.SensorImpl
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.UserId
import khome.values.domain
import khome.values.objectId
import kotlinx.datetime.Instant
import kotlin.test.Test

internal class KhomeApplicationSensorTest {

    data class SensorState(override val value: String) : State<String>

    data class SensorAttributes(
        override val userId: UserId?,
        override val lastChanged: Instant,
        override val lastUpdated: Instant,
        override val friendlyName: FriendlyName
    ) : Attributes

    @Test
    fun `assert sensor factory creates new Sensor instance`() {
        withApplication {
            val sensor =
                Sensor<SensorState, SensorAttributes>(
                    EntityId.fromPair("sensor".domain to "some_sensor".objectId),
                    SensorState::class,
                    SensorAttributes::class
                )

            assert(sensor is SensorImpl)
        }
    }
}
