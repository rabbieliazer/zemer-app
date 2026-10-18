package com.jtech.zemer.desktopcore

import java.io.File
import java.util.Properties

class DesktopSettingsStore(private val file: File) {
    private val properties = Properties()

    init {
        file.parentFile?.mkdirs()
        if (file.exists()) {
            file.inputStream().use(properties::load)
        }
    }

    fun getString(key: String, default: String): String {
        return properties.getProperty(key, default)
    }

    fun setString(key: String, value: String) {
        properties.setProperty(key, value)
        save()
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        return properties.getProperty(key)?.toBooleanStrictOrNull() ?: default
    }

    fun setBoolean(key: String, value: Boolean) {
        properties.setProperty(key, value.toString())
        save()
    }

    private fun save() {
        file.outputStream().use { stream ->
            properties.store(stream, "Zemer Desktop settings")
        }
    }
}
