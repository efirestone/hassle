package khome.entities.devices

import io.fluidsonic.time.LocalTime
import khome.KhomeApplicationImpl
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.core.boot.statehandling.flattenStateAttributes
import khome.core.koin.KoinContainer
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.fromJson
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.inputs.*
import khome.extending.entities.actuators.light.LightAttributes
import khome.extending.entities.actuators.light.RGBLightState
import khome.extending.entities.actuators.mediaplayer.MediaReceiverAttributes
import khome.extending.entities.actuators.mediaplayer.MediaReceiverState
import khome.extending.entities.actuators.mediaplayer.MediaReceiverStateValue
import khome.khomeApplication
import khome.values.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.koin.core.component.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class ActuatorTest {

    @Serializable
    data class ActuatorTestState(override val value: String, val boolean_attribute: Boolean, val int_attribute: Int) :
        State<String>

    @Serializable
    data class ActuatorTestAttributes(
        @SerialName("array_attribute")
        val arrayAttribute: List<Int>,
        @SerialName("double_attribute")
        val doubleAttribute: Double,
        @SerialName("user_id")
        override val userId: UserId?,
        @SerialName("last_changed")
        override val lastChanged: Instant,
        @SerialName("last_updated")
        override val lastUpdated: Instant,
        @SerialName("friendly_name")
        override val friendlyName: FriendlyName
    ) : Attributes

    private val mapper: ObjectMapperInterface
        get() = KoinContainer.get()

    @Test
    fun `actuator state response mapping is correct`() {

        val sut = ActuatorImpl<ActuatorTestState, ActuatorTestAttributes>(
            app = KhomeApplicationImpl(),
            mapper = mapper,
            resolver = ServiceCommandResolver {
                DefaultResolvedServiceCommand(
                    null,
                    "turn_on".service,
                    EntityIdOnlyServiceData()
                )
            },
            stateType = ActuatorTestState::class,
            attributesType = ActuatorTestAttributes::class
        )

        assertFailsWith<IllegalStateException> {
            sut.actualState
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

        sut.trySetAttributesFromAny(flattenStateAttributes(stateAsJsonObject))
        sut.trySetActualStateFromAny(flattenStateAttributes(stateAsJsonObject))

        assertEquals("on", sut.actualState.value)
        assertEquals(true, sut.actualState.boolean_attribute)
        assertEquals(73, sut.actualState.int_attribute)
        assertEquals(listOf(1, 2, 3, 4, 5), sut.attributes.arrayAttribute)
        assertEquals(30.0, sut.attributes.doubleAttribute)
        assertEquals(FriendlyName("Test Entity"), sut.attributes.friendlyName)
        assertEquals(Instant.parse("2016-11-26T01:37:24.265390+00:00"), sut.attributes.lastChanged)
        assertEquals(Instant.parse("2016-11-26T01:37:24.265390+00:00"), sut.attributes.lastUpdated)
    }

    @Test
    fun `actuator stores state and attributes youngest first`() {
        val sut = ActuatorImpl<ActuatorTestState, ActuatorTestAttributes>(
            app = KhomeApplicationImpl(),
            mapper = mapper,
            resolver = ServiceCommandResolver {
                DefaultResolvedServiceCommand(
                    null,
                    "turn_on".service,
                    EntityIdOnlyServiceData()
                )
            },
            stateType = ActuatorTestState::class,
            attributesType = ActuatorTestAttributes::class
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

        sut.trySetAttributesFromAny(flattenStateAttributes(firstStateAsJsonObject))
        sut.trySetActualStateFromAny(flattenStateAttributes(firstStateAsJsonObject))

        assertEquals(1, sut.history.size)
        assertEquals(sut.history.first().state, sut.actualState)
        assertEquals("off", sut.actualState.value)

        val secondStateAsJsonObject = mapper.fromJson<JsonObject>(secondTestState)

        sut.trySetAttributesFromAny(flattenStateAttributes(secondStateAsJsonObject))
        sut.trySetActualStateFromAny(flattenStateAttributes(secondStateAsJsonObject))

        assertEquals(2, sut.history.size)
        assertEquals(sut.history.first().state, sut.actualState)
        assertEquals("off", sut.history[1].state.value)
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

        actuator<SwitchableState, InputBooleanAttributes>(json) { actuator ->
            assertEquals(SwitchableValue.ON, actuator.actualState.value)

            assertEquals("userid", actuator.attributes.userId?.value)
            assertEquals("Notify when someone arrives home", actuator.attributes.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.attributes.lastUpdated)

            assertEquals(false, actuator.attributes.editable)
            assertEquals("mdi:car", actuator.attributes.icon.value)
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

        actuator<InputDateState, InputDateAttributes>(json) { actuator ->
            assertEquals(LocalDate.parse("2021-06-16"), actuator.actualState.value)

            assertEquals("userid", actuator.attributes.userId?.value)
            assertEquals("Input with only date", actuator.attributes.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.attributes.lastUpdated)

            assertEquals(false, actuator.attributes.editable)
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

        actuator<InputDateTimeState, InputDateTimeAttributes>(json) { actuator ->
            assertEquals(LocalDateTime.parse("2021-06-21T00:00:00"), actuator.actualState.value)

            assertNull(actuator.attributes.userId?.value)
            assertEquals("Input with both date and time", actuator.attributes.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.attributes.lastUpdated)

            assertEquals(false, actuator.attributes.editable)
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

        actuator<InputNumberState, InputNumberAttributes>(json) { actuator ->
            assertEquals(29.0, actuator.actualState.value)

            assertEquals("userid", actuator.attributes.userId?.value)
            assertEquals("Numeric Input Box", actuator.attributes.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.attributes.lastUpdated)

            assertEquals(false, actuator.attributes.editable)
            assertEquals(35.0, actuator.attributes.max.value)
            assertEquals(-20.0, actuator.attributes.min.value)
            assertEquals(1.0, actuator.attributes.step.value)
            assertEquals(Mode("box"), actuator.attributes.mode)
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
        actuator<InputSelectState, InputSelectAttributes>(json) { actuator ->
            assertEquals(Option("Visitors"), actuator.actualState.value)

            assertNull(actuator.attributes.userId?.value)
            assertNull(actuator.attributes.friendlyName?.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.attributes.lastUpdated)

            assertEquals(false, actuator.attributes.editable)
            assertEquals(
                listOf(
                    Option("Visitors"),
                    Option("Visitors with kids"),
                    Option("Home Alone"),
                ),
                actuator.attributes.options
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

        actuator<InputTextState, InputTextAttributes>(json) { actuator ->
            assertEquals("abc", actuator.actualState.value)

            assertEquals("userid", actuator.attributes.userId?.value)
            assertEquals("Text 1", actuator.attributes.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.attributes.lastUpdated)

            assertEquals(false, actuator.attributes.editable)
            assertEquals(0.0, actuator.attributes.min.value)
            assertEquals(100.0, actuator.attributes.max.value)
            assertEquals(Regex("[a-fA-F0-9]*").pattern, actuator.attributes.pattern.pattern)
            assertEquals("text", actuator.attributes.mode.value)
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

        actuator<InputTimeState, InputTimeAttributes>(json) { actuator ->
            assertEquals(LocalTime.parse("01:00:00"), actuator.actualState.value)

            assertEquals("userid", actuator.attributes.userId?.value)
            assertEquals("Input with only time", actuator.attributes.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.attributes.lastUpdated)

            assertEquals(false, actuator.attributes.editable)
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

        actuator<MediaReceiverState, MediaReceiverAttributes>(json) { actuator ->
            assertEquals(MediaReceiverStateValue.PLAYING, actuator.actualState.value)
            assertEquals(Mute.FALSE, actuator.actualState.isVolumeMuted)
            assertEquals(MediaPosition(26.0), actuator.actualState.mediaPosition)
            assertNull(actuator.actualState.volumeLevel)

            assertNull(actuator.attributes.userId?.value)
            assertEquals("Plex (Plex for Apple TV - Play Room)", actuator.attributes.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.attributes.lastUpdated)

            assertNull(actuator.attributes.appId)
            assertNull(actuator.attributes.appName)
            assertEquals(
                EntityPicture("/api/media_player_proxy/media_player.plex_plex_for_apple_tv_play_room?token=abcd1234&cache=4321dcba"),
                actuator.attributes.entityPicture
            )
            assertNull(actuator.attributes.mediaAlbumName)
            assertNull(actuator.attributes.mediaArtist)
            assertEquals(MediaContentId("8675309"), actuator.attributes.mediaContentId)
            assertEquals(MediaContentType.MOVIE, actuator.attributes.mediaContentType)
            assertEquals(MediaDuration(5059.0), actuator.attributes.mediaDuration)
            assertEquals(Instant.parse("2021-06-24T22:49:41.534947+00:00"), actuator.attributes.mediaPositionUpdatedAt)
            assertEquals(MediaTitle("Super Awesome Movie (2021)"), actuator.attributes.mediaTitle)
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

        actuator<RGBLightState, LightAttributes>(json) { actuator ->
            assertEquals(SwitchableValue.ON, actuator.actualState.value)
            assertEquals(Brightness(138), actuator.actualState.brightness)
            assertEquals(HSColor(340.0, 7.059), actuator.actualState.hsColor)
            assertEquals(RGBColor(255, 236, 242), actuator.actualState.rgbColor)
            assertEquals(XYColor(0.34, 0.321), actuator.actualState.xyColor)

            assertEquals("userid", actuator.attributes.userId?.value)
            assertEquals("Light Strip 1", actuator.attributes.friendlyName.value)
            assertEquals(Instant.parse("2021-06-21T01:39:48.096892+00:00"), actuator.attributes.lastChanged)
            assertEquals(Instant.parse("2021-06-21T02:03:54.908902+00:00"), actuator.attributes.lastUpdated)

            assertEquals(63, actuator.attributes.supportedFeatures)
        }
    }

    // Private Methods

    private inline fun <reified S : State<*>, reified A : Attributes> actuator(json: String, block: (Actuator<S, A>) -> Unit) {
        khomeApplication().run {
            val mapper: ObjectMapperInterface = KoinContainer.get()
            val sut = ActuatorImpl<S, A>(
                app = KhomeApplicationImpl(),
                mapper = mapper,
                resolver = ServiceCommandResolver {
                    DefaultResolvedServiceCommand(
                        null,
                        "turn_on".service,
                        EntityIdOnlyServiceData()
                    )
                },
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
