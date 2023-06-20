package io.appmetrica.analytics.impl.crash

import android.content.Context
import io.appmetrica.analytics.AppMetricaConfig
import io.appmetrica.analytics.impl.IReporterFactoryProvider
import io.appmetrica.analytics.impl.SdkData
import io.appmetrica.analytics.impl.SdkUtils
import io.appmetrica.analytics.impl.UnhandledSituationReporterProvider
import io.appmetrica.analytics.impl.crash.client.ICrashProcessor
import io.appmetrica.analytics.impl.crash.client.ReporterBasedCrashProcessor

class SdkCrashProcessorCreator : CrashProcessorCreator {

    override fun createCrashProcessor(
        context: Context,
        config: AppMetricaConfig,
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
