package io.appmetrica.analytics.impl.crash

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.crash.jvm.client.ICrashProcessor

internal interface AppCrashProcessorCreator {

    fun createCrashProcessor(
        context: Context,
        config: AppMetricaConfig,
        reporterFactoryProvider: IReporterFactoryProvider
    ): ICrashProcessor
}
