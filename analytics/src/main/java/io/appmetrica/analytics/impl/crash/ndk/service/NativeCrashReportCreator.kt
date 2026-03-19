package io.appmetrica.analytics.impl.crash.ndk.service

import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.EventsManager
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash

internal class NativeCrashReportCreator(
    private val crash: AppMetricaNativeCrash,
    private val eventType: InternalEvents,
    private val timestampProvider: NativeCrashTimestampProvider,
) {

    fun create(dump: String): CounterReport {
        return EventsManager.nativeCrashEntry(
            eventType,
            dump,
            crash.uuid,
            LoggerStorage.getOrCreatePublicLogger(crash.metadata.apiKey),
            timestampProvider.getTimestamp(crash)
        ).apply {
            eventEnvironment = crash.metadata.errorEnvironment
        }
    }
}
