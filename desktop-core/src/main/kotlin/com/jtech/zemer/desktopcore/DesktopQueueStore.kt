package com.jtech.zemer.desktopcore

import java.util.concurrent.atomic.AtomicReference

class DesktopQueueStore {
    private val queueRef = AtomicReference<List<DesktopTrack>>(emptyList())

    fun replace(items: List<DesktopTrack>) {
        queueRef.set(items)
    }

    fun queue(item: DesktopTrack) {
        val current = queueRef.get().toMutableList()
        if (current.none { it.id == item.id }) {
            current += item
        }
        queueRef.set(current)
    }

    fun remove(itemId: Long) {
        val current = queueRef.get().toMutableList()
        val next = current.filterNot { it.id == itemId }
        queueRef.set(next)
    }

    fun clear() {
        queueRef.set(emptyList())
    }

    fun next(): DesktopTrack? = queueRef.get().firstOrNull()

    fun currentQueue(): List<DesktopTrack> = queueRef.get()
}
