package io.appmetrica.analytics.ndkcrashesapi.internal

import android.content.Context

// do not remove/rename current functions to maintain compatibility with ndkcrashes 3.0.0
abstract class NativeCrashClientModule {
    abstract fun initHandling(context: Context, config: NativeCrashClientConfig)
    abstract fun updateAppMetricaMetadata(metadata: String)
    // add new methods with default body to maintain compatibility with ndkcrashes 3.0.0
}
