package khome.entities.devices

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.google.gson.JsonObject
import io.fluidsonic.time.LocalTime
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplicationImpl
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.core.boot.statehandling.flattenStateAttributes
import khome.core.koin.KhomeKoinContext
import khome.core.koin.KoinContainer
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.fromJson
import khome.entities.Attributes
import khome.entities.State
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.extending.entities.actuators.inputs.*
import khome.khomeApplication
import khome.values.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.component.get

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActuatorTest {

    data class ActuatorTestState(override val value: String, val booleanAttribute: Boolean, val intAttribute: Int) :
        State<String>

    data class ActuatorTestAttributes(
        val arrayAttribute: List<Int>,
        val doubleAttribute: Double,
        override val userId: UserId?,
        override val lastChanged: Instant,
        override val lastUpdated: Instant,
        override val friendlyName: FriendlyName
    ) : Attributes

    @BeforeAll
    fun createKhome() {
        khomeApplication()
    }

    private val mapper: ObjectMapperInterface
        get() = KoinContainer.get()

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(KtorExperimentalAPI::class)
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

        assertThrows<IllegalStateException> {
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

        assertThat(sut.actualState.value).isEqualTo("on")
        assertThat(sut.actualState.booleanAttribute).isEqualTo(true)
        assertThat(sut.actualState.intAttribute).isEqualTo(73)
        assertThat(sut.attributes.arrayAttribute).isEqualTo(listOf(1, 2, 3, 4, 5))
        assertThat(sut.attributes.doubleAttribute).isEqualTo(30.0)
        assertThat(sut.attributes.friendlyName).isEqualTo(FriendlyName.from("Test Entity"))
        assertThat(sut.attributes.lastChanged).isEqualTo(
            Instant.parse("2016-11-26T01:37:24.265390+00:00")
        )
        assertThat(sut.attributes.lastUpdated).isEqualTo(
            Instant.parse("2016-11-26T01:37:24.265390+00:00")
        )
    }

    @OptIn(KtorExperimentalAPI::class, ObsoleteCoroutinesApi::class)
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

        assertThat(sut.history.size).isEqualTo(1)
        assertThat(sut.actualState).isEqualTo(sut.history.first().state)
        assertThat(sut.actualState.value).isEqualTo("off")

        val secondStateAsJsonObject = mapper.fromJson<JsonObject>(secondTestState)

        sut.trySetAttributesFromAny(flattenStateAttributes(secondStateAsJsonObject))
        sut.trySetActualStateFromAny(flattenStateAttributes(secondStateAsJsonObject))

        assertThat(sut.history.size).isEqualTo(2)
        assertThat(sut.actualState).isEqualTo(sut.history.first().state)
        assertThat(sut.history[1].state.value).isEqualTo("off")
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
            assertThat(actuator.actualState.value).isEqualTo(SwitchableValue.ON)

            assertThat(actuator.attributes.userId?.value).isEqualTo("userid")
            assertThat(actuator.attributes.friendlyName.value).isEqualTo("Notify when someone arrives home")
            assertThat(actuator.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(actuator.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))

            assertThat(actuator.attributes.editable).isEqualTo(false)
            assertThat(actuator.attributes.icon.value).isEqualTo("mdi:car")
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
            assertThat(actuator.actualState.value).isEqualTo(LocalDate.parse("2021-06-16"))

            assertThat(actuator.attributes.userId?.value).isEqualTo("userid")
            assertThat(actuator.attributes.friendlyName.value).isEqualTo("Input with only date")
            assertThat(actuator.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(actuator.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))

            assertThat(actuator.attributes.editable).isEqualTo(false)
            assertThat(actuator.attributes.timestamp).isEqualTo(1623819600.0)
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
            assertThat(actuator.actualState.value).isEqualTo(LocalDateTime.parse("2021-06-21T00:00:00"))

            assertThat(actuator.attributes.userId?.value).isNull()
            assertThat(actuator.attributes.friendlyName.value).isEqualTo("Input with both date and time")
            assertThat(actuator.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(actuator.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))

            assertThat(actuator.attributes.editable).isEqualTo(false)
            assertThat(actuator.attributes.timestamp).isEqualTo(1624251600)
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
            assertThat(actuator.actualState.value).isEqualTo(29.0)

            assertThat(actuator.attributes.userId?.value).isEqualTo("userid")
            assertThat(actuator.attributes.friendlyName.value).isEqualTo("Numeric Input Box")
            assertThat(actuator.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(actuator.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))

            assertThat(actuator.attributes.editable).isEqualTo(false)
            assertThat(actuator.attributes.max.value).isEqualTo(35.0)
            assertThat(actuator.attributes.min.value).isEqualTo(-20.0)
            assertThat(actuator.attributes.step.value).isEqualTo(1.0)
            assertThat(actuator.attributes.mode).isEqualTo(Mode.from("box"))
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
            assertThat(actuator.actualState.value).isEqualTo(Option.from("Visitors"))

            assertThat(actuator.attributes.userId?.value).isNull()
            assertThat(actuator.attributes.friendlyName?.value).isNull()
            assertThat(actuator.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(actuator.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))

            assertThat(actuator.attributes.editable).isEqualTo(false)
            assertThat(actuator.attributes.options).isEqualTo(
                listOf(
                    Option.from("Visitors"),
                    Option.from("Visitors with kids"),
                    Option.from("Home Alone"),
                )
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
            assertThat(actuator.actualState.value).isEqualTo("abc")

            assertThat(actuator.attributes.userId?.value).isEqualTo("userid")
            assertThat(actuator.attributes.friendlyName.value).isEqualTo("Text 1")
            assertThat(actuator.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(actuator.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))

            assertThat(actuator.attributes.editable).isEqualTo(false)
            assertThat(actuator.attributes.min.value).isEqualTo(0.0)
            assertThat(actuator.attributes.max.value).isEqualTo(100.0)
            assertThat(actuator.attributes.pattern.pattern).isEqualTo(Regex("[a-fA-F0-9]*").pattern)
            assertThat(actuator.attributes.mode.value).isEqualTo("text")
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
            assertThat(actuator.actualState.value).isEqualTo(LocalTime.parse("01:00:00"))

            assertThat(actuator.attributes.userId?.value).isEqualTo("userid")
            assertThat(actuator.attributes.friendlyName.value).isEqualTo("Input with only time")
            assertThat(actuator.attributes.lastChanged)
                .isEqualTo(Instant.parse("2021-06-21T01:39:48.096892+00:00"))
            assertThat(actuator.attributes.lastUpdated)
                .isEqualTo(Instant.parse("2021-06-21T02:03:54.908902+00:00"))

            assertThat(actuator.attributes.editable).isEqualTo(false)
            assertThat(actuator.attributes.timestamp).isEqualTo(3600)
        }
    }

    // Tests - Cleanup

    @AfterAll
    fun stopKoin() {
        KhomeKoinContext.application?.close()
    }

    // Private Methods

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(
        ExperimentalCoroutinesApi::class,
        ExperimentalStdlibApi::class,
        KtorExperimentalAPI::class,
        ObsoleteCoroutinesApi::class
    )
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
