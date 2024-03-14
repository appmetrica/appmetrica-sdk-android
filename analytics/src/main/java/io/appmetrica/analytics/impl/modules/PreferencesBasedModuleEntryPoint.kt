package io.appmetrica.analytics.impl.modules

import android.content.Context

data class PreferencesBasedModuleEntryPoint(
    private val context: Context,
    private val prefName: String,
    private val prefValueName: String
) : ModuleEntryPointProvider {

    override val className: String
        get() = context
            .getSharedPreferences(prefName, Context.MODE_PRIVATE)
            .getString(prefValueName, "") ?: ""
}
