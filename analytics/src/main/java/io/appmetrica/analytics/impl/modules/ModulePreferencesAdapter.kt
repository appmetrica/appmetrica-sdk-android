package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage

internal class ModulePreferencesAdapter(
    private val identifier: String,
    preferences: SimplePreferenceStorage
) : BaseModulePreferencesAdapter(preferences) {

    override fun prepareKey(key: String): String = "$key-$identifier"
}
