package io.appmetrica.analytics.impl.stub

import io.appmetrica.analytics.plugins.IPluginReporter
import io.appmetrica.analytics.plugins.PluginErrorDetails

internal class PluginReporterStub : IPluginReporter {

    override fun reportUnhandledException(errorDetails: PluginErrorDetails) {
        // Do nothing
    }

    override fun reportError(errorDetails: PluginErrorDetails, message: String?) {
        // Do nothing
    }

    override fun reportError(identifier: String, message: String?, errorDetails: PluginErrorDetails?) {
        // Do nothing
    }
}
