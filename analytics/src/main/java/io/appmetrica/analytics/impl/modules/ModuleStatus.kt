package io.appmetrica.analytics.impl.modules

import org.json.JSONObject

data class ModuleStatus(
    val moduleName: String,
    val loaded: Boolean,
) {

    fun toJsonObject(): JSONObject = try {
        JSONObject().apply {
            put(MODULE_NAME_KEY, moduleName)
            put(LOADED_KEY, loaded)
        }
    } catch (e: Throwable) {
        JSONObject()
    }

    companion object {

        private const val MODULE_NAME_KEY = "moduleName"
        private const val LOADED_KEY = "loaded"

        fun fromJsonObject(json: JSONObject): ModuleStatus {
            return try {
                ModuleStatus(
                    moduleName = json.getString(MODULE_NAME_KEY),
                    loaded = json.getBoolean(LOADED_KEY)
                )
            } catch (e: Throwable) {
                ModuleStatus(
                    moduleName = "",
                    loaded = false
                )
            }
        }
    }
}
