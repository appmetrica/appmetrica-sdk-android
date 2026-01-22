package io.appmetrica.analytics.impl.component.processor.commutation

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.IdentifiersData
import io.appmetrica.analytics.impl.component.CommonArguments.ReporterArguments
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class ForceStartupHandler(
    component: CommutationDispatcherComponent
) : CommutationHandler(component) {

    private val tag = "[ForceStartupHandler]"

    override fun process(reportData: CounterReport, clientUnit: CommutationClientUnit): Boolean {
        DebugLogger.info(tag, "process: $reportData")
        val payload = reportData.payload

        @Suppress("DEPRECATION")
        val identifiersData: IdentifiersData? = payload?.getParcelable(IdentifiersData.BUNDLE_KEY)

        if (identifiersData?.isForceRefreshConfiguration == true) {
            refreshConfiguration(clientUnit.component.configuration)
        }

        component.provokeStartupOrGetCurrentState(identifiersData)
        return false
    }

    private fun refreshConfiguration(configuration: ReporterArguments) {
        val advIdTrackingEnabled = configuration.advIdentifiersTrackingEnabled
        val dataSendingEnabled = configuration.dataSendingEnabled
        DebugLogger.info(
            tag,
            "Refresh configuration: advIdTrackingEnabled = $advIdTrackingEnabled, " +
                "dataSendingEnabled = $dataSendingEnabled"
        )
        GlobalServiceLocator.getInstance().advertisingIdGetter.setInitialStateFromClientConfigIfNotDefined(
            advIdTrackingEnabled ?: DefaultValues.DEFAULT_REPORT_ADV_IDENTIFIERS_ENABLED
        )
        GlobalServiceLocator.getInstance().dataSendingRestrictionController
            .setEnabledFromMainReporterIfNotYet(dataSendingEnabled)
    }
}
