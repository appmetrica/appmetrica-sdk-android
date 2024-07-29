package io.appmetrica.analytics.impl

import android.os.Handler
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor
import io.appmetrica.analytics.impl.crash.jvm.client.JvmCrashClientController

internal interface IAppMetricaCore {
    fun activate(
        config: AppMetricaConfig?,
        reporterFactoryProvider: IReporterFactoryProvider
    )

    val defaultHandler: Handler
    val clientTimeTracker: ClientTimeTracker
    val defaultExecutor: ICommonExecutor
    val appOpenWatcher: AppOpenWatcher
    val jvmCrashClientController: JvmCrashClientController
}
