package io.appmetrica.analytics.impl.adrevenue

import io.appmetrica.analytics.AdType
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenue
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdType
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import java.math.BigDecimal
import java.util.Currency
import org.assertj.core.api.Assertions.assertThat

class ModuleAdRevenueConverterTest : CommonTest() {

    private val converter = ModuleAdRevenueConverter()

    @Test
    fun convert() {
        val autoAdRevenue = ModuleAdRevenue(
            adRevenue = BigDecimal.valueOf(1213.32312),
            currency = Currency.getInstance("RUB"),
            adType = ModuleAdType.INTERSTITIAL,
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
        val autoAdRevenue = ModuleAdRevenue(
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
        val expectedMapping = mapOf(
            ModuleAdType.NATIVE to AdType.NATIVE,
            ModuleAdType.BANNER to AdType.BANNER,
            ModuleAdType.REWARDED to AdType.REWARDED,
            ModuleAdType.INTERSTITIAL to AdType.INTERSTITIAL,
            ModuleAdType.MREC to AdType.MREC,
            ModuleAdType.APP_OPEN to AdType.APP_OPEN,
            ModuleAdType.OTHER to AdType.OTHER,
            null to null,
        )

        assertThat(expectedMapping.keys).containsAll(ModuleAdType.values().toList())

        expectedMapping.forEach { (autoAdType, adType) ->
            val autoAdRevenue = ModuleAdRevenue(
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
