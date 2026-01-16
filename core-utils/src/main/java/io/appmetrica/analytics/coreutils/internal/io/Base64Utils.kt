package io.appmetrica.analytics.coreutils.internal.io

import android.util.Base64
import io.appmetrica.analytics.coreutils.internal.io.CloseableUtils.closeSafely
import io.appmetrica.analytics.coreutils.internal.io.GZIPUtils.gzipBytes
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

object Base64Utils {
    private const val TAG = "[Base64Utils]"

    /**
     * Buffer size.
     */
    const val IO_BUFFER_SIZE = 4096

    /**
     * Compresses and encodes [Base64] [String] object.
     */
    @JvmStatic
    fun compressBase64String(stringToZip: String?): String? {
        var result: String? = null
        try {
            result = compressBase64(stringToZip?.toByteArray())
        } catch (ignored: Throwable) {
        }
        return result
    }

    @JvmStatic
    fun compressBase64(bytes: ByteArray?): String? {
        var result: String? = null
        try {
            result = Base64.encodeToString(gzipBytes(bytes), Base64.DEFAULT)
        } catch (ignored: Throwable) {
        }
        return result
    }

    @JvmStatic
    fun decompressBase64GzipAsString(stringToUnZip: String?): String? {
        var result: String? = null
        try {
            result = String(decompressBase64GzipAsBytes(stringToUnZip))
        } catch (e: Throwable) {
            DebugLogger.error(TAG, e)
        }
        return result
    }

    @JvmStatic
    fun decompressBase64GzipAsBytes(stringToUnZip: String?): ByteArray {
        var inputStream: ByteArrayInputStream? = null
        var zipInputStream: GZIPInputStream? = null
        return try {
            val stringBytes = Base64.decode(stringToUnZip, Base64.DEFAULT)
            inputStream = ByteArrayInputStream(stringBytes)
            zipInputStream = GZIPInputStream(inputStream)
            zipInputStream.readBytes()
        } catch (e: Throwable) {
            ByteArray(0)
        } finally {
            zipInputStream.closeSafely()
            inputStream.closeSafely()
        }
    }
}
