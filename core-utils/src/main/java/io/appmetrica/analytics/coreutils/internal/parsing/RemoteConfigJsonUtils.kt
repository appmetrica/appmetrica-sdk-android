package io.appmetrica.analytics.coreutils.internal.parsing

import io.appmetrica.analytics.coreutils.internal.WrapUtils
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val FEATURES = "features"
private const val ENABLED = "enabled"
private const val LIST = "list"
private const val QUERIES = "queries"
private const val URL = "url"
private const val QUERY_HOSTS = "query_hosts"
private const val URLS = "urls"

object RemoteConfigJsonUtils {

    @JvmStatic
    fun extractFeature(input: JSONObject, name: String, fallback: Boolean): Boolean {
        try {
            input.optJSONObject(FEATURES)?.let { features ->
                features.optJSONObject(LIST)?.let { list ->
                    list.optJSONObject(name)?.let { feature ->
                        return feature.optBoolean(ENABLED, fallback)
                    }
                }
            }
        } catch (ignored: Throwable) {
        }
        return fallback
    }

    @JvmStatic
    fun extractQuery(input: JSONObject, key: String): String? {
        try {
            input.optJSONObject(QUERIES)?.let { queriesJson ->
                queriesJson.optJSONObject(LIST)?.let { listJson ->
                    listJson.optJSONObject(key)?.let { json ->
                        return json.optString(URL, "").nullIfEmpty()
                    }
                }
            }
        } catch (ignored: Throwable) {
        }
        return null
    }

    @JvmStatic
    fun extractHosts(input: JSONObject, type: String): Array<ByteArray> =
        input.optJSONObject(QUERY_HOSTS)
            ?.optJSONObject(LIST)
            ?.optJSONObject(type)
            ?.optJSONArray(URLS)
            ?.let { urls ->
                Array(urls.length()) { index ->
                    urls.optString(index).toByteArray()
                }
            } ?: emptyArray()

    @JvmStatic
    fun extractMillisFromSecondsOrDefault(input: JSONObject, key: String, defaultMillis: Long) =
        extractMillisOrDefault(input, key, TimeUnit.SECONDS, defaultMillis)

    @JvmStatic
    fun extractMillisOrDefault(input: JSONObject, key: String, timeUnit: TimeUnit, defaultMillis: Long): Long =
        WrapUtils.getMillisOrDefault(
            input.optLongOrNull(key),
            timeUnit,
            defaultMillis
        )

    private fun String.nullIfEmpty(): String? = takeUnless { it == "" }
}
