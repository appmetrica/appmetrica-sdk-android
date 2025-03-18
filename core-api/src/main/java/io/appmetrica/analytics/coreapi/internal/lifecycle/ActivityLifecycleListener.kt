package io.appmetrica.analytics.coreapi.internal.lifecycle

import android.app.Activity
import androidx.annotation.MainThread

interface ActivityLifecycleListener {

    @MainThread
    fun onEvent(activity: Activity, event: ActivityEvent)
}
