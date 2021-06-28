package khome.entities.devices

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.google.gson.JsonObject
import io.ktor.util.*
import khome.KhomeApplicationImpl
import khome.core.boot.statehandling.flattenStateAttributes
import khome.core.koin.KoinContainer
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.fromJson
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.extending.entities.sensors.*
import khome.extending.entities.sensors.binary.MotionSensorAttributes
import khome.khomeApplication
import khome.values.*
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.component.get

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SensorTest {

    @Test
    fun `parse luminance sensor`() {
        val json =
            //language=json
            """
                {
                    "entity_id": "sensor.luminance",
                    "state": "5000",
                    "attributes":
                    {
                        "unit_of_measurement": "lx",
                        "friendly_name": "Outside Luminance",
                        "device_class": "illuminance"
                    },
                    "last_changed": "2021-06-21T01:39:48.096892+00:00",
                    "last_updated": "2021-06-21T02:03:54.908902+00:00",
                    "context":
                    {
                        "id": "abcd1234",
                        "parent_id": null,
                        "user_id": null
                    }
                }
            """.trimIndent()

        sensor<LuminanceState, LuminanceAttributes>(json) { sensor ->
            assertThat(sensor.measurement.value).isEqualTo(5000.0)

            assertThat(sensor.attributes.userId).isNull()
            assertThat(sensor.attributes.friendlyName).isEqualTo(FriendlyName.from("Outside Luminance"))
            assertThat(sensor.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(sensor.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))
        }
    }

    @Test
    fun `parse motion sensor`() {
        val json =
            //language=json
            """
                {
                    "entity_id": "binary_sensor.hallway_motion",
                    "state": "off",
                    "attributes":
                    {
                        "friendly_name": "Hallway Motion",
                        "device_class": "motion"
                    },
                    "last_changed": "2021-06-21T01:39:48.096892+00:00",
                    "last_updated": "2021-06-21T02:03:54.908902+00:00",
                    "context":
                    {
                        "id": "abcd1234",
                        "parent_id": null,
                        "user_id": null
                    }
                }
            """.trimIndent()

        sensor<SwitchableState, MotionSensorAttributes>(json) { sensor ->
            assertThat(sensor.measurement.value).isEqualTo(SwitchableValue.OFF)

            assertThat(sensor.attributes.userId).isNull()
            assertThat(sensor.attributes.friendlyName).isEqualTo(FriendlyName.from("Hallway Motion"))
            assertThat(sensor.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(sensor.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))
        }
    }

    @Test
    fun `parse person sensor`() {
        val json =
            //language=json
            """
                {
                    "entity_id": "person.john",
                    "state": "home",
                    "attributes":
                    {
                        "editable": false,
                        "id": "john",
                        "latitude": 220.941325508873,
                        "longitude": -974.56800880282,
                        "gps_accuracy": 65,
                        "source": "device_tracker.macbookair",
                        "user_id": "abcd1234efgh5678",
                        "friendly_name": "John"
                    },
                    "last_changed": "2021-06-21T01:39:48.096892+00:00",
                    "last_updated": "2021-06-21T02:03:54.908902+00:00",
                    "context":
                    {
                        "id": "abcd1234",
                        "parent_id": null,
                        "user_id": null
                    }
                }
            """.trimIndent()

        sensor<PersonState, PersonAttributes>(json) { sensor ->
            assertThat(sensor.measurement.value).isEqualTo(Zone.from("home"))

            assertThat(sensor.attributes.userId).isEqualTo(UserId.from("abcd1234efgh5678"))
            assertThat(sensor.attributes.friendlyName).isEqualTo(FriendlyName.from("John"))
            assertThat(sensor.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(sensor.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))

            assertThat(sensor.attributes.id).isEqualTo(PersonId.from("john"))
            assertThat(sensor.attributes.source).isEqualTo(EntityId.fromString("device_tracker.macbookair"))
        }
    }

    @Test
    fun `parse sun sensor`() {
        val json =
            //language=json
            """
                {
                    "entity_id": "sun.sun",
                    "state": "below_horizon",
                    "attributes":
                    {
                        "next_dawn": "2021-06-21T11:02:13.760874+00:00",
                        "next_dusk": "2021-06-22T02:04:07.347021+00:00",
                        "next_midnight": "2021-06-21T06:33:10+00:00",
                        "next_noon": "2021-06-21T18:33:00+00:00",
                        "next_rising": "2021-06-21T11:30:20.573917+00:00",
                        "next_setting": "2021-06-22T01:36:00.624975+00:00",
                        "elevation": -6.0,
                        "azimuth": 301.67,
                        "rising": false,
                        "friendly_name": "Sun"
                    },
                    "last_changed": "2021-06-21T01:39:48.096892+00:00",
                    "last_updated": "2021-06-21T02:03:54.908902+00:00",
                    "context":
                    {
                        "id": "abcd1234",
                        "parent_id": null,
                        "user_id": null
                    }
                }
            """.trimIndent()

        sensor<SunState, SunAttributes>(json) { sensor ->
            assertThat(sensor.measurement.value).isEqualTo(SunValue.BELOW_HORIZON)
            assertThat(sensor.isAboveHorizon).isFalse()
            assertThat(sensor.isBelowHorizon).isTrue()

            assertThat(sensor.attributes.userId).isEqualTo(null)
            assertThat(sensor.attributes.friendlyName).isEqualTo(FriendlyName.from("Sun"))
            assertThat(sensor.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(sensor.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))

            assertThat(sensor.attributes.azimuth).isEqualTo(Azimuth.from(301.67))
            assertThat(sensor.attributes.elevation).isEqualTo(Elevation.from(-6.0))
            assertThat(sensor.attributes.next_dawn).isEqualTo(Instant.parse("2021-06-21T11:02:13.760874+00:00"))
            assertThat(sensor.attributes.next_dusk).isEqualTo(Instant.parse("2021-06-22T02:04:07.347021+00:00"))
            assertThat(sensor.attributes.next_midnight).isEqualTo(Instant.parse("2021-06-21T06:33:10+00:00"))
            assertThat(sensor.attributes.next_noon).isEqualTo(Instant.parse("2021-06-21T18:33:00+00:00"))
            assertThat(sensor.attributes.next_rising)
                .isEqualTo(Instant.parse("2021-06-21T11:30:20.573917+00:00"))
            assertThat(sensor.attributes.next_setting)
                .isEqualTo(Instant.parse("2021-06-22T01:36:00.624975+00:00"))
            assertThat(sensor.attributes.rising).isEqualTo(Rising.FALSE)
        }
    }

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(ExperimentalStdlibApi::class)
    private inline fun <reified S : State<*>, reified A : Attributes> sensor(json: String, block: (Sensor<S, A>) -> Unit) {
        khomeApplication().run {
            val mapper: ObjectMapperInterface = KoinContainer.get()
            val sut = SensorImpl<S, A>(
                app = KhomeApplicationImpl(),
                mapper = mapper,
                stateType = S::class,
                attributesType = A::class
            )

            val stateAsJsonObject = mapper.fromJson<JsonObject>(json)
            sut.trySetAttributesFromAny(flattenStateAttributes(stateAsJsonObject))
            sut.trySetActualStateFromAny(flattenStateAttributes(stateAsJsonObject))
            block(sut)
        }
    }
}
