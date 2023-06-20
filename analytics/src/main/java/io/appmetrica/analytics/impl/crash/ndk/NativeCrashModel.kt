package io.appmetrica.analytics.impl.crash.ndk

enum class NativeCrashSource {
    UNKNOWN,
    USER,
    CRASHPAD,
}

data class NativeCrashHandlerDescription(
    val source: NativeCrashSource = NativeCrashSource.UNKNOWN,
    val handlerVersion: String? = null
)

internal data class NativeCrashModel(
    val data: ByteArray,
    val handlerDescription: NativeCrashHandlerDescription
)
