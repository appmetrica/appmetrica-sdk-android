package io.appmetrica.analytics.productflow.impl

import io.appmetrica.analytics.coreapi.event.AppMetricaEvent
import io.appmetrica.analytics.coreapi.internal.event.AppMetricaEventData
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock

internal class ProductFlowAppMetricaEventTest : CommonTest() {

    @Test
    fun `getEventData returns constructor argument`() {
        val eventData: AppMetricaEventData = mock()
        val event: AppMetricaEvent = ProductFlowAppMetricaEvent(eventData)

        assertThat(event.eventData).isSameAs(eventData)
    }
}
