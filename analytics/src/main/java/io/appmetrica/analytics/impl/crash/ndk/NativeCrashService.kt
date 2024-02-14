package io.appmetrica.analytics.impl.crash.ndk

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.logger.internal.YLogger
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceConfig
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceModule
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceModuleDummy

class NativeCrashService {
    private val tag = "[NativeCrashService]"

    private val serviceModule: NativeCrashServiceModule =
        ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<NativeCrashServiceModule>(
            "io.appmetrica.analytics.ndkcrashes.NativeCrashServiceModuleImpl"
        ) ?: NativeCrashServiceModuleDummy()

    private lateinit var crashReporter: NativeCrashReporter

    fun initNativeCrashReporting(context: Context, reportConsumer: ReportConsumer) {
        YLogger.debug(tag, "Start native crash reporting")
        val nativeCrashFolder = FileUtils.getNativeCrashDirectory(context)?.absolutePath
        if (nativeCrashFolder == null) {
            YLogger.error(tag, "Skip handle native crash. Failed to get native crash folder")
            return
        }
        serviceModule.init(
            context,
            NativeCrashServiceConfig(
                nativeCrashFolder = nativeCrashFolder,
            )
        )

        crashReporter = NativeCrashReporter(reportConsumer, this::markCrashCompletedAndDeleteCompletedCrashes)
        crashReporter.reportCrashesFromPrevSession(serviceModule.getAllCrashes())
        serviceModule.setDefaultCrashHandler(crashReporter)
    }

    private fun markCrashCompletedAndDeleteCompletedCrashes(uuid: String) {
        serviceModule.markCrashCompleted(uuid)
        serviceModule.deleteCompletedCrashes()
    }
}
