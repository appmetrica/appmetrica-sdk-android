package io.appmetrica.analytics.impl.crash.ndk

import android.content.Context
import io.appmetrica.analytics.coreutils.internal.io.FileUtils
import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.impl.crash.ndk.service.NativeCrashHandlerFactory
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceConfig
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceModule
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashServiceModuleDummy

internal class NativeCrashService {
    private val tag = "[NativeCrashService]"

    private val serviceModule: NativeCrashServiceModule =
        ReflectionUtils.loadAndInstantiateClassWithDefaultConstructor<NativeCrashServiceModule>(
            "io.appmetrica.analytics.ndkcrashes.NativeCrashServiceModuleImpl"
        ) ?: NativeCrashServiceModuleDummy()

    private val handlerFactory = NativeCrashHandlerFactory(this::markCrashCompletedAndDeleteCompletedCrashes)

    fun initNativeCrashReporting(context: Context, reportConsumer: ReportConsumer) {
        DebugLogger.info(tag, "Start native crash reporting")
        val nativeCrashFolder = FileUtils.getNativeCrashDirectory(context)?.absolutePath
        if (nativeCrashFolder == null) {
            DebugLogger.error(tag, "Skip handle native crash. Failed to get native crash folder")
            return
        }
        serviceModule.init(
            context,
            NativeCrashServiceConfig(
                nativeCrashFolder = nativeCrashFolder,
            )
        )

        sendOldNativeCrashes(context, reportConsumer)
        serviceModule.setDefaultCrashHandler(handlerFactory.createHandlerForActualSession(context, reportConsumer))
    }

    private fun sendOldNativeCrashes(context: Context, reportConsumer: ReportConsumer) {
        val crashes = serviceModule.getAllCrashes()
        if (crashes.isNotEmpty()) {
            handlerFactory.createHandlerForPrevSession(context, reportConsumer).apply {
                crashes.forEach { newCrash(it) }
            }
        }
    }

    private fun markCrashCompletedAndDeleteCompletedCrashes(uuid: String) {
        serviceModule.markCrashCompleted(uuid)
        serviceModule.deleteCompletedCrashes()
    }
}
