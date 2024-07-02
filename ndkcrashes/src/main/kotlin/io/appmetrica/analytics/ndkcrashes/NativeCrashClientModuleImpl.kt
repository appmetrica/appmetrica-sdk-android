package io.appmetrica.analytics.ndkcrashes

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Process
import androidx.annotation.RequiresApi
import io.appmetrica.analytics.ndkcrashes.impl.AppMetricaNativeLibraryLoader
import io.appmetrica.analytics.ndkcrashes.impl.CrashpadHandlerFinder
import io.appmetrica.analytics.ndkcrashes.impl.NativeCrashWatcher
import io.appmetrica.analytics.ndkcrashes.impl.utils.AbiResolver
import io.appmetrica.analytics.ndkcrashes.impl.utils.AndroidUtils
import io.appmetrica.analytics.ndkcrashes.impl.utils.DebugLogger
import io.appmetrica.analytics.ndkcrashes.impl.utils.PackagePaths
import io.appmetrica.analytics.ndkcrashes.jni.core.NativeCrashCoreJniWrapper
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashClientConfig
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashClientModule

class NativeCrashClientModuleImpl : NativeCrashClientModule() {
    private val tag = "[NativeCrashClientModuleImpl]"
    private val libraryLoader = AppMetricaNativeLibraryLoader()

    override fun initHandling(context: Context, config: NativeCrashClientConfig) {
        if (!loadAppMetricaNativeLibrary()) {
            DebugLogger.error(tag, "Failed to load native library. Handling native crashes are disabled")
            return
        }
        if (!startHandler(context, config)) {
            DebugLogger.error(tag, "Failed to start native crash handler. Handling native crashes are disabled")
        }
    }

    override fun updateAppMetricaMetadata(metadata: String) {
        if (loadAppMetricaNativeLibrary()) {
            NativeCrashCoreJniWrapper.updateAppMetricaMetadata(metadata)
        }
    }

    private fun loadAppMetricaNativeLibrary(): Boolean = libraryLoader.loadIfNeeded()

    private fun startHandler(context: Context, config: NativeCrashClientConfig): Boolean = try {
        if (AndroidUtils.isAndroidQAchieved()) {
            startHandlerWithLinkerAtCrash(context, config)
        } else if (AndroidUtils.isAndroidMAchieved()) {
            startJavaHandlerAtCrash(context, config)
        } else {
            startHandlerAtCrash(context, config)
        }
    } catch (t: Throwable) {
        DebugLogger.error(tag, "Failed to start native crash handler", t)
        false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startHandlerWithLinkerAtCrash(context: Context, config: NativeCrashClientConfig): Boolean {
        DebugLogger.info(tag, "Start handler with linker at crash")
        return NativeCrashCoreJniWrapper.startHandlerWithLinkerAtCrash(
            handlerPath = getHandlerPath(context),
            crashFolder = config.nativeCrashFolder,
            socketName = NativeCrashWatcher.getSocketName(context),
            is64bit = is64bit(),
            appMetricaMetadata = config.nativeCrashMetadata
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun startJavaHandlerAtCrash(context: Context, config: NativeCrashClientConfig): Boolean {
        DebugLogger.info(tag, "Start java handler at crash")
        val abi = AbiResolver.getAbi()
        if (abi == null) {
            DebugLogger.info(tag, "Failed to detekt abi")
            return false
        }
        val (apkPath, libPath) = PackagePaths.makePackagePaths(context, abi)
        return NativeCrashCoreJniWrapper.startJavaHandlerAtCrash(
            javaHandlerClassName = JavaHandlerRunner::class.java.name,
            handlerPath = getHandlerPath(context),
            crashFolder = config.nativeCrashFolder,
            socketName = NativeCrashWatcher.getSocketName(context),
            apkPath = apkPath,
            libPath = libPath,
            dataDir = Environment.getDataDirectory().absolutePath,
            appMetricaMetadata = config.nativeCrashMetadata
        )
    }

    private fun startHandlerAtCrash(context: Context, config: NativeCrashClientConfig): Boolean {
        DebugLogger.info(tag, "Start handler at crash")
        return NativeCrashCoreJniWrapper.startHandlerAtCrash(
            handlerPath = getHandlerPath(context),
            crashFolder = config.nativeCrashFolder,
            socketName = NativeCrashWatcher.getSocketName(context),
            appMetricaMetadata = config.nativeCrashMetadata
        )
    }

    private fun getHandlerPath(context: Context): String {
        val path = checkNotNull(CrashpadHandlerFinder(AbiResolver).find(context)?.absolutePath) {
            "Not found native crash handler library"
        }
        DebugLogger.info(tag, "Found crashpad handler by path $path")
        return path
    }

    private fun is64bit(): Boolean = AndroidUtils.isAndroidMAchieved() && Process.is64Bit()
}
