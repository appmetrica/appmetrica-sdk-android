package io.appmetrica.analytics.impl.proxy.synchronous

import io.appmetrica.analytics.plugins.PluginErrorDetails

class PluginsReporterSynchronousStageExecutor {

    fun reportPluginUnhandledException(
        errorDetails: PluginErrorDetails
    ) {}

    fun reportPluginError(
        errorDetails: PluginErrorDetails,
        message: String?
    ) {}

    fun reportPluginError(
        identifier: String,
        message: String?,
        errorDetails: PluginErrorDetails?
    ) {}
}
