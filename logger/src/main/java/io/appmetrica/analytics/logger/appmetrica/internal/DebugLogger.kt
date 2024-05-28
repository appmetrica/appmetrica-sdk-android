package io.appmetrica.analytics.logger.appmetrica.internal

import io.appmetrica.analytics.logger.common.BaseDebugLogger
import org.json.JSONObject

object DebugLogger : BaseDebugLogger("AppMetricaDebug") {

    private const val JSON_INDENT_SPACES = 2
    private const val DUMP_EXCEPTION_MESSAGE = "Exception during dumping JSONObject"

    fun dumpJson(tag: String, jsonObject: JSONObject) {
        val message = try {
            jsonObject.toString(JSON_INDENT_SPACES)
        } catch (e: Throwable) {
            DUMP_EXCEPTION_MESSAGE
        }
        info(tag, message)
    }
}
