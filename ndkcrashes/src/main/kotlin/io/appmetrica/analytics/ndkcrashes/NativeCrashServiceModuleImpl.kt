package io.appmetrica.analytics.ndkcrashes

import android.content.Context
import io.appmetrica.analytics.ndkcrashes.impl.AppMetricaServiceNativeLibraryLoader
import io.appmetrica.analytics.ndkcrashes.impl.NativeCrashWatcher
import io.appmetrica.analytics.ndkcrashes.impl.utils.DebugLogger
import io.appmetrica.analytics.ndkcrashes.jni.service.CrashpadCrash
import io.appmetrica.analytics.ndkcrashes.jni.service.NativeCrashServiceJniWrapper
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashHandler
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceConfig
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceModule
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashSource

class NativeCrashServiceModuleImpl : NativeCrashServiceModule() {
    private val tag = "[NativeCrashServiceModuleImpl]"
    private val libraryLoader = AppMetricaServiceNativeLibraryLoader()

    private lateinit var nativeCrashWatcher: NativeCrashWatcher
    private var nativeCrashHandler: NativeCrashHandler? = null

    private val nativeCrashListener: NativeCrashWatcher.Listener = object : NativeCrashWatcher.Listener {
        override fun onNewCrash(uuid: String) {
            val localNativeCrashHandler = nativeCrashHandler
            if (localNativeCrashHandler != null) {
                try {
                    localNativeCrashHandler.newCrash(NativeCrashServiceJniWrapper.readCrash(uuid)!!.toNativeCrash())
                } catch (t: Throwable) {
                    DebugLogger.error(tag, "Failed to read native crash $uuid", t)
                }
            } else {
                DebugLogger.warning(tag, "NativeCrashHandler is not set. Failed to process crash $uuid")
            }
        }
    }

    override fun init(context: Context, config: NativeCrashServiceConfig) {
        if (libraryLoader.loadIfNeeded()) {
            NativeCrashServiceJniWrapper.init(config.nativeCrashFolder)
            nativeCrashWatcher = NativeCrashWatcher(NativeCrashWatcher.getSocketName(context))
            nativeCrashWatcher.subscribe(nativeCrashListener)
        }
    }

    override fun setDefaultCrashHandler(handler: NativeCrashHandler?) {
        nativeCrashHandler = handler
    }

    override fun getAllCrashes(): List<NativeCrash> {
        return if (libraryLoader.loadIfNeeded()) {
            NativeCrashServiceJniWrapper.readAllCrashes().map { it.toNativeCrash() }
        } else {
            DebugLogger.warning(tag, "Failed to read native crashes. Native library is not loaded")
            emptyList()
        }
    }

    override fun markCrashCompleted(uuid: String) {
        if (libraryLoader.loadIfNeeded()) {
            NativeCrashServiceJniWrapper.markCrashCompleted(uuid)
        } else {
            DebugLogger.warning(tag, "Failed to mark native crash completed. Native library is not loaded")
        }
    }

    override fun deleteCompletedCrashes() {
        if (libraryLoader.loadIfNeeded()) {
            NativeCrashServiceJniWrapper.deleteCompletedCrashes()
        } else {
            DebugLogger.warning(tag, "Failed to delete completed crashes. Native library is not loaded")
        }
    }

    private fun CrashpadCrash.toNativeCrash() = NativeCrash.Builder(
        NativeCrashSource.CRASHPAD,
        BuildConfig.VERSION_NAME,
        uuid,
        dumpFile,
        creationTime,
        appMetricaData
    ).build()
}
