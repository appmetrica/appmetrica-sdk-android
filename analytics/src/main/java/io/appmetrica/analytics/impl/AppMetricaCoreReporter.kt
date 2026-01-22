package io.appmetrica.analytics.impl

import android.os.Bundle

internal interface AppMetricaCoreReporter {

    fun reportData(data: Bundle)
}
