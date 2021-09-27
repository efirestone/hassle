package khome.core.observing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class CircularBufferTest {

    @Test
    fun `youngest element first in order`() {
        val sut = CircularBuffer<Int>(10)

        (1..10).forEach {
            sut.addFirst(it)
        }

        assertEquals(10, sut.snapshot[0])
        assertEquals(1, sut.snapshot[9])
    }

    @Test
    fun `max capacity constraints buffer size`() {
        val sut = CircularBuffer<Int>(100)

        (1..100).forEach {
            sut.addFirst(it)
        }

        assertFailsWith<IndexOutOfBoundsException> {
            sut.snapshot[100]
        }
    }

    @Test
    fun `last() returns null on empty buffer`() {
        val sut = CircularBuffer<Int>(10)
        assertNull(sut.first)
        assertNull(sut.last)
    }
}
