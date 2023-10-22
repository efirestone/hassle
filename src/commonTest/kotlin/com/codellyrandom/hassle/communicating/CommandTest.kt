package com.codellyrandom.hassle.communicating

import com.codellyrandom.hassle.Command
import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.values.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CommandTest {
    @Test
    fun playMedia() = runBlocking {
        val command = PlayMediaServiceCommand(
            EntityId.fromString("media_player.living_room"),
            MediaContentType.MOVIE,
            MediaContentId("content/id"),
        )
        command.id = 1

        val json = ObjectMapper().toJson(command)

        val expectedJson = """
        {
            "id": 1,
            "domain": "media_player",
            "service": "play_media",
            "type": "call_service",
            "target": {
                "entity_id": "media_player.living_room"
            },
            "service_data": {
                "media_content_type": "MOVIE",
                "media_content_id": "content/id"
            }
        }
        """.trimIndent()
        assertEquals(expectedJson, json)
    }

    @Test
    fun serializeCommands() {
        allCommands().forEach { commandClass: KClass<out Command> ->
            when (commandClass) {
                CloseCoverServiceCommand::class -> {
                    serializeCommand(
                        command = CloseCoverServiceCommand(
                            target = EntityId.fromString("cover.foo"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "cover",
                            "service": "close_cover",
                            "type": "call_service",
                            "target": {
                                "entity_id": "cover.foo"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                ConfigEntityRegistrationListCommand::class -> {
                    serializeCommand(
                        command = ConfigEntityRegistrationListCommand(id = 1),
                        expectedJson = """
                        {
                            "id": 1,
                            "type": "config/entity_registry/list"
                        }
                        """.trimIndent(),
                    )
                }
                CreatePersistentNotificationServiceCommand::class -> {
                    serializeCommand(
                        command = CreatePersistentNotificationServiceCommand(
                            serviceData = CreatePersistentNotificationServiceCommand.ServiceData(
                                title = "Title",
                                message = "Notification",
                                notificationId = "id",
                            ),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "persistent_notification",
                            "service": "create",
                            "type": "call_service",
                            "service_data": {
                                "title": "Title",
                                "message": "Notification",
                                "notification_id": "id"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                DismissPersistentNotificationServiceCommand::class -> {
                    serializeCommand(
                        command = DismissPersistentNotificationServiceCommand(
                            serviceData = DismissPersistentNotificationServiceCommand.ServiceData(
                                notificationId = "id",
                            ),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "persistent_notification",
                            "service": "dismiss",
                            "type": "call_service",
                            "service_data": {
                                "notification_id": "id"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                GetConfigEntriesCommand::class -> {
                    serializeCommand(
                        command = GetConfigEntriesCommand(id = 1),
                        expectedJson = """
                        {
                            "id": 1,
                            "type": "config_entries/get"
                        }
                        """.trimIndent(),
                    )
                }
                GetServicesCommand::class -> {
                    serializeCommand(
                        command = GetServicesCommand(id = 1),
                        expectedJson = """
                        {
                            "id": 1,
                            "type": "get_services"
                        }
                        """.trimIndent(),
                    )
                }
                GetStatesCommand::class -> {
                    serializeCommand(
                        command = GetStatesCommand(id = 1),
                        expectedJson = """
                        {
                            "id": 1,
                            "type": "get_states"
                        }
                        """.trimIndent(),
                    )
                }
                MarkReadPersistentNotificationServiceCommand::class -> {
                    serializeCommand(
                        command = MarkReadPersistentNotificationServiceCommand(
                            serviceData = MarkReadPersistentNotificationServiceCommand.ServiceData(
                                notificationId = "id",
                            ),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "persistent_notification",
                            "service": "mark_read",
                            "type": "call_service",
                            "service_data": {
                                "notification_id": "id"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                MuteVolumeServiceCommand::class -> {
                    serializeCommand(
                        command = MuteVolumeServiceCommand(
                            target = EntityId.fromString("media_player.foo"),
                            isMuted = Mute.FALSE,
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "media_player",
                            "service": "volume_mute",
                            "type": "call_service",
                            "target": {
                                "entity_id": "media_player.foo"
                            },
                            "service_data": {
                                "is_volume_muted": false
                            }
                        }
                        """.trimIndent(),
                    )
                }
                OpenCoverServiceCommand::class -> {
                    serializeCommand(
                        command = OpenCoverServiceCommand(
                            target = EntityId.fromString("cover.foo"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "cover",
                            "service": "open_cover",
                            "type": "call_service",
                            "target": {
                                "entity_id": "cover.foo"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                PauseMediaServiceCommand::class -> {
                    serializeCommand(
                        command = PauseMediaServiceCommand(
                            target = EntityId.fromString("media_player.foo"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "media_player",
                            "service": "media_pause",
                            "type": "call_service",
                            "target": {
                                "entity_id": "media_player.foo"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                PlayMediaServiceCommand::class -> {
                    serializeCommand(
                        command = PlayMediaServiceCommand(
                            target = EntityId.fromString("media_player.foo"),
                            mediaContentId = MediaContentId(value = "content_id"),
                            mediaContentType = MediaContentType.MOVIE,
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "media_player",
                            "service": "play_media",
                            "type": "call_service",
                            "target": {
                                "entity_id": "media_player.foo"
                            },
                            "service_data": {
                                "media_content_type": "MOVIE",
                                "media_content_id": "content_id"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                ResumeMediaServiceCommand::class -> {
                    serializeCommand(
                        command = ResumeMediaServiceCommand(
                            target = EntityId.fromString("media_player.foo"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "media_player",
                            "service": "media_play",
                            "type": "call_service",
                            "target": {
                                "entity_id": "media_player.foo"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SelectOptionServiceCommand::class -> {
                    serializeCommand(
                        command = SelectOptionServiceCommand(
                            target = EntityId.fromString("select.foo"),
                            option = Option("bar"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "input_select",
                            "service": "select_option",
                            "type": "call_service",
                            "target": {
                                "entity_id": "select.foo"
                            },
                            "service_data": {
                                "option": "bar"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SendNotificationServiceCommand::class -> {
                    serializeCommand(
                        command = SendNotificationServiceCommand(
                            device = Device("device.foo"),
                            title = "Title",
                            message = "Message",
                            messageBuilder = {},
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "notify",
                            "service": "device.foo",
                            "type": "call_service",
                            "service_data": {
                                "title": "Title",
                                "message": "Message",
                                "data": {
                                    "push": {
                                        "thread-id": null,
                                        "sound": null,
                                        "badge": null,
                                        "category": null
                                    },
                                    "apnsHeaders": null,
                                    "presentationOptions": null,
                                    "attachment": null,
                                    "actionData": null,
                                    "entityId": null
                                }
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SetCoverPositionServiceCommand::class -> {
                    serializeCommand(
                        command = SetCoverPositionServiceCommand(
                            target = EntityId.fromString("cover.foo"),
                            position = Position(value = 50),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "cover",
                            "service": "set_cover_position",
                            "type": "call_service",
                            "target": {
                                "entity_id": "cover.foo"
                            },
                            "service_data": {
                                "position": 50
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SetDateTimeServiceCommand::class -> {
                    serializeCommand(
                        command = SetDateTimeServiceCommand(
                            target = EntityId.fromString("date.foo"),
                            date = Instant.DISTANT_PAST,
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "input_datetime",
                            "service": "set_datetime",
                            "type": "call_service",
                            "target": {
                                "entity_id": "date.foo"
                            },
                            "service_data": {
                                "date": "-100001-12-31T23:59:59.999999999Z"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SetHvacPresetModeServiceCommand::class -> {
                    serializeCommand(
                        command = SetHvacPresetModeServiceCommand(
                            target = EntityId.fromString("climate.foo"),
                            presetMode = PresetMode(value = "heat"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "climate",
                            "service": "set_preset_mode",
                            "type": "call_service",
                            "target": {
                                "entity_id": "climate.foo"
                            },
                            "service_data": {
                                "preset_mode": "heat"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SetMediaSourceServiceCommand::class -> {
                    serializeCommand(
                        command = SetMediaSourceServiceCommand(
                            target = EntityId.fromString("media_player.foo"),
                            source = MediaSource(value = "spotify"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "media_player",
                            "service": "select_source",
                            "type": "call_service",
                            "target": {
                                "entity_id": "media_player.foo"
                            },
                            "service_data": {
                                "source": "spotify"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SetSeekPositionServiceCommand::class -> {
                    serializeCommand(
                        command = SetSeekPositionServiceCommand(
                            target = EntityId.fromString("media_player.foo"),
                            seekPosition = MediaPosition(value = 0.0),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "media_player",
                            "service": "seek_position",
                            "type": "call_service",
                            "target": {
                                "entity_id": "media_player.foo"
                            },
                            "service_data": {
                                "seek_position": 0.0
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SetTemperatureServiceCommand::class -> {
                    serializeCommand(
                        command = SetTemperatureServiceCommand(
                            target = EntityId.fromString("climate.foo"),
                            temperature = Temperature(value = 80.0),
                            hvacMode = HvacMode(value = "heat"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "climate",
                            "service": "set_temperature",
                            "type": "call_service",
                            "target": {
                                "entity_id": "climate.foo"
                            },
                            "service_data": {
                                "temperature": {
                                    "value": 80.0
                                },
                                "hvac_mode": "heat"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SetValueServiceCommand::class -> {
                    serializeCommand(
                        command = SetValueServiceCommand(
                            target = EntityId.fromString("value.foo"),
                            value = 5,
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "value",
                            "service": "set_value",
                            "type": "call_service",
                            "target": {
                                "entity_id": "value.foo"
                            },
                            "service_data": {
                                "value": 5
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SetVolumeServiceCommand::class -> {
                    serializeCommand(
                        command = SetVolumeServiceCommand(
                            target = EntityId.fromString("media_player.foo"),
                            volumeLevel = VolumeLevel(50.0),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "media_player",
                            "service": "volume_set",
                            "type": "call_service",
                            "target": {
                                "entity_id": "media_player.foo"
                            },
                            "service_data": {
                                "volume_level": 0.5
                            }
                        }
                        """.trimIndent(),
                    )
                }
                SubscribeEventsCommand::class -> {
                    serializeCommand(
                        command = SubscribeEventsCommand(
                            id = 1,
                            eventType = EventType(value = "event"),
                        ),
                        expectedJson = """
                        {
                            "id": 1,
                            "event_type": "event",
                            "type": "subscribe_events"
                        }
                        """.trimIndent(),
                    )
                }
                TurnOffServiceCommand::class -> {
                    serializeCommand(
                        command = TurnOffServiceCommand(
                            target = EntityId.fromString("light.foo"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "light",
                            "service": "turn_off",
                            "type": "call_service",
                            "target": {
                                "entity_id": "light.foo"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                TurnOnLightServiceCommand::class -> {
                    serializeCommand(
                        command = TurnOnLightServiceCommand(
                            target = EntityId.fromString("light.foo"),
                            serviceData = TurnOnLightServiceCommand.ServiceData(
                                brightness = Brightness(50),
                            ),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "light",
                            "service": "turn_on",
                            "type": "call_service",
                            "target": {
                                "entity_id": "light.foo"
                            },
                            "service_data": {
                                "brightness": 50,
                                "color_name": null,
                                "color_temp": null,
                                "hs_color": null,
                                "kelvin": null,
                                "rgb_color": null,
                                "xy_color": null
                            }
                        }
                        """.trimIndent(),
                    )
                }
                TurnOnServiceCommand::class -> {
                    serializeCommand(
                        command = TurnOnServiceCommand(
                            target = EntityId.fromString("light.foo"),
                        ),
                        expectedJson = """
                        {
                            "id": null,
                            "domain": "light",
                            "service": "turn_on",
                            "type": "call_service",
                            "target": {
                                "entity_id": "light.foo"
                            }
                        }
                        """.trimIndent(),
                    )
                }
                else -> {
                    fail("No test for class $commandClass")
                }
            }
        }
    }

    private inline fun <reified CommandType : Command> serializeCommand(
        command: CommandType,
        expectedJson: String,
    ) {
        val json = ObjectMapper().toJson(command)
        assertEquals(json, expectedJson)
    }
}

fun allCommands(): List<KClass<out Command>> = listOf(
    ConfigEntityRegistrationListCommand::class,
    CloseCoverServiceCommand::class,
    CreatePersistentNotificationServiceCommand::class,
    DismissPersistentNotificationServiceCommand::class,
    GetConfigEntriesCommand::class,
    GetServicesCommand::class,
    GetStatesCommand::class,
    MarkReadPersistentNotificationServiceCommand::class,
    MuteVolumeServiceCommand::class,
    OpenCoverServiceCommand::class,
    PauseMediaServiceCommand::class,
    PlayMediaServiceCommand::class,
    ResumeMediaServiceCommand::class,
    SelectOptionServiceCommand::class,
    SendNotificationServiceCommand::class,
    SetCoverPositionServiceCommand::class,
    SetDateTimeServiceCommand::class,
    SetHvacPresetModeServiceCommand::class,
    SetMediaSourceServiceCommand::class,
    SetSeekPositionServiceCommand::class,
    SetTemperatureServiceCommand::class,
    SetValueServiceCommand::class,
    SetVolumeServiceCommand::class,
    SubscribeEventsCommand::class,
    TurnOffServiceCommand::class,
    TurnOnLightServiceCommand::class,
    TurnOnServiceCommand::class,
)
