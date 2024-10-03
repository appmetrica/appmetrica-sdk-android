package io.appmetrica.analytics.coreutils.internal.parsing

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONArray
import org.json.JSONObject

object JsonUtils {

    private const val TAG = "[JsonUtils]"

    @JvmStatic
    fun JSONObject?.optLongOrDefault(key: String, fallback: Long?): Long? =
        optLongOrNull(key) ?: fallback

    @JvmStatic
    fun JSONObject?.optLongOrNull(key: String): Long? {
        if (this?.has(key) == true) {
            try {
                return this.getLong(key)
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e, e.message)
            }
        }
        return null
    }

    @JvmStatic
    fun JSONObject?.optFloatOrDefault(key: String, fallback: Float): Float =
        this.optFloatOrNull(key) ?: fallback

    @JvmStatic
    fun JSONObject?.optFloatOrNull(key: String): Float? {
        if (this?.has(key) == true) {
            try {
                return this.getDouble(key).toFloat()
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e, e.message)
            }
        }
        return null
    }

    @JvmStatic
    fun JSONObject?.optStringOrNullable(key: String, fallback: String?): String? =
        this.optStringOrNull(key) ?: fallback

    @JvmStatic
    fun JSONObject?.optStringOrNull(key: String): String? {
        if (this?.has(key) == true) {
            try {
                return this.getString(key)
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e, e.message)
            }
        }
        return null
    }

    @JvmStatic
    fun JSONObject?.optBooleanOrDefault(key: String, fallback: Boolean): Boolean =
        this.optBooleanOrNull(key) ?: fallback

    @JvmStatic
    fun JSONObject?.optBooleanOrNullable(key: String, fallback: Boolean?): Boolean? =
        this.optBooleanOrNull(key) ?: fallback

    @JvmStatic
    fun JSONObject?.optBooleanOrNull(key: String): Boolean? {
        if (this?.has(key) == true) {
            try {
                return this.getBoolean(key)
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e, e.message)
            }
        }
        return null
    }

    @JvmStatic
    fun JSONObject?.optJsonObjectOrDefault(key: String, fallback: JSONObject): JSONObject =
        this.optJsonObjectOrNull(key) ?: fallback

    @JvmStatic
    fun JSONObject?.optJsonObjectOrNullable(key: String, fallback: JSONObject?): JSONObject? =
        this.optJsonObjectOrNull(key) ?: fallback

    @JvmStatic
    fun JSONObject?.optJsonObjectOrNull(key: String): JSONObject? = this?.optJSONObject(key)

    @JvmStatic
    fun JSONObject.isEqualTo(value: JSONObject): Boolean {
        if (keys().asSequence().toSet() != value.keys().asSequence().toSet()) {
            return false
        }
        return keys().asSequence().all { key ->
            val first = get(key)
            val second = value.get(key)
            when {
                (first is JSONObject) -> {
                    if (second is JSONObject) {
                        first.isEqualTo(second)
                    } else {
                        false
                    }
                }
                (first is JSONArray) -> {
                    if (second is JSONArray) {
                        first.isEqualTo(second)
                    } else {
                        false
                    }
                }
                else -> first.equals(second)
            }
        }
    }

    @JvmStatic
    fun JSONArray.isEqualTo(value: JSONArray): Boolean {
        if (length() != value.length()) {
            return false
        }
        return (0 until length()).all { index ->
            val first = get(index)
            val second = value.get(index)
            when {
                (first is JSONObject) -> {
                    if (second is JSONObject) {
                        first.isEqualTo(second)
                    } else {
                        false
                    }
                }
                (first is JSONArray) -> {
                    if (second is JSONArray) {
                        first.isEqualTo(second)
                    } else {
                        false
                    }
                }
                else -> first.equals(second)
            }
        }
    }

    @JvmStatic
    fun JSONObject?.optHexByteArray(
        key: String,
        fallback: ByteArray? = null
    ): ByteArray? {
        return this?.optStringOrNull(key)?.let {
            try {
                StringUtils.hexToBytes(it)
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e, "Fail to decode hex string: $it")
                null
            }
        } ?: fallback
    }
}
