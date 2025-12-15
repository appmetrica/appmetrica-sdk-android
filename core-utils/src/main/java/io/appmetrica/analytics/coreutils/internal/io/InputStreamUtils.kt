package io.appmetrica.analytics.coreutils.internal.io

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.ByteArrayOutputStream
import java.io.InputStream

object InputStreamUtils {

    private const val TAG = "[InputStreamUtils]"
    private const val IO_BUFFER_SIZE = 8 * 1024 // recommended average buffer size, possible options: 4, 8, 16 KB

    fun readSafelyApprox(limit: Int, inputStreamProvider: () -> InputStream?): ByteArray {
        try {
            inputStreamProvider()?.use { inputStream ->
                ByteArrayOutputStream().use {
                    try {
                        val bufferBytes = ByteArray(IO_BUFFER_SIZE)
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
}
