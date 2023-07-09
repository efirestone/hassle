package com.codellyrandom.hassle.entities.devices

import com.codellyrandom.hassle.core.boot.statehandling.flattenStateAttributes
import com.codellyrandom.hassle.entities.Attributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.extending.entities.SwitchableState
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.sensors.*
import com.codellyrandom.hassle.extending.entities.sensors.binary.MotionSensorAttributes
import com.codellyrandom.hassle.values.*
import com.codellyrandom.hassle.withConnection
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
            assertEquals(5000.0, sensor.measurement.value)

            assertNull(sensor.attributes.userId)
            assertEquals(FriendlyName("Outside Luminance"), sensor.attributes.friendlyName)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), sensor.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), sensor.attributes.lastUpdated)
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
            assertEquals(SwitchableValue.OFF, sensor.measurement.value)

            assertNull(sensor.attributes.userId)
            assertEquals(FriendlyName("Hallway Motion"), sensor.attributes.friendlyName)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), sensor.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), sensor.attributes.lastUpdated)
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
            assertEquals(Zone("home"), sensor.measurement.value)

            assertEquals(UserId("abcd1234efgh5678"), sensor.attributes.userId)
            assertEquals(FriendlyName("John"), sensor.attributes.friendlyName)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), sensor.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), sensor.attributes.lastUpdated)

            assertEquals(PersonId("john"), sensor.attributes.id)
            assertEquals(EntityId.fromString("device_tracker.macbookair"), sensor.attributes.source)
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
            assertEquals(SunValue.BELOW_HORIZON, sensor.measurement.value)
            assertEquals(false, sensor.isAboveHorizon)
            assertEquals(true, sensor.isBelowHorizon)

            assertNull(sensor.attributes.userId)
            assertEquals(FriendlyName("Sun"), sensor.attributes.friendlyName)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), sensor.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), sensor.attributes.lastUpdated)

            assertEquals(Azimuth(301.67), sensor.attributes.azimuth)
            assertEquals(Elevation(-6.0), sensor.attributes.elevation)
            assertEquals(Instant.parse("2021-06-21T11:02:13.760874+00:00"), sensor.attributes.nextDawn)
            assertEquals(Instant.parse("2021-06-22T02:04:07.347021+00:00"), sensor.attributes.nextDusk)
            assertEquals(Instant.parse("2021-06-21T06:33:10+00:00"), sensor.attributes.nextMidnight)
            assertEquals(Instant.parse("2021-06-21T18:33:00+00:00"), sensor.attributes.nextNoon)
            assertEquals(Instant.parse("2021-06-21T11:30:20.573917+00:00"), sensor.attributes.nextRising)
            assertEquals(Instant.parse("2021-06-22T01:36:00.624975+00:00"), sensor.attributes.nextSetting)
            assertEquals(Rising.FALSE, sensor.attributes.rising)
        }
    }

    private inline fun <reified S : State<*>, reified A : Attributes> sensor(
        json: String,
        crossinline block: (Sensor<S, A>) -> Unit,
    ) = withConnection {
        val sut = Sensor(
            connection = this,
            mapper = mapper,
            stateType = S::class,
            attributesType = A::class,
        )

        val stateAsJsonObject = mapper.fromJson<JsonObject>(json)
        sut.trySetAttributesFromAny(flattenStateAttributes(stateAsJsonObject))
        sut.trySetActualStateFromAny(flattenStateAttributes(stateAsJsonObject))
        block(sut)
    }
}
