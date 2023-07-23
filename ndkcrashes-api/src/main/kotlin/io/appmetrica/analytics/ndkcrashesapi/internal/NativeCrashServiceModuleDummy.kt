package io.appmetrica.analytics.ndkcrashesapi.internal

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

class NativeCrashServiceModuleDummy : NativeCrashServiceModule() {
    private val tag = "[NativeCrashServiceModuleDummy]"

    override fun init(context: Context, config: NativeCrashServiceConfig) {
        YLogger.debug(tag, "Skipping initialization of server part of native crashes")
    }

    override fun setDefaultCrashHandler(handler: NativeCrashHandler?) {
        YLogger.debug(tag, "Skipping set default native crash handler")
    }

    override fun getAllCrashes(): List<NativeCrash> {
        return emptyList()
    }

    override fun markCrashCompleted(uuid: String) {
        YLogger.debug(tag, "Skipping mark crash completed $uuid")
    }

    override fun deleteCompletedCrashes() {
        YLogger.debug(tag, "Skipping delete completed crashes")
    }
}
