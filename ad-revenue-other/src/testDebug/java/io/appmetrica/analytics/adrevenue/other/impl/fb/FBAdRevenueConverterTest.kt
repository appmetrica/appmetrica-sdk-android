package io.appmetrica.analytics.adrevenue.other.impl.fb

import android.os.Bundle
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AdRevenueConstants
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.util.Currency

@RunWith(RobolectricTestRunner::class)
internal class FBAdRevenueConverterTest : CommonTest() {

    private val converter = FBAdRevenueConverter()

    @Test
    fun convert() {
        val bundle = Bundle().apply {
            putString("encrypted_cpm", "someEncryptedValue")
            putString("placement_id", "12345")
        }

        val result = converter.convert(bundle)

        assertThat(result.adRevenue).isEqualTo(BigDecimal.ZERO)
        assertThat(result.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(result.adType).isNull()
        assertThat(result.adNetwork).isEqualTo("facebook")
        assertThat(result.adUnitId).isNull()
        assertThat(result.adUnitName).isNull()
        assertThat(result.adPlacementId).isNull()
        assertThat(result.adPlacementName).isNull()
        assertThat(result.precision).isNull()
        assertThat(result.autoCollected).isTrue()

        val payload = result.payload!!
        assertThat(payload[AdRevenueConstants.SOURCE_KEY]).isEqualTo("facebook")
        assertThat(payload[AdRevenueConstants.ORIGINAL_SOURCE_KEY]).isEqualTo("ad-revenue-other")
        assertThat(payload[AdRevenueConstants.ORIGINAL_AD_TYPE_KEY]).isEqualTo("null")
        assertThat(payload["raw_payload"]).contains("encrypted_cpm")
        assertThat(payload["raw_payload"]).contains("someEncryptedValue")
    }

    @Test
    fun convertEmptyBundle() {
        val result = converter.convert(Bundle())

        assertThat(result.adRevenue).isEqualTo(BigDecimal.ZERO)
        assertThat(result.autoCollected).isTrue()
        assertThat(result.payload!!["raw_payload"]).isEqualTo("{}")
    }
}
