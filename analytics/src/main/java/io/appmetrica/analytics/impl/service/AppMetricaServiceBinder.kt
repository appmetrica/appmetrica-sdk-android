package io.appmetrica.analytics.impl.service

import android.os.Bundle
import io.appmetrica.analytics.impl.AppMetricaServiceCore
import io.appmetrica.analytics.internal.IAppMetricaService

class AppMetricaServiceBinder(
    private val serviceCore: AppMetricaServiceCore
) : IAppMetricaService.Stub() {

    override fun resumeUserSession(data: Bundle) {
        serviceCore.resumeUserSession(data)
    }

    override fun pauseUserSession(data: Bundle) {
        serviceCore.pauseUserSession(data)
    }

    override fun reportData(type: Int, data: Bundle) {
        serviceCore.reportData(type, data)
    }
}
