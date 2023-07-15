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
                            "config_entry_id": "541e0e1b4dac48d9aff5a07dd3076983",
                            "device_id": "64865b11cf832da0f6f24ab7a5233f27",
                            "disabled_by": null,
                            "entity_category": null,
                            "entity_id": "sensor.sun_next_dusk",
                            "has_entity_name": true,
                            "hidden_by": null,
                            "icon": null,
                            "id": "30f74e48d3807d958e2b39dae3247904",
                            "name": null,
                            "options":
                            {
                                "conversation":
                                {
                                    "should_expose": false
                                }
                            },
                            "original_name": "Next dusk",
                            "platform": "sun",
                            "translation_key": "next_dusk",
                            "unique_id": "541e0e1b4dac48d9aff5a07dd3076983-next_dusk"
                        }
                    ]
                }
            ]
            """.trimIndent(),
            forCommandType = "config/entity_registry/list"
        )

        val registrations = client.getEntityRegistrations()
        val expectedRegistrations = listOf(
            EntityRegistration(
                configEntryId = ConfigEntryId(value = ""),
                deviceId = DeviceId(value = ""),
                entityId = EntityId(
                    domain = Domain(value = ""),
                    objectId = ObjectId(value = "")
                ),
                id = "",
                originalName = "",
                platform = "",
                uniqueId = ""
            )
        )
        assertEquals(expectedRegistrations, registrations)
    }
}
