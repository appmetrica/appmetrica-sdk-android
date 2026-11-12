package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optDoubleOrNull
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optLongOrNull
import io.appmetrica.analytics.impl.startup.parsing.JsonResponseKey
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject
import kotlin.math.floor
import kotlin.math.min

internal data class ClientReportsQueueBudgetConfig private constructor(
    val maxSizeMiB: Long,
    val heapFraction: Double,
) {

    /**
     * @return byte budget limit, or `null` when budget is disabled (unlimited).
     */
    @JvmOverloads
    fun toByteLimit(
        maxMemoryBytes: Long = Runtime.getRuntime().maxMemory(),
    ): Long? {
        if (maxSizeMiB == 0L || heapFraction == 0.0) {
            return null
        }
        // Cap before MiB->bytes multiply so a bogus huge maxSizeMiB cannot overflow Long.
        val maxHeapMiB = maxMemoryBytes / BYTES_IN_MIB
        val fromMaxSize = if (maxSizeMiB > maxHeapMiB) {
            maxMemoryBytes
        } else {
            maxSizeMiB * BYTES_IN_MIB
        }
        val fromHeap = floor(maxMemoryBytes.toDouble() * heapFraction).toLong()
        return min(fromMaxSize, fromHeap)
    }

    fun toJsonString(): String? {
        return try {
            JSONObject()
                .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB, maxSizeMiB)
                .put(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION, heapFraction)
                .toString()
        } catch (e: Throwable) {
            DebugLogger.error(TAG, e, e.message)
            null
        }
    }

    companion object {
        private const val TAG = "[ClientReportsQueueBudgetConfig]"
        private const val BYTES_IN_MIB = 1024L * 1024L

        const val DEFAULT_MAX_SIZE_MIB = 16L
        const val DEFAULT_HEAP_FRACTION = 0.125

        @JvmField
        val DEFAULT = ClientReportsQueueBudgetConfig(
            maxSizeMiB = DEFAULT_MAX_SIZE_MIB,
            heapFraction = DEFAULT_HEAP_FRACTION,
        )

        /**
         * @return valid config, or `null` if values are invalid.
         * `null` means no custom config - resolve via [DEFAULT].
         */
        @JvmStatic
        fun create(maxSizeMiB: Long, heapFraction: Double): ClientReportsQueueBudgetConfig? {
            if (maxSizeMiB < 0L || !heapFraction.isFinite() || heapFraction !in 0.0..1.0) {
                return null
            }
            return ClientReportsQueueBudgetConfig(maxSizeMiB, heapFraction)
        }

        @JvmStatic
        fun fromJson(json: JSONObject?): ClientReportsQueueBudgetConfig? {
            if (json == null) {
                return null
            }
            val maxSizeMiB = json.optLongOrNull(JsonResponseKey.CLIENT_REPORTS_QUEUE_MAX_SIZE_MIB)
            val heapFraction = json.optDoubleOrNull(JsonResponseKey.CLIENT_REPORTS_QUEUE_HEAP_FRACTION)
            if (maxSizeMiB == null || heapFraction == null) {
                // Incomplete / unparsable pair: ignore so last-valid is kept on network update.
                return null
            }
            return create(maxSizeMiB, heapFraction)
        }

        @JvmStatic
        fun fromJsonString(value: String?): ClientReportsQueueBudgetConfig? {
            if (value.isNullOrEmpty()) {
                return null
            }
            return try {
                fromJson(JSONObject(value))
            } catch (e: Throwable) {
                DebugLogger.error(TAG, e, e.message)
                null
            }
        }
    }
}
