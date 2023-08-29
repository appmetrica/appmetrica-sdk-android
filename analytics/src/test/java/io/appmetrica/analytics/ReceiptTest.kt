package io.appmetrica.analytics

import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReceiptTest : CommonTest() {

    @Test
    fun optional() {
        val receipt = Revenue.Receipt.newBuilder()
            .withData("data")
            .withSignature("signature").build()
        val softAssertion = SoftAssertions()
        softAssertion.assertThat(receipt.data).describedAs("data").isEqualTo("data")
        softAssertion.assertThat(receipt.signature).describedAs("signature").isEqualTo("signature")
        softAssertion.assertAll()
    }
}
