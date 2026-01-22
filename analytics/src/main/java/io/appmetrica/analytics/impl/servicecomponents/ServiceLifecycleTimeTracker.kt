package io.appmetrica.analytics.impl.servicecomponents

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import java.util.concurrent.TimeUnit

internal class ServiceLifecycleTimeTracker {

    private val timeProvider: TimeProvider = SystemTimeProvider()

    private val creationTimestamp = timeProvider.currentTimeMillis()

    fun offsetInSecondsSinceCreation(timeUnit: TimeUnit): Long = timeUnit.convert(
        timeProvider.currentTimeMillis() - creationTimestamp,
        TimeUnit.MILLISECONDS
    )
}
