package io.appmetrica.analytics.impl

import android.os.Bundle
import androidx.annotation.WorkerThread
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ReportProxy {

    private val TAG = "[ReportProxy]"

    @WorkerThread
    fun proxyReport(type: Int, data: Bundle) {
        DebugLogger.info(TAG, "reportData. Type: $type")
        val reporters = GlobalServiceLocator.getInstance()
            .serviceDataReporterHolder
            .getServiceDataReporters(type)
        if (reporters.isEmpty()) {
            DebugLogger.info(TAG, "No reporter with type $type registered")
            return
        }
        reporters.forEach { reporter ->
            reporter.reportData(type, data)
        }
    }
}
