package com.codellyrandom.hassle.communicating

import com.codellyrandom.hassle.core.mapping.ObjectMapper
import com.codellyrandom.hassle.values.*
import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
