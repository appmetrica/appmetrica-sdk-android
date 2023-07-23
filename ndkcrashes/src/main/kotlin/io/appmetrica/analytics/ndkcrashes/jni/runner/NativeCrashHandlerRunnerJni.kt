package io.appmetrica.analytics.ndkcrashes.jni.runner

internal object NativeCrashHandlerRunnerJni {
    external fun runHandler(args: Array<String>): Int
}
