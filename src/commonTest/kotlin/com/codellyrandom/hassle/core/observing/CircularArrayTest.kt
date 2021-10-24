package com.codellyrandom.hassle.core.observing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * From https://gist.github.com/ToxicBakery/05d3d98256aaae50bfbde04ae0c62dbd
 */
class CircularArrayTest {

    @Test
    fun getSize() {
        val circularArray = CircularArray<Int>(10)
        assertEquals(0, circularArray.size)

        circularArray.add(1)
        assertEquals(1, circularArray.size)

        (0..100).forEach(circularArray::add)
        assertEquals(10, circularArray.size)
    }

    @Test
    fun get() {
        CircularArray<Int>(1).also { circularArray ->
            circularArray.add(1)
            assertEquals(1, circularArray[0])

            circularArray.add(2)
            assertEquals(2, circularArray[0])
        }

        CircularArray<Int>(2).also { circularArray ->
            circularArray.add(1)
            assertEquals(1, circularArray[0])

            circularArray.add(2)
            assertEquals(1, circularArray[0])
            assertEquals(2, circularArray[1])

            circularArray.add(3)
            assertEquals(2, circularArray[0])
            assertEquals(3, circularArray[1])

            circularArray.add(4)
            assertEquals(3, circularArray[0])
            assertEquals(4, circularArray[1])
        }
    }

    @Test
    fun getZeroSizeException() {
        assertFailsWith<IndexOutOfBoundsException> {
            CircularArray<Int>(1)[0]
        }
    }

    @Test
    fun getIndexOutOfBounds() {
        assertFailsWith<IndexOutOfBoundsException> {
            CircularArray<Int>(1).also { circularArray ->
                circularArray.add(1)
                circularArray[10]
            }
        }
    }

    @Test
    fun getIndexNegative() {
        assertFailsWith<IndexOutOfBoundsException> {
            CircularArray<Int>(1).also { circularArray ->
                circularArray.add(1)
                circularArray[-1]
            }
        }
    }

    @Test
    fun toList() {
        CircularArray<Int>(1).also { circularArray ->
            circularArray.add(0)
            assertEquals(listOf(0), circularArray.toList())

            circularArray.add(1)
            assertEquals(listOf(1), circularArray.toList())
        }

        CircularArray<Int>(2).also { circularArray ->
            circularArray.add(0)
            assertEquals(listOf(0), circularArray.toList())

            circularArray.add(1)
            assertEquals(listOf(0, 1), circularArray.toList())

            circularArray.add(2)
            assertEquals(listOf(1, 2), circularArray.toList())
            println("${circularArray[0]}")

            circularArray.add(3)
            assertEquals(listOf(2, 3), circularArray.toList())

            circularArray.add(4)
            assertEquals(listOf(3, 4), circularArray.toList())
        }
    }

    @Test
    fun testClone() {
        CircularArray<Int>(1)
            .also { circularArray -> circularArray.add(1) }
            .clone()
            .also { clonedCircularArray -> assertEquals(1, clonedCircularArray[0]) }
    }
}
