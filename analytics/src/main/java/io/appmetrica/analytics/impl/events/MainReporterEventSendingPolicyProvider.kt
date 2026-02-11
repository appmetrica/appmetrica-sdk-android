package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class MainReporterEventSendingPolicyProvider {

    private val tag = "[MainReporterEventSendingPolicyProvider]"

    fun getPolicy(
        triggerProvider: EventTriggerProvider,
        configurationHolder: ReportComponentConfigurationHolder,
        initialConfig: CommonArguments.ReporterArguments,
        preferences: PreferencesComponentDbStorage
    ): MainReporterEventSendingPolicy = if (shouldUseNativeImpl()) {
        DebugLogger.info(
            tag,
            "Select native main reporter policy. Framework: ${FrameworkDetector.framework()}; " +
                "Plugin build id: ${GlobalServiceLocator.getInstance().extraMetaInfoRetriever.pluginId}"
        )
        NativeMainReporterEventSendingPolicy()
    } else {
        DebugLogger.info(
            tag,
            "Select plugin main reporter policy. Framework: ${FrameworkDetector.framework()}; " +
                "Plugin build id: ${GlobalServiceLocator.getInstance().extraMetaInfoRetriever.pluginId}"
        )
        PluginMainReporterEventSendingPolicy(triggerProvider, configurationHolder, initialConfig, preferences)
    }

    private fun shouldUseNativeImpl() = FrameworkDetector.isNative() &&
        StringUtils.isNullOrEmpty(GlobalServiceLocator.getInstance().extraMetaInfoRetriever.pluginId)
}
