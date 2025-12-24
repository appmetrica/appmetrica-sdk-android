package io.appmetrica.analytics.idsync.impl

import android.util.Base64
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONArray
import org.json.JSONObject

internal class IdSyncResultStringComposer {

    private val tag = "[EventValueComposer]"
    private val type = "type"
    private val url = "url"
    private val responseCode = "responseCode"
    private val responseBody = "responseBody"
    private val responseHeaders = "responseHeaders"

    fun compose(result: RequestResult): String = JSONObject().apply {
        put(type, result.type)
        put(url, result.url)
        put(responseCode, result.responseCode)
        put(responseBody, result.responseBody.toUtfOrBase64String())
        put(responseHeaders, result.responseHeaders.toJsonObject())
    }.toString()

    private fun Map<String, List<String>>.toJsonObject(): JSONObject {
        val result = JSONObject()
        forEach { (key, values) ->
            result.putOpt(key, JSONArray(values))
        }
        return result
    }

    private fun ByteArray.toUtfOrBase64String(): String = try {
        String(this, Charsets.UTF_8)
    } catch (e: Throwable) {
        DebugLogger.error(tag, e)
        Base64.encodeToString(this, Base64.DEFAULT)
    }
}
