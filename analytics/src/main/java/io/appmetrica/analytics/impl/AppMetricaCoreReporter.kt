package io.appmetrica.analytics.impl

import android.os.Bundle

interface AppMetricaCoreReporter {

    fun reportData(data: Bundle)
}
