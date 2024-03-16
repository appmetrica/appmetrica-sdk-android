package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences

internal abstract class BaseModulePreferencesAdapter(
    private val preferences: SimplePreferenceStorage
) : ModulePreferences {

    override fun putString(key: String, value: String?) {
        preferences.putString(prepareKey(key), value).commit()
    }

    override fun getString(key: String, fallback: String?): String? = preferences.getString(prepareKey(key), fallback)

    override fun putLong(key: String, value: Long) {
        preferences.putLong(prepareKey(key), value).commit()
    }

    override fun getLong(key: String, fallback: Long): Long = preferences.getLong(prepareKey(key), fallback)

    override fun putBoolean(key: String, value: Boolean) {
        preferences.putBoolean(prepareKey(key), value).commit()
    }

    override fun getBoolean(key: String, fallback: Boolean): Boolean = preferences.getBoolean(key, fallback)

    override fun putInt(key: String, value: Int) {
        preferences.putInt(key, value).commit()
    }

    override fun getInt(key: String, fallback: Int): Int = preferences.getInt(key, fallback)

    protected abstract fun prepareKey(key: String): String
}
