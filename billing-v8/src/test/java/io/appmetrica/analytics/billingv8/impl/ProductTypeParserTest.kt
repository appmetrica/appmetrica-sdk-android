package io.appmetrica.analytics.billingv8.impl

import com.android.billingclient.api.BillingClient
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billingv8.impl.ProductTypeParser.parse
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class ProductTypeParserTest(
    private val productType: String,
    private val expected: ProductType
) : CommonTest() {

    companion object {

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(BillingClient.ProductType.INAPP, ProductType.INAPP),
                arrayOf(BillingClient.ProductType.SUBS, ProductType.SUBS),
                arrayOf("type", ProductType.UNKNOWN),
                arrayOf("", ProductType.UNKNOWN)
            )
        }
    }

    @Test
    fun parse() {
        assertThat(
            parse(
                productType
            )
        ).isEqualTo(expected)
    }
}
