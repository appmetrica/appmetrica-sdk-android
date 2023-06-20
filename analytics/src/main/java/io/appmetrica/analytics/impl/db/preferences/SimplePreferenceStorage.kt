package io.appmetrica.analytics.impl.db.preferences

interface SimplePreferenceStorage {

    fun putString(key: String, value: String?): SimplePreferenceStorage

    fun getString(key: String, fallback: String?): String?

    fun putInt(key: String, value: Int): SimplePreferenceStorage

    fun getInt(key: String, fallback: Int): Int

    fun putLong(key: String, value: Long): SimplePreferenceStorage

    fun getLong(key: String, fallback: Long): Long

    fun putBoolean(key: String, value: Boolean): SimplePreferenceStorage

    fun getBoolean(key: String, fallback: Boolean): Boolean

    fun remove(key: String): SimplePreferenceStorage

    fun contains(key: String): Boolean

    fun commit()
}
