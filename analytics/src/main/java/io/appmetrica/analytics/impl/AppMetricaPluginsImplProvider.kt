package io.appmetrica.analytics.impl

import io.appmetrica.analytics.plugins.AppMetricaPlugins

internal object AppMetricaPluginsImplProvider {

    @JvmStatic
    val impl: AppMetricaPlugins = AppMetricaPluginsImpl()
}
