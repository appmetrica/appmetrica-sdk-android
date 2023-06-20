package io.appmetrica.analytics.impl.service

interface AppMetricaServiceAction {

    companion object {
        const val ACTION_CLIENT_CONNECTION = "io.appmetrica.analytics.IAppMetricaService"
        const val ACTION_SERVICE_WAKELOCK = "io.appmetrica.analytics.ACTION_SERVICE_WAKELOCK"
    }
}
