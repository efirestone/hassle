package com.codellyrandom.hassle.entities.devices

import com.codellyrandom.hassle.communicating.ServiceCommandResolver
import com.codellyrandom.hassle.communicating.TurnOnServiceCommand
import com.codellyrandom.hassle.core.boot.statehandling.flattenStateAttributes
import com.codellyrandom.hassle.entities.State
import com.codellyrandom.hassle.extending.entities.SwitchableSettableState
import com.codellyrandom.hassle.extending.entities.SwitchableValue
import com.codellyrandom.hassle.extending.entities.actuators.inputs.*
import com.codellyrandom.hassle.extending.entities.actuators.light.RGBLightSettableState
import com.codellyrandom.hassle.extending.entities.actuators.light.RGBLightState
import com.codellyrandom.hassle.extending.entities.actuators.mediaplayer.MediaReceiverSettableState
import com.codellyrandom.hassle.extending.entities.actuators.mediaplayer.MediaReceiverState
import com.codellyrandom.hassle.extending.entities.actuators.mediaplayer.MediaReceiverStateValue
import com.codellyrandom.hassle.values.*
import com.codellyrandom.hassle.withConnection
import io.fluidsonic.time.LocalTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class ActuatorTest {

    @Serializable
    data class ActuatorTestState(
        override val value: String,
        @SerialName("boolean_attribute")
        val booleanAttribute: Boolean,
        @SerialName("int_attribute")
        val intAttribute: Int,
        @SerialName("array_attribute")
        val arrayAttribute: List<Int>,
        @SerialName("double_attribute")
        val doubleAttribute: Double,
        @SerialName("user_id")
        val userId: UserId?,
        @SerialName("last_changed")
        val lastChanged: Instant,
        @SerialName("last_updated")
        val lastUpdated: Instant,
        @SerialName("friendly_name")
        val friendlyName: FriendlyName,
    ) : State<String>

    data class ActuatorTestSettableState(
        override val value: String,
        val boolean_attribute: Boolean,
        val int_attribute: Int,
    ) : State<String>

    @Test
    fun `actuator state response mapping is correct`() = withConnection {
        val sut = Actuator<ActuatorTestState, ActuatorTestSettableState>(
            EntityId.fromString("actuator.test"),
            connection = this,
            mapper = mapper,
            resolver = ServiceCommandResolver { entityId, _ ->
                TurnOnServiceCommand(entityId)
            },
            stateType = typeOf<ActuatorTestState>(),
        )

        assertFailsWith<IllegalStateException> {
            sut.state
        }

        val testStateJson =
            //language=json
            """
            {
                "entity_id":"test.object_id",
                "last_changed":"2016-11-26T01:37:24.265390+00:00",
                "state":"on",
                "attributes":{
                    "array_attribute": [1,2,3,4,5],
                    "int_attribute": 73,
                    "double_attribute": 30.0,
                    "boolean_attribute": true,
                    "friendly_name":"Test Entity"
                },
                "last_updated":"2016-11-26T01:37:24.265390+00:00",
                "context": { "user_id": null }
             }
            """.trimIndent()

        val stateAsJsonObject = mapper.fromJson<JsonObject>(testStateJson)

        sut.trySetStateFromAny(flattenStateAttributes(stateAsJsonObject))

        assertEquals("on", sut.state.value)
        assertEquals(true, sut.state.booleanAttribute)
        assertEquals(73, sut.state.intAttribute)
        assertEquals(listOf(1, 2, 3, 4, 5), sut.state.arrayAttribute)
        assertEquals(30.0, sut.state.doubleAttribute)
        assertEquals(FriendlyName("Test Entity"), sut.state.friendlyName)
        assertEquals(Instant.parse("2016-11-26T01:37:24.265390+00:00"), sut.state.lastChanged)
        assertEquals(Instant.parse("2016-11-26T01:37:24.265390+00:00"), sut.state.lastUpdated)
    }

    @Test
    fun `actuator stores state and attributes youngest first`() = withConnection {
        val sut = Actuator<ActuatorTestState, ActuatorTestSettableState>(
            EntityId.fromString("actuator.test"),
            connection = this,
            mapper = mapper,
            resolver = ServiceCommandResolver { entityId, _ ->
                TurnOnServiceCommand(entityId)
            },
            stateType = typeOf<ActuatorTestState>(),
        )

        val firstTestState =
            //language=json
            """
            {
                "entity_id":"test.object_id",
                "last_changed":"2016-11-26T01:37:24.265390+00:00",
                "state":"off",
                "attributes":{
                    "array_attribute": [1,2,3,4,5],
                    "int_attribute": 73,
                    "double_attribute": 30.0,
                    "boolean_attribute": true,
                    "friendly_name":"Test Entity"
                },
                "last_updated":"2016-11-26T01:37:24.265390+00:00",
                "context": { "user_id": null }
             }
            """.trimIndent()

        val secondTestState =
            //language=json
            """
            {
                "entity_id":"test.object_id",
                "last_changed":"2016-11-26T01:37:24.265390+00:00",
                "state":"on",
                "attributes":{
                    "array_attribute": [1,2,3,4,5],
                    "int_attribute": 73,
                    "double_attribute": 30.0,
                    "boolean_attribute": true,
                    "friendly_name":"Test Entity"
                },
                "last_updated":"2016-11-26T01:37:24.265390+00:00",
                "context": { "user_id": null }
            }
            """.trimIndent()

        val firstStateAsJsonObject = mapper.fromJson<JsonObject>(firstTestState)

        sut.trySetStateFromAny(flattenStateAttributes(firstStateAsJsonObject))

        assertEquals(1, sut.history.size)
        assertEquals(sut.history.first(), sut.state)
        assertEquals("off", sut.state.value)

        val secondStateAsJsonObject = mapper.fromJson<JsonObject>(secondTestState)

        sut.trySetStateFromAny(flattenStateAttributes(secondStateAsJsonObject))

        assertEquals(2, sut.history.size)
        assertEquals(sut.history[1], sut.state)
        assertEquals("on", sut.history[1].value)
    }

    // Tests - Parsing

    @Test
    fun `parse input boolean actuator`() {
        val json =
            //language=json
            """
            {
                "entity_id": "input_boolean.notify_home",
                "state": "on",
                "attributes":
                {
                    "editable": false,
                    "friendly_name": "Notify when someone arrives home",
                    "icon": "mdi:car"
                },
                "last_changed": "2021-06-21T01:39:48.096892+00:00",
                "last_updated": "2021-06-21T02:03:54.908902+00:00",
                "context":
                {
                    "id": "abcd1234",
                    "parent_id": null,
                    "user_id": "userid"
                }
            }
            """.trimIndent()

        actuator<InputBooleanState, SwitchableSettableState>(json) { actuator ->
            assertEquals(SwitchableValue.ON, actuator.state.value)

            assertEquals("userid", actuator.state.userId?.value)
            assertEquals("Notify when someone arrives home", actuator.state.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.state.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.state.lastUpdated)

            assertEquals(false, actuator.state.editable)
            assertEquals("mdi:car", actuator.state.icon.value)
        }
    }

    @Test
    fun `parse input date actuator`() {
        val json =
            //language=json
            """
            {
                "entity_id": "input_datetime.only_date",
                "state": "2021-06-16",
                "attributes":
                {
                    "editable": false,
                    "has_date": true,
                    "has_time": false,
                    "year": 2021,
                    "month": 6,
                    "day": 16,
                    "timestamp": 1623819600.0,
                    "friendly_name": "Input with only date"
                },
                "last_changed": "2021-06-21T01:39:48.096892+00:00",
                "last_updated": "2021-06-21T02:03:54.908902+00:00",
                "context":
                {
                    "id": "abcd1234",
                    "parent_id": null,
                    "user_id": "userid"
                }
            }
            """.trimIndent()

        actuator<InputDateState, InputDateSettableState>(json) { actuator ->
            assertEquals(LocalDate.parse("2021-06-16"), actuator.state.value)

            assertEquals("userid", actuator.state.userId?.value)
            assertEquals("Input with only date", actuator.state.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.state.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.state.lastUpdated)

            assertEquals(false, actuator.state.editable)
        }
    }

    @Test
    fun `parse input datetime actuator`() {
        val json =
            //language=json
            """
            {
                "entity_id": "input_datetime.both_date_and_time",
                "state": "2021-06-21 00:00:00",
                "attributes":
                {
                    "editable": false,
                    "has_date": true,
                    "has_time": true,
                    "year": 2021,
                    "month": 6,
                    "day": 21,
                    "hour": 0,
                    "minute": 0,
                    "second": 0,
                    "timestamp": 1624251600.0,
                    "friendly_name": "Input with both date and time"
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

        actuator<InputDateTimeState, InputDateTimeSettableState>(json) { actuator ->
            assertEquals(LocalDateTime.parse("2021-06-21T00:00:00"), actuator.state.value)

            assertNull(actuator.state.userId?.value)
            assertEquals("Input with both date and time", actuator.state.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.state.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.state.lastUpdated)

            assertEquals(false, actuator.state.editable)
        }
    }

    @Test
    fun `parse input number actuator`() {
        val json =
            //language=json
            """
            {
                "entity_id": "input_number.box1",
                "state": "29.0",
                "attributes":
                {
                    "initial": 30.0,
                    "editable": false,
                    "min": -20.0,
                    "max": 35.0,
                    "step": 1.0,
                    "mode": "box",
                    "friendly_name": "Numeric Input Box"
                },
                "last_changed": "2021-06-21T01:39:48.096892+00:00",
                "last_updated": "2021-06-21T02:03:54.908902+00:00",
                "context":
                {
                    "id": "abcd1234",
                    "parent_id": null,
                    "user_id": "userid"
                }
            }
            """.trimIndent()

        actuator<InputNumberState, InputNumberSettableState>(json) { actuator ->
            assertEquals(29.0, actuator.state.value)

            assertEquals("userid", actuator.state.userId?.value)
            assertEquals("Numeric Input Box", actuator.state.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.state.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.state.lastUpdated)

            assertEquals(false, actuator.state.editable)
            assertEquals(35.0, actuator.state.max.value)
            assertEquals(-20.0, actuator.state.min.value)
            assertEquals(1.0, actuator.state.step.value)
            assertEquals(Mode("box"), actuator.state.mode)
        }
    }

    @Test
    fun `parse input select actuator`() {
        val json =
            //language=json
            """
            {
                "entity_id": "input_select.living_room_preset",
                "state": "Visitors",
                "attributes":
                {
                    "options":
                    [
                        "Visitors",
                        "Visitors with kids",
                        "Home Alone"
                    ],
                    "editable": false
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
        actuator<InputSelectState, InputSelectSettableState>(json) { actuator ->
            assertEquals(Option("Visitors"), actuator.state.value)

            assertNull(actuator.state.userId?.value)
            assertNull(actuator.state.friendlyName?.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.state.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.state.lastUpdated)

            assertEquals(false, actuator.state.editable)
            assertEquals(
                listOf(
                    Option("Visitors"),
                    Option("Visitors with kids"),
                    Option("Home Alone"),
                ),
                actuator.state.options,
            )
        }
    }

    @Test
    fun `test input text actuator`() {
        val json =
            //language=json
            """
            {
                "entity_id": "input_text.text1",
                "state": "abc",
                "attributes":
                {
                    "editable": false,
                    "min": 0,
                    "max": 100,
                    "pattern": "[a-fA-F0-9]*",
                    "mode": "text",
                    "friendly_name": "Text 1"
                },
                "last_changed": "2021-06-21T01:39:48.096892+00:00",
                "last_updated": "2021-06-21T02:03:54.908902+00:00",
                "context":
                {
                    "id": "abcd1234",
                    "parent_id": null,
                    "user_id": "userid"
                }
            }
            """.trimIndent()

        actuator<InputTextState, InputTextSettableState>(json) { actuator ->
            assertEquals("abc", actuator.state.value)

            assertEquals("userid", actuator.state.userId?.value)
            assertEquals("Text 1", actuator.state.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.state.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.state.lastUpdated)

            assertEquals(false, actuator.state.editable)
            assertEquals(0.0, actuator.state.min.value)
            assertEquals(100.0, actuator.state.max.value)
            assertEquals(Regex("[a-fA-F0-9]*").pattern, actuator.state.pattern.pattern)
            assertEquals("text", actuator.state.mode.value)
        }
    }

    @Test
    fun `test input time actuator`() {
        val json =
            //language=json
            """
            {
                "entity_id": "input_datetime.only_time",
                "state": "01:00:00",
                "attributes":
                {
                    "editable": false,
                    "has_date": false,
                    "has_time": true,
                    "hour": 1,
                    "minute": 0,
                    "second": 0,
                    "timestamp": 3600,
                    "friendly_name": "Input with only time"
                },
                "last_changed": "2021-06-21T01:39:48.096892+00:00",
                "last_updated": "2021-06-21T02:03:54.908902+00:00",
                "context":
                {
                    "id": "abcd1234",
                    "parent_id": null,
                    "user_id": "userid"
                }
            }
            """.trimIndent()

        actuator<InputTimeState, InputTimeSettableState>(json) { actuator ->
            assertEquals(LocalTime.parse("01:00:00"), actuator.state.value)

            assertEquals("userid", actuator.state.userId?.value)
            assertEquals("Input with only time", actuator.state.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.state.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.state.lastUpdated)

            assertEquals(false, actuator.state.editable)
        }
    }

    @Test
    fun `parse media player playing a movie`() {
        val json =
            //language=json
            """
            {
                "entity_id": "media_player.plex_plex_for_apple_tv_play_room",
                "state": "playing",
                "attributes":
                {
                    "is_volume_muted": false,
                    "media_content_id": 8675309,
                    "media_content_type": "movie",
                    "media_duration": 5059,
                    "media_position": 26,
                    "media_position_updated_at": "2021-06-24T22:49:41.534947+00:00",
                    "media_title": "Super Awesome Movie (2021)",
                    "media_content_rating": "PG-13",
                    "media_library_title": "Movies",
                    "player_source": "session",
                    "media_summary": "This is the best movie ever.",
                    "username": "johnsmith",
                    "friendly_name": "Plex (Plex for Apple TV - Play Room)",
                    "entity_picture": "/api/media_player_proxy/media_player.plex_plex_for_apple_tv_play_room?token=abcd1234&cache=4321dcba",
                    "supported_features": 131584
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

        actuator<MediaReceiverState, MediaReceiverSettableState>(json) { actuator ->
            assertEquals(MediaReceiverStateValue.PLAYING, actuator.state.value)
            assertEquals(Mute.FALSE, actuator.state.isVolumeMuted)
            assertEquals(MediaPosition(26.0), actuator.state.mediaPosition)
            assertNull(actuator.state.volumeLevel)

            assertNull(actuator.state.userId?.value)
            assertEquals("Plex (Plex for Apple TV - Play Room)", actuator.state.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.state.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.state.lastUpdated)

            assertNull(actuator.state.appId)
            assertNull(actuator.state.appName)
            assertEquals(
                EntityPicture("/api/media_player_proxy/media_player.plex_plex_for_apple_tv_play_room?token=abcd1234&cache=4321dcba"),
                actuator.state.entityPicture,
            )
            assertNull(actuator.state.mediaAlbumName)
            assertNull(actuator.state.mediaArtist)
            assertEquals(MediaContentId("8675309"), actuator.state.mediaContentId)
            assertEquals(MediaContentType.MOVIE, actuator.state.mediaContentType)
            assertEquals(MediaDuration(5059.0), actuator.state.mediaDuration)
            assertEquals(Instant.parse("2021-06-24T22:49:41.534947+00:00"), actuator.state.mediaPositionUpdatedAt)
            assertEquals(MediaTitle("Super Awesome Movie (2021)"), actuator.state.mediaTitle)
        }
    }

    @Test
    fun `parse RGB light`() {
        val json =
            //language=json
            """
            {
                "entity_id": "light.light_strip_bar_1",
                "state": "on",
                "attributes":
                {
                    "min_mireds": 153,
                    "max_mireds": 588,
                    "effect_list": ["Strobe color", "Police", "Christmas", "RGB", "Random Loop", "Disco"],
                    "supported_color_modes": ["color_temp", "hs"],
                    "color_mode": "hs",
                    "brightness": 138,
                    "hs_color": [340.0, 7.059],
                    "rgb_color": [255, 236, 242],
                    "xy_color": [0.34, 0.321],
                    "color_temp": 250,
                    "flowing": false,
                    "music_mode": false,
                    "friendly_name": "Light Strip 1",
                    "supported_features": 63
                },
                "last_changed": "2021-06-21T01:39:48.096892+00:00",
                "last_updated": "2021-06-21T02:03:54.908902+00:00",
                "context":
                {
                    "id": "abcd1234",
                    "parent_id": null,
                    "user_id": "userid"
                }
            }
            """.trimIndent()

        actuator<RGBLightState, RGBLightSettableState>(json) { actuator ->
            assertEquals(SwitchableValue.ON, actuator.state.value)
            assertEquals(Brightness(138), actuator.state.brightness)
            assertEquals(HSColor(340.0, 7.059), actuator.state.hsColor)
            assertEquals(RGBColor(255, 236, 242), actuator.state.rgbColor)
            assertEquals(XYColor(0.34, 0.321), actuator.state.xyColor)

            assertEquals("userid", actuator.state.userId?.value)
            assertEquals("Light Strip 1", actuator.state.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.state.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.state.lastUpdated)

            assertEquals(63, actuator.state.supportedFeatures)
        }
    }

    // Private Methods

    private inline fun <reified S : State<*>, reified SS : Any> actuator(
        json: String,
        crossinline block: (Actuator<S, SS>) -> Unit,
    ) = withConnection {
        val sut = Actuator<S, SS>(
            EntityId.fromString("actuator.test"),
            connection = this,
            mapper = mapper,
            resolver = ServiceCommandResolver { entityId, _ ->
                TurnOnServiceCommand(entityId)
            },
            stateType = typeOf<S>(),
        )

        val stateAsJsonObject = mapper.fromJson<JsonObject>(json)
        sut.trySetStateFromAny(flattenStateAttributes(stateAsJsonObject))
        block(sut)
    }
}
