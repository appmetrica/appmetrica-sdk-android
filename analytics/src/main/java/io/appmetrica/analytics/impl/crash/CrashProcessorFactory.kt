package io.appmetrica.analytics.impl.crash

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.crash.client.ICrashProcessor

class CrashProcessorFactory {

    private val crashProcessorCreators = mutableListOf(
        SdkCrashProcessorCreator(),
        PushCrashProcessorCreator(),
        ApplicationCrashProcessorCreator()
    )

    fun createCrashProcessors(
        context: Context,
        config: AppMetricaConfig,
        reporterFactoryProvider: IReporterFactoryProvider
    ): List<ICrashProcessor> = crashProcessorCreators.map {
        it.createCrashProcessor(context, config, reporterFactoryProvider)
    }

    fun registerCrashProcessorCreator(consumer: CrashProcessorCreator) {
        crashProcessorCreators.add(consumer)
    }
}
