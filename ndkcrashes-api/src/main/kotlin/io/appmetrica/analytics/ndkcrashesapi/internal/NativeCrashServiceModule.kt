package io.appmetrica.analytics.ndkcrashesapi.internal

import android.content.Context

// do not remove/rename current functions to maintain compatibility with ndkcrashes 3.0.0
abstract class NativeCrashServiceModule {
    abstract fun init(context: Context, config: NativeCrashServiceConfig)
    abstract fun setDefaultCrashHandler(handler: NativeCrashHandler?)
    abstract fun getAllCrashes(): List<NativeCrash>
    abstract fun markCrashCompleted(uuid: String)
    abstract fun deleteCompletedCrashes()
    // add new methods with default body to maintain compatibility with ndkcrashes 3.0.0
}
