package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.attribution.ExternalAttributionHelper
import io.appmetrica.analytics.impl.attribution.ExternalAttributionTypeConverter
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution

class ExternalAttributionHandler(
    component: ComponentUnit,
    timeProvider: TimeProvider
) : ReportComponentHandler(component) {

    private val externalAttributionHelper = ExternalAttributionHelper(component, timeProvider)

    override fun process(reportData: CounterReport): Boolean {
        if (!externalAttributionHelper.isInAttributionCollectingWindow()) {
            component.publicLogger.i("Ignoring attribution since out of collecting interval")
            return true
        }

        val attribution = ClientExternalAttribution.parseFrom(reportData.valueBytes)
        val attributionType = attribution.attributionType
        val attributionJson = String(attribution.value)

        if (!externalAttributionHelper.isNewAttribution(attributionType, attributionJson)) {
            component.publicLogger.i(
                "Ignoring attribution " +
                    "of type `${ExternalAttributionTypeConverter.toString(attributionType)}` " +
                    "with value `$attributionJson` " +
                    "since it is not new"
            )
            return true
        }

        externalAttributionHelper.saveAttribution(attributionType, attributionJson)
        component.publicLogger.i(
            "Handling attribution " +
                "of type `${ExternalAttributionTypeConverter.toString(attributionType)}`"
        )

        return false
    }
}
