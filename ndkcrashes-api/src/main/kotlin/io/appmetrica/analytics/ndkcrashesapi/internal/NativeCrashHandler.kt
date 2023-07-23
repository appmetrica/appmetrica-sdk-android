package io.appmetrica.analytics.ndkcrashesapi.internal

// do not remove/rename current function to maintain compatibility with ndkcrashes 3.0.0
interface NativeCrashHandler {
    fun newCrash(nativeCrash: NativeCrash)
}
