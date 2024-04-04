package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.AdType
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.AutoAdType
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency

class AutoAdRevenueConverterTest : CommonTest() {

    private val converter = AutoAdRevenueConverter()

    @Test
    fun convert() {
        val autoAdRevenue = AutoAdRevenue(
            adRevenue = BigDecimal.valueOf(1213.32312),
            currency = Currency.getInstance("RUB"),
            adType = AutoAdType.INTERSTITIAL,
            adNetwork = "adNetwork",
            adUnitId = "adUnitId",
            adUnitName = "adUnitName",
            adPlacementId = "adPlacementId",
            adPlacementName = "adPlacementName",
            precision = "precision",
            payload = mapOf("key" to "value")
        )
        val adRevenue = converter.convert(autoAdRevenue)

        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", autoAdRevenue.adRevenue)
            .checkField("currency", autoAdRevenue.currency)
            .checkField("adType", AdType.INTERSTITIAL)
            .checkField("adNetwork", autoAdRevenue.adNetwork)
            .checkField("adUnitId", autoAdRevenue.adUnitId)
            .checkField("adUnitName", autoAdRevenue.adUnitName)
            .checkField("adPlacementId", autoAdRevenue.adPlacementId)
            .checkField("adPlacementName", autoAdRevenue.adPlacementName)
            .checkField("precision", autoAdRevenue.precision)
            .checkField("payload", autoAdRevenue.payload)
            .checkAll()
    }

    @Test
    fun convertIfFieldsAreNull() {
        val autoAdRevenue = AutoAdRevenue(
            adRevenue = BigDecimal.valueOf(1213.32312),
            currency = Currency.getInstance("RUB"),
            adType = null,
            adNetwork = null,
            adUnitId = null,
            adUnitName = null,
            adPlacementId = null,
            adPlacementName = null,
            precision = null,
            payload = null,
        )
        val adRevenue = converter.convert(autoAdRevenue)

        ObjectPropertyAssertions(adRevenue)
            .checkField("adRevenue", autoAdRevenue.adRevenue)
            .checkField("currency", autoAdRevenue.currency)
            .checkFieldsAreNull(
                "adType",
                "adNetwork",
                "adUnitId",
                "adUnitName",
                "adPlacementId",
                "adPlacementName",
                "precision",
                "payload"
            )
            .checkAll()
    }

    @Test
    fun convertAndCheckAdType() {
        val softly = SoftAssertions()
        mapOf(
            AutoAdType.NATIVE to AdType.NATIVE,
            AutoAdType.BANNER to AdType.BANNER,
            AutoAdType.REWARDED to AdType.REWARDED,
            AutoAdType.INTERSTITIAL to AdType.INTERSTITIAL,
            AutoAdType.MREC to AdType.MREC,
            AutoAdType.OTHER to AdType.OTHER,
            null to null,
        ).forEach { autoAdType, adType ->
            val autoAdRevenue = AutoAdRevenue(
                adRevenue = BigDecimal.valueOf(1213.32312),
                currency = Currency.getInstance("RUB"),
                adType = autoAdType,
            )
            val adRevenue = converter.convert(autoAdRevenue)
            softly.assertThat(adRevenue.adType).describedAs("$autoAdRevenue -> $adType").isEqualTo(adType)
        }
        softly.assertAll()
    }

}
