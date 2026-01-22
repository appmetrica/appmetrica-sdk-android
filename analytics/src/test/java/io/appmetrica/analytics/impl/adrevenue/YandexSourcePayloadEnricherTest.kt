package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class YandexSourcePayloadEnricherTest : CommonTest() {

    private val inputPayloadKey = "input payload key"
    private val inputPayloadValue = "input payload value"
    private val inputPayload = mutableMapOf(inputPayloadKey to inputPayloadValue)

    private val enricher by setUp { YandexSourcePayloadEnricher() }

    @Test
    fun enrich() {
        val outputPayload = enricher.enrich(inputPayload)
        assertThat(outputPayload)
            .isSameAs(inputPayload)
            .isEqualTo(
                mutableMapOf(
                    inputPayloadKey to inputPayloadValue,
                    "source" to "yandex"
                )
            )
    }
}
