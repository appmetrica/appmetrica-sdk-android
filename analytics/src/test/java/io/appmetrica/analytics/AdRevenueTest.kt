package io.appmetrica.analytics

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency

class AdRevenueTest : CommonTest() {

    private val currency = Currency.getInstance("EUR")
    private val revenue = BigDecimal("739879.1293798798679123")

    @Test
    fun builderWithBigDecimal() {
        val result = AdRevenue.newBuilder(revenue, currency).build()
        assertThat(result.adRevenue).isEqualTo(revenue)
    }

    @Test
    fun builderWithMicros() {
        val revenue = 38246572656938179L
        val result = AdRevenue.newBuilder(revenue, currency).build()
        assertThat(result.adRevenue).isEqualTo(BigDecimal("38246572656.938179"))
    }

    @Test
    fun builderWithDouble() {
        val revenue = 23764872364.12984681726
        val result = AdRevenue.newBuilder(revenue, currency).build()
        assertThat(result.adRevenue).isEqualTo(BigDecimal(revenue))
    }

    @Test
    fun builderWithNanDouble() {
        val revenue = Double.NaN
        val result = AdRevenue.newBuilder(revenue, currency).build()
        assertThat(result.adRevenue).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun builderWithNegativeInfinityDouble() {
        val revenue = Double.NEGATIVE_INFINITY
        val result = AdRevenue.newBuilder(revenue, currency).build()
        assertThat(result.adRevenue).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun builderWithPositiveInfinityDouble() {
        val revenue = Double.POSITIVE_INFINITY
        val result = AdRevenue.newBuilder(revenue, currency).build()
        assertThat(result.adRevenue).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun payloadIsCopied() {
        val payload = mutableMapOf("key1" to "value1", "key2" to "value2")
        val payloadCopy = mutableMapOf("key1" to "value1", "key2" to "value2")
        val builder = AdRevenue.newBuilder(revenue, currency).withPayload(payload)
        payload["key3"] = "value3"
        assertThat(builder.build().payload).isEqualTo(payloadCopy)
    }

    @Test(expected = Exception::class)
    fun payloadIsUnmodifiable() {
        val payload = mutableMapOf("key1" to "value1", "key2" to "value2")
        val result = AdRevenue.newBuilder(revenue, currency).withPayload(payload).build()
        result.payload!!["key3"] = "value3"
    }

    @Test
    fun allFieldsNullable() {
        val result = AdRevenue.newBuilder(revenue, currency).build()
        ObjectPropertyAssertions(result)
            .checkField("adRevenue", revenue)
            .checkField("currency", currency)
            .checkFieldsAreNull(
                "adType", "adNetwork", "adUnitId", "adUnitName",
                "adPlacementId", "adPlacementName", "precision", "payload"
            )
            .checkAll()
    }

    @Test
    fun allFieldsFilled() {
        val type = AdType.INTERSTITIAL
        val network = "some network"
        val unitId = "444-555"
        val unitName = "some unit name"
        val placementId = "888-999"
        val placementName = "some placement name"
        val precision = "very precise"
        val payload = mapOf("key 1" to "value 1", "key 2" to "value 2")
        val result = AdRevenue.newBuilder(revenue, currency)
            .withAdType(type)
            .withAdNetwork(network)
            .withAdUnitId(unitId)
            .withAdUnitName(unitName)
            .withAdPlacementId(placementId)
            .withAdPlacementName(placementName)
            .withPrecision(precision)
            .withPayload(payload)
            .build()
        ObjectPropertyAssertions(result)
            .checkField("adRevenue", revenue)
            .checkField("currency", currency)
            .checkField("adType", type)
            .checkField("adNetwork", network)
            .checkField("adUnitId", unitId)
            .checkField("adUnitName", unitName)
            .checkField("adPlacementId", placementId)
            .checkField("adPlacementName", placementName)
            .checkField("precision", precision)
            .checkField("payload", payload)
            .checkAll()
    }
}
