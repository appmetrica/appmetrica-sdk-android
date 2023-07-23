package io.appmetrica.analytics.ndkcrashes.jni.core

internal object NativeCrashUtilsJni {
    @JvmStatic // for tests
    external fun getLibDirInsideApk(): String?
}
