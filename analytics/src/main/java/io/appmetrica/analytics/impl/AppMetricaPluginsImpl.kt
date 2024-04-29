package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.proxy.AppMetricaPluginsProxy
import io.appmetrica.analytics.plugins.AppMetricaPlugins
import io.appmetrica.analytics.plugins.PluginErrorDetails

internal class AppMetricaPluginsImpl(private val proxy: AppMetricaPluginsProxy) : AppMetricaPlugins {

    constructor() : this(AppMetricaPluginsProxy())

    override fun reportUnhandledException(errorDetails: PluginErrorDetails) {
        proxy.reportUnhandledException(errorDetails)
    }

    override fun reportError(errorDetails: PluginErrorDetails, message: String?) {
        proxy.reportError(errorDetails, message)
    }

    override fun reportError(identifier: String, message: String?, errorDetails: PluginErrorDetails?) {
        proxy.reportError(identifier, message, errorDetails)
    }
}
