package io.appmetrica.analytics.ndkcrashes.impl

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import io.appmetrica.analytics.ndkcrashes.BuildConfig
import io.appmetrica.analytics.ndkcrashes.impl.utils.AbiResolver
import io.appmetrica.analytics.ndkcrashes.impl.utils.AndroidUtils
import io.appmetrica.analytics.ndkcrashes.impl.utils.DebugLogger
import io.appmetrica.analytics.ndkcrashes.jni.core.NativeCrashUtilsJni
import java.io.File

internal class CrashpadHandlerFinder(private val abiResolver: AbiResolver) {
    private val tag = "[CrashpadHandlerFinder]"
    private val libName = "libappmetrica_crashpad_handler.so"

    fun find(context: Context): File? = try {
        val handlerFile = (getLibDirInsideApk() ?: getLibDir(context))
            ?.let { File(it, libName) }
            ?.takeIf { it.exists() }
            ?: extractLibToDataDir(context)
        handlerFile?.absoluteFile
    } catch (t: Throwable) {
        DebugLogger.error(tag, "Failed to find handler", t)
        null
    }

    private fun getLibDirInsideApk(): File? = try {
        NativeCrashUtilsJni.getLibDirInsideApk()?.let { File(it) }
    } catch (t: Throwable) {
        null
    }

    private fun extractLibToDataDir(context: Context): File? {
        DebugLogger.info(tag, "Handler not found. Try to extract")
        val startTime = SystemClock.elapsedRealtime()
        val suffix = "-${BuildConfig.VERSION_NAME}"

        val abi: String? = abiResolver.getAbi()
        DebugLogger.info(tag, "current ABI is $abi")
        if (abi == null) return null

        val cacheDir = context.cacheDir
        val extractedBinariesDir = cacheDir?.let { File(it, "appmetrica_crashpad_handler_extracted") }
        if (extractedBinariesDir == null) {
            DebugLogger.info(tag, "Extracted binaries dir is null")
            return null
        }

        if (!makeCrashpadDirAndSetPermission(cacheDir, extractedBinariesDir)) {
            DebugLogger.info(tag, "can't make tmp dir")
        }
        val extractedFile = CrashpadHandlerExtractor(context, extractedBinariesDir).extractFileIfStale(
            "lib/$abi/$libName", "$libName$suffix"
        )
        val duration = SystemClock.elapsedRealtime() - startTime
        DebugLogger.info(tag, "Time to extract crashpad binary: $duration ms")
        return extractedFile?.let { File(it) }
    }

    private fun makeCrashpadDirAndSetPermission(cacheDir: File, extractedBinariesDir: File): Boolean {
        if (!extractedBinariesDir.exists()) {
            DebugLogger.info(tag, "make crashpad dir ${extractedBinariesDir.absolutePath}")
            return extractedBinariesDir.mkdirs() &&
                cacheDir.setExecutable(true, false) &&
                extractedBinariesDir.setExecutable(true, false)
        }
        return true
    }

    private fun getLibDir(context: Context): File? {
        val dataDir = getAppDataDir(context) ?: return null
        val libDir = File(dataDir, "lib")
        if (!libDir.exists()) {
            libDir.mkdirs()
        }
        return libDir
    }

    @SuppressLint("NewApi")
    private fun getAppDataDir(context: Context): File? {
        return if (AndroidUtils.isAndroidNAchieved()) {
            context.dataDir
        } else {
            context.filesDir?.parentFile
        }
    }
}
