package khome.extending.serviceCalls.mediaPlayer

import khome.TestHassConnection
import khome.values.*
import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MediaPlayerTest {
    @Test
    fun playMedia() = runBlocking {
        val connection = TestHassConnection()

        connection.playMedia(MediaContentId("content/id"))

        val json = """
        {
            "domain": "media_player",
            "service": "play_media",
            "service_data": {
                "media_content_id": "content/id"
            }
        }
        """.trimIndent()
        assertEquals(json, connection.callServiceRequests.last())
    }
}
