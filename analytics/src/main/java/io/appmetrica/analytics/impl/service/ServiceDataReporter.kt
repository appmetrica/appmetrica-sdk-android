package io.appmetrica.analytics.impl.service

import android.os.Bundle

interface ServiceDataReporter {

    fun reportData(type: Int, bundle: Bundle)
}
