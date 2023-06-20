package io.appmetrica.analytics.billingv4.impl

import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billingv4.impl.ProductTypeParser.parse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class ProductTypeParserTest(
    private val productType: String,
    private val expected: ProductType
) {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(BillingClient.SkuType.INAPP, ProductType.INAPP),
                arrayOf(BillingClient.SkuType.SUBS, ProductType.SUBS),
                arrayOf("type", ProductType.UNKNOWN),
                arrayOf("", ProductType.UNKNOWN)
            )
        }
    }

    @Test
    fun testParse() {
        assertThat(
            parse(
                productType
            )
        ).isEqualTo(expected)
    }
}
