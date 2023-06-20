package io.appmetrica.analytics.modulesapi.internal

interface ModulePreferences {

    fun putString(key: String, value: String?)
    fun getString(key: String, fallback: String? = null): String?

    fun putLong(key: String, value: Long)
    fun getLong(key: String, fallback: Long = 0L): Long

    fun putInt(key: String, value: Int)
    fun getInt(key: String, fallback: Int = 0): Int

    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, fallback: Boolean): Boolean
}
