package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.networktasks.internal.ConfigProvider

internal class LazyReportConfigProvider(
    private val componentUnit: ComponentUnit
) : ConfigProvider<ReportRequestConfig> {

    private val tag = "[LazyReportConfigProvider]"

    private val cachedConfig by lazy {
        componentUnit.freshReportRequestConfig.also {
            DebugLogger.info(tag, "init config with value: %s", it)
        }
    }

    override fun getConfig(): ReportRequestConfig = cachedConfig
}
