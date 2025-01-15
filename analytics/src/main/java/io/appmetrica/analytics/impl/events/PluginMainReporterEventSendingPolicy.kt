package io.appmetrica.analytics.impl.events

import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.ReportComponentConfigurationHolder
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import java.util.concurrent.TimeUnit

internal class PluginMainReporterEventSendingPolicy(
    triggerProvider: EventTriggerProvider,
    configurationHolder: ReportComponentConfigurationHolder,
    initialConfig: CommonArguments.ReporterArguments,
    preferences: PreferencesComponentDbStorage
) : MainReporterEventSendingPolicy {

    private val tag = "[PluginMainReporterEventSendingPolicy]"

    private val mainReporterEventCondition = MainReporterEventCondition(configurationHolder, initialConfig, preferences)

    override val condition: EventCondition = mainReporterEventCondition

    init {
        if (mainReporterEventCondition.isConditionMet.not()) {
            DebugLogger.info(
                tag,
                "Schedule delayed activation in ${DefaultValues.ANONYMOUS_API_KEY_EVENT_SENDING_DELAY_SECONDS} seconds"
            )
            GlobalServiceLocator.getInstance().activationBarrier.subscribe(
                TimeUnit.SECONDS.toMillis(DefaultValues.ANONYMOUS_API_KEY_EVENT_SENDING_DELAY_SECONDS),
                GlobalServiceLocator.getInstance().serviceExecutorProvider.defaultExecutor
            ) {
                DebugLogger.info(tag, "Delay has been ended. Force set condition met.")
                mainReporterEventCondition.setConditionMetByTimer()
                triggerProvider.eventTrigger.trigger()
            }
        } else {
            DebugLogger.info(tag, "Condition already met. ")
        }
    }
}
