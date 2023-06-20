package io.appmetrica.analytics.impl

import android.content.Context

internal interface DataAwaiter {

    fun waitForData(context: Context)
}
