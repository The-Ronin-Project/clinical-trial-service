package com.projectronin.clinical.trial.server.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ReflectionTest {
    @Test
    fun `copy - IllegalStateException thrown when instance has no copy method`() {
        class NotADataClass(val value: String)

        val instance = NotADataClass("Test")
        val exception = assertThrows<IllegalStateException> { copy(instance, mapOf("value" to "New Test")) }
        assertEquals(
            "Supplied instance does not represent a data class and cannot be copied",
            exception.message,
        )
    }

    @Test
    fun `copy - creates a new instance when no values are changed`() {
        val instance = SimpleDataClass("value1", 2, "not null")
        val copied = copy(instance, mapOf())
        assertEquals(instance, copied)
        assertTrue(instance !== copied)
    }

    @Test
    fun `copy - creates a new instance when some values are changed`() {
        val instance = SimpleDataClass("value1", 2, "not null")
        val copied = copy(instance, mapOf("value1" to "new value1", "value2" to 200))
        assertNotEquals(instance, copied)
        assertEquals("new value1", copied.value1)
        assertEquals(200, copied.value2)
        assertEquals("not null", copied.nullable)
    }

    @Test
    fun `copy - creates a new instance when all values are changed`() {
        val instance = SimpleDataClass("value1", 2, "not null")
        val copied = copy(instance, mapOf("value1" to "new value1", "value2" to 200, "nullable" to null))
        assertNotEquals(instance, copied)
        assertEquals("new value1", copied.value1)
        assertEquals(200, copied.value2)
        assertNull(copied.nullable)
    }

    data class SimpleDataClass(
        val value1: String,
        val value2: Int,
        val nullable: String?,
    )
}
