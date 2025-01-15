package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.coreutils.internal.services.FrameworkDetector
import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

internal class MainReporterEventCondition(
    private val configurationHolder: ReportComponentConfigurationHolder,
    initialConfig: CommonArguments.ReporterArguments,
    private val preferences: PreferencesComponentDbStorage
) : EventCondition {

    private val tag = "[MainReporterEventCondition]"

    private val eventName = "activation_unlock_event_sending"

    private val isConditionMet: AtomicBoolean =
        AtomicBoolean(preferences.getMainReporterEventsTriggerConditionMet(false) || initialConfig.isConditionMet())

    init {
        DebugLogger.info(
            tag,
            "Initial value is ${isConditionMet.get()}; " +
                "valueFromPrefs = ${preferences.getMainReporterEventsTriggerConditionMet(false)}; " +
                "valueFromInitialConfig = ${initialConfig.isConditionMet()}"
        )
    }

    override fun isConditionMet(): Boolean {
        refreshConfig()
        val isConditionMet = isConditionMet.get()
        return isConditionMet
    }

    private fun refreshConfig() {
        if (isConditionMet.get().not()) {
            val incomingValue = configurationHolder.isConditionMet()
            if (incomingValue && isConditionMet.compareAndSet(false, true)) {
                DebugLogger.info(tag, "UpdateConfig: change condition met to true")
                preferences.putMainReporterEventsTriggerConditionMet(true)
                sendActivationUnlockEventSendingEvent("activation")
            }
        }
    }

    fun setConditionMetByTimer() {
        if (isConditionMet.compareAndSet(false, true)) {
            DebugLogger.info(tag, "ForceSetConditionMet: change condition met to true")
            preferences.putMainReporterEventsTriggerConditionMet(true)
            sendActivationUnlockEventSendingEvent("timer")
        }
    }

    private fun sendActivationUnlockEventSendingEvent(source: String) {
        try {
            AppMetricaSelfReportFacade.getReporter().reportEvent(
                eventName,
                JSONObject()
                    .put("source", source)
                    .put("framework", FrameworkDetector.framework())
                    .put("appmetrica_plugin_id", GlobalServiceLocator.getInstance().extraMetaInfoRetriever.pluginId)
                    .put(
                        "activation_offset",
                        GlobalServiceLocator.getInstance().serviceLifecycleTimeTracker
                            .offsetInSecondsSinceCreation(TimeUnit.SECONDS)
                    ).toString()
            )
        } catch (e: Throwable) {
            DebugLogger.error(tag, e)
        }
    }

    private fun CommonArguments.ReporterArguments.isConditionMet(): Boolean {
        return this.apiKey != null && this.apiKey != DefaultValues.ANONYMOUS_API_KEY
    }

    private fun ReportComponentConfigurationHolder.isConditionMet(): Boolean {
        val apiKey = this.get().apiKey
        return apiKey != null && apiKey != DefaultValues.ANONYMOUS_API_KEY
    }
}
