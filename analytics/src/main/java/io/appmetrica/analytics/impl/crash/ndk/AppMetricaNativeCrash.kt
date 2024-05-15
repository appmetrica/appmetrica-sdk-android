package io.appmetrica.analytics.impl.crash.ndk

import io.appmetrica.analytics.logger.internal.DebugLogger
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashSource

data class AppMetricaNativeCrash(
    val source: NativeCrashSource,
    val handlerVersion: String,
    val uuid: String,
    val dumpFile: String,
    val creationTime: Long,
    val metadata: AppMetricaNativeCrashMetadata,
) {
    companion object {
        fun from(crash: NativeCrash): AppMetricaNativeCrash? = AppMetricaNativeCrashConverter.from(crash)
    }
}

// need for tests
internal object AppMetricaNativeCrashConverter {
    private const val tag = "[AppMetricaNativeCrashConverter]"
    private val metadataSerializer = AppMetricaNativeCrashMetadataSerializer()

    @JvmStatic
    fun from(crash: NativeCrash): AppMetricaNativeCrash? = try {
        AppMetricaNativeCrash(
            source = crash.source,
            handlerVersion = crash.handlerVersion,
            uuid = crash.uuid,
            dumpFile = crash.dumpFile,
            creationTime = crash.creationTime,
            metadata = metadataSerializer.deserialize(crash.metadata)!!,
        )
    } catch (t: Throwable) {
        DebugLogger.error(tag, "Failed to transform native crash to appmetrica crash", t)
        null
    }
}
