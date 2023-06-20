package io.appmetrica.analytics.coreutils.internal.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object GZIPUtils {

    @JvmStatic
    @Throws(IOException::class)
    fun gzipBytes(input: ByteArray?): ByteArray? = input?.let {
        var outputStream: ByteArrayOutputStream? = null
        var zipOutputStream: GZIPOutputStream? = null
        try {
            outputStream = ByteArrayOutputStream()
            zipOutputStream = GZIPOutputStream(outputStream)
            zipOutputStream.write(input)
            zipOutputStream.finish()
            outputStream.toByteArray()
        } finally {
            zipOutputStream.closeSafely()
            outputStream.closeSafely()
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun unGzipBytes(input: ByteArray?): ByteArray? = input?.let {
        var inputStream: ByteArrayInputStream? = null
        var gzipInputStream: GZIPInputStream? = null
        try {
            inputStream = ByteArrayInputStream(input)
            gzipInputStream = GZIPInputStream(inputStream)
            gzipInputStream.readBytes()
        } finally {
            gzipInputStream.closeSafely()
            inputStream.closeSafely()
        }
    }
}
