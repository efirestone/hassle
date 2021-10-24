package com.codellyrandom.hassle.core.boot.authentication

import com.codellyrandom.hassle.core.mapping.ObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthRequestTest {
    @Test
    fun serialization() {
        val objectMapper = ObjectMapper()
        val authRequest = AuthRequest(accessToken = "abcd1234")

        val json = objectMapper.toJson(authRequest)
        val expected = """
        {
            "type": "auth",
            "access_token": "abcd1234"
        }
        """.trimIndent()

        assertEquals(expected, json)
    }
}
