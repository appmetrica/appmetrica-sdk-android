package io.appmetrica.analytics.impl.crash.ndk

import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashSource

class NativeCrashModel(
    val data: ByteArray,
    val handlerDescription: NativeCrashHandlerDescription
)

class NativeCrashHandlerDescription(
    val source: NativeCrashSource = NativeCrashSource.UNKNOWN,
    val handlerVersion: String? = null
)
