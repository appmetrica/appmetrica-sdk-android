package io.appmetrica.analytics.impl.crash.ndk

import io.appmetrica.analytics.coreapi.internal.backport.Function
import java.io.File

class NativeDumpHandlerWrapper(
    val description: NativeCrashHandlerDescription,
    private val nativeDumpHandler: NativeDumpHandler = NativeDumpHandler()
) : Function<File, String> {

    @Override
    override fun apply(input: File): String? {
        return nativeDumpHandler.apply(input, description)
    }
}
