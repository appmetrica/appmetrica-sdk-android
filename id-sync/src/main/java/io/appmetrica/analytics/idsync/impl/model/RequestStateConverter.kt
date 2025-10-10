package io.appmetrica.analytics.idsync.impl.model

import io.appmetrica.analytics.coreapi.internal.data.Converter
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONArray
import org.json.JSONObject

internal class RequestStateConverter : Converter<List<RequestState>?, String?> {

    private val tag = "[RequestStateConverter]"

    private val requestStateKey = "request_state"
    private val typeKey = "type"
    private val lastAttemptKey = "last_attempt"
    private val prevAttemptResultKey = "prev_attempt_result"

    override fun fromModel(value: List<RequestState>?): String {
        val json = JSONObject().apply {
            put(
                requestStateKey,
                JSONArray(value?.map { it.toJson() } ?: emptyList<JSONObject>())
            )
        }
        return json.toString()
    }

    override fun toModel(value: String?): List<RequestState> = try {
        val result = mutableListOf<RequestState>()
        value?.let { json ->
            JSONObject(json).optJSONArray(requestStateKey)?.let { jsonArray ->
                for (i in 0 until jsonArray.length()) {
                    jsonArray.optJSONObject(i)?.toRequestState()?.let { result.add(it) }
                }
            }
        }
        result
    } catch (e: Throwable) {
        DebugLogger.error(tag, e)
        emptyList()
    }

    private fun RequestState.toJson(): JSONObject = try {
        JSONObject().apply {
            put(typeKey, type)
            put(lastAttemptKey, lastAttempt)
            put(prevAttemptResultKey, lastAttemptResult.value)
        }
    } catch (e: Throwable) {
        DebugLogger.error(tag, e)
        JSONObject()
    }

    private fun JSONObject.toRequestState(): RequestState? = try {
        RequestState(
            getString(typeKey),
            getLong(lastAttemptKey),
            RequestAttemptResult.fromString(getString(prevAttemptResultKey))
        )
    } catch (e: Throwable) {
        DebugLogger.error(tag, e)
        null
    }
}
