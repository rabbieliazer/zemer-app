package com.jtech.zemer.desktopcore

import java.io.File

class DesktopStorageStub : StorageBackend {
    private val base = File(System.getProperty("java.io.tmpdir"), "zemer-desktop")
    init { base.mkdirs() }

    override fun saveFile(path: String, bytes: ByteArray): Boolean {
        return try {
            val f = File(base, path)
            f.parentFile?.mkdirs()
            f.writeBytes(bytes)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun readFile(path: String): ByteArray? {
        return try {
            val f = File(base, path)
            if (!f.exists()) null else f.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
