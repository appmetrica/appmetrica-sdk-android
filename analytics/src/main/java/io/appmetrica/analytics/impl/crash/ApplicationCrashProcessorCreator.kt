package io.appmetrica.analytics.impl.crash

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.MainOrCrashReporterProvider
import io.appmetrica.analytics.impl.crash.jvm.client.ICrashProcessor
import io.appmetrica.analytics.impl.crash.jvm.client.ReporterBasedCrashProcessor

class ApplicationCrashProcessorCreator : AppCrashProcessorCreator {

    override fun createCrashProcessor(
        context: Context,
        config: AppMetricaConfig,
        reporterFactoryProvider: IReporterFactoryProvider
    ): ICrashProcessor {
        return ReporterBasedCrashProcessor(
            context,
            MainOrCrashReporterProvider(reporterFactoryProvider, config),
            { true },
            config.crashTransformer
        )
    }
}
