package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONArray
import org.json.JSONObject

data class ModulesStatus(
    val modulesStatus: List<ModuleStatus>,
    val lastSendTime: Long
) {

    fun toJson(): String = try {
        JSONObject().apply {
            put(MODULES_STATUS_KEY, JSONArray(modulesStatus.map { it.toJsonObject() }))
            put(LAST_SEND_TIME_KEY, lastSendTime)
        }.toString()
    } catch (e: Throwable) {
        DebugLogger.error(TAG, "toJson: $e")
        ""
    }

    companion object {

        private const val TAG = "[ModulesStatus]"

        private const val MODULES_STATUS_KEY = "modulesStatus"
        private const val LAST_SEND_TIME_KEY = "lastSendTime"

        fun fromJson(json: String): ModulesStatus {
            try {
                val jsonObject = JSONObject(json)
                DebugLogger.info("ModulesStatus", "fromJson: $json")
                return ModulesStatus(
                    modulesStatus = jsonObject.getJSONArray(MODULES_STATUS_KEY).let { statuses ->
                        (0 until statuses.length()).map {
                            ModuleStatus.fromJsonObject(statuses.getJSONObject(it))
                        }
                    },
                    lastSendTime = jsonObject.getLong(LAST_SEND_TIME_KEY)
                )
            } catch (e: Throwable) {
                DebugLogger.error(TAG, "fromJson: $e")
                return ModulesStatus(emptyList(), 0)
            }
        }
    }
}
