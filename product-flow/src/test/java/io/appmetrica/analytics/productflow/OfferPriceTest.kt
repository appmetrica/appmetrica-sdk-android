package io.appmetrica.analytics.productflow

import io.appmetrica.analytics.coreutils.internal.WrapUtils
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal

internal class OfferPriceTest : CommonTest() {

    @Test
    fun `big decimal constructor stores amount and unit`() {
        val price = OfferPrice(BigDecimal("10.5"), "USD")

        assertThat(price.amount).isEqualByComparingTo(BigDecimal("10.5"))
        assertThat(price.unit).isEqualTo("USD")
    }

    @Test
    fun `micros constructor stores converted amount and unit`() {
        val micros = 10_500_000L
        val price = OfferPrice(micros, "RUB")

        assertThat(price.amount).isEqualByComparingTo(WrapUtils.microsToBigDecimal(micros))
        assertThat(price.unit).isEqualTo("RUB")
    }

    @Test
    fun `double constructor stores amount and unit`() {
        val price = OfferPrice(42.5, "gold")

        assertThat(price.amount).isEqualByComparingTo(BigDecimal("42.5"))
        assertThat(price.unit).isEqualTo("gold")
    }

    @Test
    fun `double constructor treats NaN as zero`() {
        assertThat(OfferPrice(Double.NaN, "USD").amount).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `double constructor treats positive infinity as zero`() {
        assertThat(OfferPrice(Double.POSITIVE_INFINITY, "USD").amount).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `double constructor treats negative infinity as zero`() {
        assertThat(OfferPrice(Double.NEGATIVE_INFINITY, "USD").amount).isEqualByComparingTo(BigDecimal.ZERO)
    }
}
