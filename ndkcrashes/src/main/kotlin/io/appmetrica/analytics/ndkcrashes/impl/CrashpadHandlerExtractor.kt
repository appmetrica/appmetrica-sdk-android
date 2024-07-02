package io.appmetrica.analytics.ndkcrashes.impl

import android.annotation.SuppressLint
import android.content.Context
import io.appmetrica.analytics.ndkcrashes.impl.utils.DebugLogger
import io.appmetrica.analytics.ndkcrashes.impl.utils.SuspendableFileLocker
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipFile

internal class CrashpadHandlerExtractor(
    private val context: Context,
    private val extractedBinariesDir: File
) {
    private val tag = "[CrashpadHandlerExtractor]"
    private val ioBufferSize = 4096

    @SuppressLint("SetWorldReadable")
    fun extractFileIfStale(pathWithinApk: String, extractedExecutableName: String): String? {
        val apkPath = context.applicationInfo.sourceDir
        val libraryFile = File(extractedBinariesDir, extractedExecutableName)
        if (libraryFile.exists()) {
            DebugLogger.info(tag, "crashpad handler is already extracted")
            return libraryFile.absolutePath
        }
        return withFileLock(fileName = "crpad_ext", workDescription = "copy handler") {
            if (libraryFile.exists()) {
                DebugLogger.info(tag, "crashpad handler is already extracted - under lock")
                return@withFileLock libraryFile.absolutePath
            }
            ZipFile(apkPath).use { zipFile ->
                val zipEntry = zipFile.getEntry(pathWithinApk)
                    ?: throw RuntimeException("Cannot find ZipEntry$pathWithinApk")
                val inputStream = zipFile.getInputStream(zipEntry)
                copyChars(inputStream, FileOutputStream(libraryFile))
                if (!libraryFile.setReadable(true, false)) {
                    DebugLogger.info(tag, "can't make crashpad executable readable")
                    return@use null
                }
                if (!libraryFile.setExecutable(true, false)) {
                    DebugLogger.info(tag, "can't make crashpad executable executable")
                    return@use null
                }
                libraryFile.absolutePath
            }
        }
    }

    private fun <T> withFileLock(fileName: String, workDescription: String, block: () -> T): T? {
        val fileLocker = SuspendableFileLocker.getLock(context, fileName)
        return try {
            fileLocker.lock()
            block()
        } catch (e: Throwable) {
            DebugLogger.error(tag, "Failed to $workDescription", e)
            null
        } finally {
            fileLocker.unlock()
        }
    }

    @Throws(IOException::class)
    private fun copyChars(input: InputStream, output: OutputStream): Int {
        val bufferChars = ByteArray(ioBufferSize)
        var resultCountChars = 0
        var readCountChars: Int
        while (-1 != input.read(bufferChars, 0, ioBufferSize).also { readCountChars = it }) {
            output.write(bufferChars, 0, readCountChars)
            resultCountChars += readCountChars
        }
        return resultCountChars
    }
}
