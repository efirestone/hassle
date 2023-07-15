package com.codellyrandom.hassle.communicating

import DeviceId
import com.codellyrandom.hassle.TestHomeAssistantApiClient
import com.codellyrandom.hassle.extending.commands.getEntityRegistrations
import com.codellyrandom.hassle.values.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigCommandTest {
    @Test
    fun parseListEntityRegistrationCommand() = runBlocking {
        val client = TestHomeAssistantApiClient()
        client.setResponse(
            """
            [
                {
                    "id": 38,
                    "type": "result",
                    "success": true,
                    "result":
                    [
                        {
                            "area_id": null,
                            "config_entry_id": "541e0e1b4dac48d9aff5a07dd3076983",
                            "device_id": "64865b11cf832da0f6f24ab7a5233f27",
                            "disabled_by": null,
                            "entity_category": null,
                            "entity_id": "sensor.sun_next_dawn",
                            "has_entity_name": true,
                            "hidden_by": null,
                            "icon": null,
                            "id": "5080fab30439f8b345836bb0721a9762",
                            "name": null,
                            "options":
                            {
                                "conversation":
                                {
                                    "should_expose": false
                                }
                            },
                            "original_name": "Next dawn",
                            "platform": "sun",
                            "translation_key": "next_dawn",
                            "unique_id": "541e0e1b4dac48d9aff5a07dd3076983-next_dawn"
                        },
                        {
                            "area_id": null,
                            "config_entry_id": null,
                            "device_id": null,
                            "disabled_by": null,
                            "entity_category": null,
                            "entity_id": "light.virtual_light_1",
                            "has_entity_name": false,
                            "hidden_by": null,
                            "icon": null,
                            "id": "793330a1acc7846fa7d2557eac4d8e8d",
                            "name": null,
                            "options":
                            {
                                "conversation":
                                {
                                    "should_expose": true
                                }
                            },
                            "original_name": "Light 1",
                            "platform": "virtual",
                            "translation_key": null,
                            "unique_id": "light_1"
                        }
                    ]
                }
            ]
            """.trimIndent(),
            forCommandType = "config/entity_registry/list",
        )

        val registrations = client.getEntityRegistrations()
        val expectedRegistrations = listOf(
            EntityRegistration(
                configEntryId = ConfigEntryId("541e0e1b4dac48d9aff5a07dd3076983"),
                deviceId = DeviceId("64865b11cf832da0f6f24ab7a5233f27"),
                entityId = EntityId(domain = Domain("sensor"), objectId = ObjectId("sun_next_dawn")),
                id = "5080fab30439f8b345836bb0721a9762",
                originalName = "Next dawn",
                platform = "sun",
                uniqueId = "541e0e1b4dac48d9aff5a07dd3076983-next_dawn",
            ),
            EntityRegistration(
                configEntryId = null,
                deviceId = null,
                entityId = EntityId(domain = Domain("light"), objectId = ObjectId("virtual_light_1")),
                id = "793330a1acc7846fa7d2557eac4d8e8d",
                originalName = "Light 1",
                platform = "virtual",
                uniqueId = "light_1",
            ),
        )
        assertEquals(expectedRegistrations, registrations)
    }
}
