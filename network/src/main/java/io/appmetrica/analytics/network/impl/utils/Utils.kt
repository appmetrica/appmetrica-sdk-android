package io.appmetrica.analytics.network.impl.utils

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Collections

private const val IO_BUFFER_SIZE = 4096

internal object Utils {

    private const val TAG = "[Utils]"

    fun readSafely(limit: Int, inputStreamProvider: () -> InputStream?): ByteArray {
        try {
            inputStreamProvider()?.use { inputStream ->
                ByteArrayOutputStream().use {
                    try {
                        val bufferBytes = ByteArray(IO_BUFFER_SIZE * 2)
                        var currentSize = 0
                        while (true) {
                            val readCountBytes = inputStream.read(bufferBytes)
                            if (-1 == readCountBytes || currentSize > limit) {
                                return it.toByteArray()
                            } else if (readCountBytes > 0) {
                                it.write(bufferBytes, 0, readCountBytes)
                                currentSize += readCountBytes
                            }
                        }
                    } catch (ex: Throwable) {
                        DebugLogger.error(TAG, ex)
                    }
                }
            }
        } catch (ex: Throwable) {
            DebugLogger.error(TAG, ex)
        }
        return ByteArray(0)
    }

    @JvmStatic
    fun <K, V> unmodifiableMapCopy(original: Map<K, V>): Map<K, V> {
        return Collections.unmodifiableMap(HashMap(original))
    }
}
