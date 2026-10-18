package com.jtech.zemer.desktopcore

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DesktopQueueStoreTest {
    @Test
    fun `queue add and next semantics`() {
        val store = DesktopQueueStore()
        val t1 = DesktopTrack(1, "A", "X", "", "")
        val t2 = DesktopTrack(2, "B", "Y", "", "")

        assertTrue(store.currentQueue().isEmpty())
        store.queue(t1)
        assertEquals(1, store.currentQueue().size)
        store.queue(t1) // duplicate should not be added
        assertEquals(1, store.currentQueue().size)
        store.queue(t2)
        assertEquals(2, store.currentQueue().size)

        val next = store.next()
        assertNotNull(next)
        assertEquals(t1.id, next?.id)
    }
}
