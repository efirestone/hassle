package khome.communicating

import khome.TestHomeAssistantApiClient
import khome.values.*
import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceCommandTest {
    @Test
    fun playMedia() = runBlocking {
        val connection = TestHomeAssistantApiClient()

        connection.callService2(
            PlayMediaServiceCommand(
                MediaContentId("content/id"),
                EntityId.fromString("media_player.living_room")
            )
        )

        val json = """
        {
            "type": "call_service",
            "id": null,
            "domain": "media_player",
            "service": "play_media",
            "service_data": {
                "media_content_id": "content/id"
            },
            "target": {
                "entity_id": "media_player.living_room"
            }
        }
        """.trimIndent()
        assertEquals(json, connection.callServiceRequests.last())
    }
}
