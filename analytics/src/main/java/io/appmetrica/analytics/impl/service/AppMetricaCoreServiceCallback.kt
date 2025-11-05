package io.appmetrica.analytics.impl.service

import android.app.Service

class AppMetricaCoreServiceCallback(
    private val service: Service
) : AppMetricaServiceCallback {
    override fun onStartFinished(startId: Int) {
        service.stopSelf(startId)
    }
}
