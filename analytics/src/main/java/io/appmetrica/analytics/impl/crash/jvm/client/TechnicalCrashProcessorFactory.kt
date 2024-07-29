package io.appmetrica.analytics.impl.crash.jvm.client

import android.content.Context
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.crash.PushCrashProcessorCreator
import io.appmetrica.analytics.impl.crash.SdkCrashProcessorCreator
import io.appmetrica.analytics.impl.crash.TechnicalCrashProcessorCreator

internal class TechnicalCrashProcessorFactory {

    private val crashProcessorCreators: MutableList<TechnicalCrashProcessorCreator> = mutableListOf(
        SdkCrashProcessorCreator(),
        PushCrashProcessorCreator(),
    )

    fun createCrashProcessors(
        context: Context,
        reporterFactoryProvider: IReporterFactoryProvider
    ): List<ICrashProcessor> = crashProcessorCreators.map {
        it.createCrashProcessor(context, reporterFactoryProvider)
    }

    fun registerCrashProcessorCreator(consumer: TechnicalCrashProcessorCreator) {
        crashProcessorCreators.add(consumer)
    }
}
