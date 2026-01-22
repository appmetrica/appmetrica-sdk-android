package io.appmetrica.analytics.impl.crash.jvm.converter

import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashModel
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid.Crash
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashSource

internal class NativeCrashConverter : ProtobufConverter<NativeCrashModel, Crash> {
    companion object {
        private val handlerMapping = mapOf(
            NativeCrashSource.UNKNOWN to CrashAndroid.UNKNOWN,
            NativeCrashSource.CRASHPAD to CrashAndroid.CRASHPAD,
        )
    }

    override fun fromModel(value: NativeCrashModel): Crash {
        return Crash().apply {
            type = Crash.NATIVE
            native_ = Crash.NativeCrash().apply {
                nativeCrashPayload = value.data
                handler = CrashAndroid.NativeCrashHandler().apply {
                    handlerMapping[value.handlerDescription.source]?.let { source = it }
                    version = value.handlerDescription.handlerVersion ?: ""
                }
            }
        }
    }

    override fun toModel(value: Crash): NativeCrashModel {
        throw UnsupportedOperationException()
    }
}
