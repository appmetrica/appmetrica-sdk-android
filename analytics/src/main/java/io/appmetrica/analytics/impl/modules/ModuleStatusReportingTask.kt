package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.impl.selfreporting.SelfReportingLazyEvent
import io.appmetrica.analytics.impl.selfreporting.SelfReportingLazyEventTask
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

internal class ModuleStatusReportingTask(
    private val preferences: SimplePreferenceStorage,
    private val modulesType: String,
    private val timeProvider: SystemTimeProvider = SystemTimeProvider(),
    private val modulesStatus: List<ModuleStatus>
) : SelfReportingLazyEventTask {

    private val tag = "[ModuleStatusReportingTask]"

    private val preferenceKey = "${modulesType.uppercase()}_STATUS"
    private val sendTimeout = TimeUnit.DAYS.toMillis(1)

    override fun get(): SelfReportingLazyEvent? {
        if (modulesStatus.isEmpty()) {
            DebugLogger.info(tag, "Modules status is empty")
            return null
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

            preferences.putString(preferenceKey, newModulesStatus.toJson())

            return SelfReportingLazyEvent(
                "${modulesType}_status",
                newModulesStatus.toJson()
            )
        }

        return null
    }
}
