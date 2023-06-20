package io.appmetrica.analytics.impl

import android.os.Bundle

interface MetricaCoreReporter {

    fun reportData(data: Bundle)
}
