package io.appmetrica.analytics.impl.crash

import android.content.Context
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.crash.jvm.client.ICrashProcessor

internal interface TechnicalCrashProcessorCreator {

    fun createCrashProcessor(
        context: Context,
        reporterFactoryProvider: IReporterFactoryProvider
    ): ICrashProcessor
}
