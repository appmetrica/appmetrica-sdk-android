package io.appmetrica.analytics.productflow.impl

import io.appmetrica.analytics.coreapi.event.AppMetricaEvent
import io.appmetrica.analytics.coreapi.internal.event.AppMetricaEventData

internal class ProductFlowAppMetricaEvent(
    private val data: AppMetricaEventData
) : AppMetricaEvent() {

    override fun getEventData() = data
}
