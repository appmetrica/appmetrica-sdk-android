package io.appmetrica.analytics.impl.utils

import io.appmetrica.analytics.plugins.PluginErrorDetails

internal fun PluginErrorDetails.toLogString() = "PluginErrorDetails{" +
    "exceptionClass='$exceptionClass', message='$message', stacktrace=$stacktrace, platform='$platform'," +
    " virtualMachineVersion='$virtualMachineVersion', pluginEnvironment=$pluginEnvironment}"
