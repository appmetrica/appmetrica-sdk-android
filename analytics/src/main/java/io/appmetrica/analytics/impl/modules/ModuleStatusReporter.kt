package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class ModuleStatusReporter(
    private val executor: Executor,
    private val preferences: SimplePreferenceStorage,
    private val modulesType: String,
    private val timeProvider: SystemTimeProvider = SystemTimeProvider()
) {

    private val tag = "[ModuleStatusReporter-$modulesType]"
    private val preferenceKey = "${modulesType.uppercase()}_STATUS"
    private val sendTimeout = TimeUnit.DAYS.toMillis(1)

    fun reportModulesStatus(
        modulesStatus: List<ModuleStatus>
    ): Unit = executor.execute {
        if (modulesStatus.isEmpty()) {
            DebugLogger.info(tag, "Modules status is empty")
            return@execute
        }
        val sortedModulesStatus = modulesStatus.sortedBy { it.moduleName }
        DebugLogger.info(tag, "Report modules status $sortedModulesStatus")

        val preferenceModulesStatus = preferences.getString(preferenceKey, null)?.let {
            ModulesStatus.fromJson(it)
        }
        val currentTime = timeProvider.currentTimeMillis()

        DebugLogger.info(tag, "Modules status from preferences ${preferenceModulesStatus?.toJson()}")

        val shouldSend = preferenceModulesStatus == null ||
            currentTime - preferenceModulesStatus.lastSendTime > sendTimeout ||
            sortedModulesStatus != preferenceModulesStatus.modulesStatus

        if (shouldSend) {
            val newModulesStatus = ModulesStatus(
                modulesStatus = sortedModulesStatus,
                lastSendTime = currentTime
            )

            AppMetricaSelfReportFacade.getReporter().reportEvent(
                "${modulesType}_status",
                newModulesStatus.toJson()
            )

            preferences.putString(preferenceKey, newModulesStatus.toJson())
        }
    }
}
