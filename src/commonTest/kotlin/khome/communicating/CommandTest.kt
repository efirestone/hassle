package khome.communicating

import khome.core.mapping.ObjectMapper
import khome.values.*
import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandTest {
    @Test
    fun playMedia() = runBlocking {
        val command = PlayMediaServiceCommand(
            EntityId.fromString("media_player.living_room"),
            MediaContentId("content/id"),
        )

        val json = ObjectMapper().toJson(command)

        val expectedJson = """
        {
            "domain": "media_player",
            "service": "play_media",
            "type": "call_service",
            "target": {
                "entity_id": "media_player.living_room"
            },
            "service_data": {
                "media_content_id": "content/id"
            }
        }
        """.trimIndent()
        assertEquals(expectedJson, json)
    }
}
