@file:JvmName("JsonUtils")

package io.appmetrica.analytics.coreutils.internal.parsing

import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import org.json.JSONObject

fun JSONObject?.optLongOrDefault(key: String, fallback: Long?): Long? = optLongOrNull(key) ?: fallback

fun JSONObject?.optLongOrNull(key: String): Long? {
    if (this?.has(key) == true) {
        try {
            return this.getLong(key)
        } catch (e: Throwable) {
            YLogger.e(e, e.message)
        }
    }
    return null
}

fun JSONObject?.optFloatOrDefault(key: String, fallback: Float): Float = this.optFloatOrNull(key) ?: fallback

fun JSONObject?.optFloatOrNull(key: String): Float? {
    if (this?.has(key) == true) {
        try {
            return this.getDouble(key).toFloat()
        } catch (e: Throwable) {
            YLogger.e(e, e.message)
        }
    }
    return null
}

fun JSONObject?.optStringOrNullable(key: String, fallback: String?): String? =
    this.optStringOrNull(key) ?: fallback

fun JSONObject?.optStringOrNull(key: String): String? {
    if (this?.has(key) == true) {
        try {
            return this.getString(key)
        } catch (e: Throwable) {
            YLogger.e(e, e.message)
        }
    }
    return null
}

fun JSONObject?.optBooleanOrDefault(key: String, fallback: Boolean): Boolean = this.optBooleanOrNull(key) ?: fallback

fun JSONObject?.optBooleanOrNullable(key: String, fallback: Boolean?): Boolean? =
    this.optBooleanOrNull(key) ?: fallback

fun JSONObject?.optBooleanOrNull(key: String): Boolean? {
    if (this?.has(key) == true) {
        try {
            return this.getBoolean(key)
        } catch (e: Throwable) {
            YLogger.e(e, e.message)
        }
    }
    return null
}

fun JSONObject?.optJsonObjectOrDefault(key: String, fallback: JSONObject): JSONObject =
    this.optJsonObjectOrNull(key) ?: fallback

fun JSONObject?.optJsonObjectOrNullable(key: String, fallback: JSONObject?): JSONObject? =
    this.optJsonObjectOrNull(key) ?: fallback

fun JSONObject?.optJsonObjectOrNull(key: String): JSONObject? = this?.optJSONObject(key)
