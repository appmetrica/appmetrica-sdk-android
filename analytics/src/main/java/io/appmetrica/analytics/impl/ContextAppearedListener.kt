package io.appmetrica.analytics.impl

import android.content.Context
import io.appmetrica.analytics.IReporter
import io.appmetrica.analytics.impl.ActivityLifecycleManager.ActivityEvent
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.logger.internal.DebugLogger

class ContextAppearedListener
@JvmOverloads constructor(
    private val activityLifecycleManager: ActivityLifecycleManager,
    private val selfReporter: IReporter = AppMetricaSelfReportFacade.getReporter()
) {

    private val tag = "[ContextAppearedListener]"
    private var context: Context? = null
    private val selfReporterListener = ActivityLifecycleManager.Listener { _, event ->
        DebugLogger.info(tag, "Event received: $event")
        when (event) {
            ActivityEvent.RESUMED -> selfReporter.resumeSession()
            ActivityEvent.PAUSED -> selfReporter.pauseSession()
            else -> {}
        }
    }

    @Synchronized
    fun onProbablyAppeared(context: Context) {
        if (this.context == null) {
            val applicationContext = context.applicationContext
            activityLifecycleManager.maybeInit(applicationContext)
            activityLifecycleManager.registerListener(
                selfReporterListener,
                ActivityEvent.RESUMED, ActivityEvent.PAUSED
            )
            this.context = applicationContext
        }
    }
}
