package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class NativeLayerPayloadEnricherTest : CommonTest() {

    private val enricher by setUp { NativeLayerPayloadEnricher() }

    private val inputPayloadKey = "key"
    private val inputPayloadValue = "value"
    private val inputPayload = mutableMapOf(inputPayloadKey to inputPayloadValue)

    @Test
    fun enrich() {
        val outputPayload = enricher.enrich(inputPayload)
        assertThat(outputPayload).isSameAs(inputPayload)
            .isEqualTo(
                mutableMapOf(
                    inputPayloadKey to inputPayloadValue,
                    AdRevenueConstants.LAYER_KEY to AdRevenueConstants.NATIVE_LAYER
                )
            )
    }
}
