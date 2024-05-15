package io.appmetrica.analytics.ndkcrashesapi.internal

import android.content.Context
import io.appmetrica.analytics.logger.internal.DebugLogger

class NativeCrashServiceModuleDummy : NativeCrashServiceModule() {
    private val tag = "[NativeCrashServiceModuleDummy]"

    override fun init(context: Context, config: NativeCrashServiceConfig) {
        DebugLogger.info(tag, "Skipping initialization of server part of native crashes")
    }

    override fun setDefaultCrashHandler(handler: NativeCrashHandler?) {
        DebugLogger.info(tag, "Skipping set default native crash handler")
    }

    override fun getAllCrashes(): List<NativeCrash> {
        return emptyList()
    }

    override fun markCrashCompleted(uuid: String) {
        DebugLogger.info(tag, "Skipping mark crash completed $uuid")
    }

    override fun deleteCompletedCrashes() {
        DebugLogger.info(tag, "Skipping delete completed crashes")
    }
}
