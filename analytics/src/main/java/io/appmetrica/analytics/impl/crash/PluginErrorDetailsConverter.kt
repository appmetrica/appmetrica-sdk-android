package io.appmetrica.analytics.impl.crash

import io.appmetrica.analytics.impl.ExtraMetaInfoRetriever
import io.appmetrica.analytics.impl.crash.jvm.client.RegularError
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledExceptionFactory
import io.appmetrica.analytics.plugins.PluginErrorDetails

internal class PluginErrorDetailsConverter(
    private val extraMetaInfoRetriever: ExtraMetaInfoRetriever
) {

    fun toRegularError(message: String?, errorDetails: PluginErrorDetails?): RegularError {
        return RegularError(message, errorDetails?.let { toUnhandledException(it) })
    }

    fun toUnhandledException(errorDetails: PluginErrorDetails): UnhandledException {
        return UnhandledExceptionFactory.getUnhandledExceptionFromPlugin(
            errorDetails.exceptionClass,
            errorDetails.message,
            errorDetails.stacktrace,
            errorDetails.platform,
            errorDetails.virtualMachineVersion,
            errorDetails.pluginEnvironment,
            extraMetaInfoRetriever.buildId,
            extraMetaInfoRetriever.isOffline
        )
    }
}
