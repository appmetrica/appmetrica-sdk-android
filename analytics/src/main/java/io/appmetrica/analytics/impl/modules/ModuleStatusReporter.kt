package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.db.preferences.SimplePreferenceStorage
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import java.util.concurrent.Executor

internal class ModuleStatusReporter(
    private val executor: Executor,
    private val preferences: SimplePreferenceStorage,
    private val modulesType: String,
    private val timeProvider: SystemTimeProvider = SystemTimeProvider()
) {

    fun reportModulesStatus(
        modulesStatus: List<ModuleStatus>
    ): Unit = executor.execute {
        AppMetricaSelfReportFacade.getReporter().reportLazyEvent(
            ModuleStatusReportingTask(
                preferences,
                modulesType,
                timeProvider,
                modulesStatus
            )
        )
    }
}
