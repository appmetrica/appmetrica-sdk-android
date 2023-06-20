package io.appmetrica.analytics.modulesapi.internal

/**
 * Only for preferences stored before module separating.
 */
interface LegacyModulePreferences {

    fun putLegacyString(key: String, value: String?)
    fun getLegacyString(key: String, fallback: String? = null): String?

    fun putLegacyLong(key: String, value: Long)
    fun getLegacyLong(key: String, fallback: Long = 0L): Long

    fun putLegacyBoolean(key: String, value: Boolean)
    fun getLegacyBoolean(key: String, fallback: Boolean): Boolean
}
