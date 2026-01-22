package io.appmetrica.analytics.impl.crash.ndk

import io.appmetrica.analytics.coreapi.internal.backport.Function
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils
import io.appmetrica.analytics.impl.IOUtils
import io.appmetrica.analytics.impl.crash.jvm.converter.NativeCrashConverter
import io.appmetrica.analytics.protobuf.nano.MessageNano
import java.io.File

internal class NativeCrashDumpReader(
    private val description: NativeCrashHandlerDescription,
    private val nativeCrashConverter: NativeCrashConverter,
) : Function<File, String?> {
    override fun apply(file: File): String? = try {
        IOUtils.readAll(file.absolutePath)?.takeIf { it.isNotEmpty() }?.let { dumpDescription ->
            Base64Utils.compressBase64(
                MessageNano.toByteArray(
                    nativeCrashConverter.fromModel(
                        NativeCrashModel(dumpDescription, description)
                    )
                )
            )
        }
    } catch (ignored: Throwable) {
        null
    }
}
