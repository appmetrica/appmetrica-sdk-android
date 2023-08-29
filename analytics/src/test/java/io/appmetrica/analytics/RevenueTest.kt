package io.appmetrica.analytics

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import java.util.Currency

@RunWith(RobolectricTestRunner::class)
class RevenueTest : CommonTest() {

    @Test
    fun onlyRequired() {
        val revenue = Revenue.newBuilder(100.0, Currency.getInstance("USD")).build()
        assertThat(revenue.price).isEqualTo(100.0)
        assertThat(revenue.priceMicros).isNull()
        assertThat(revenue.currency.currencyCode).isEqualTo("USD")
    }

    @Test
    fun onlyRequiredDecimal() {
        val revenue = Revenue.newBuilder(55.5, Currency.getInstance("USD")).build()
        assertThat(revenue.price).isEqualTo(55.5)
        assertThat(revenue.priceMicros).isNull()
        assertThat(revenue.currency.currencyCode).isEqualTo("USD")
    }

    @Test
    fun onlyRequiredMicros() {
        val revenue = Revenue.newBuilderWithMicros(55500000, Currency.getInstance("USD")).build()
        assertThat(revenue.price).isNull()
        assertThat(revenue.priceMicros).isEqualTo(55500000)
        assertThat(revenue.currency.currencyCode).isEqualTo("USD")
    }

    @Test
    fun optional() {
        val receipt = mock<Revenue.Receipt>()
        val revenue = Revenue.newBuilder(100.0, Currency.getInstance("USD"))
            .withPayload("payload")
            .withProductID("productID")
            .withQuantity(300)
            .withReceipt(receipt)
            .build()
        val assertion = SoftAssertions()
        assertion.assertThat(revenue.payload).`as`("payload").isEqualTo("payload")
        assertion.assertThat(revenue.productID).`as`("productID").isEqualTo("productID")
        assertion.assertThat(revenue.quantity).`as`("quantity").isEqualTo(300)
        assertion.assertThat(revenue.receipt).`as`("receipt").isSameAs(receipt)
        assertion.assertAll()
    }
}
