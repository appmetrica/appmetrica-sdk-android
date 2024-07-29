package io.appmetrica.analytics.impl.crash

import android.content.Context
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.SdkData
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.impl.UnhandledSituationReporterProvider
import io.appmetrica.analytics.impl.crash.jvm.client.ICrashProcessor
import io.appmetrica.analytics.impl.crash.jvm.client.ReporterBasedCrashProcessor

class SdkCrashProcessorCreator : TechnicalCrashProcessorCreator {

    override fun createCrashProcessor(
        context: Context,
        reporterFactoryProvider: IReporterFactoryProvider
    ): ICrashProcessor {
        return ReporterBasedCrashProcessor(
            context,
            UnhandledSituationReporterProvider(reporterFactoryProvider, SdkData.SDK_API_KEY_UUID),
            { exception -> SdkUtils.isExceptionFromMetrica(exception) },
            null
        )
    }
}
