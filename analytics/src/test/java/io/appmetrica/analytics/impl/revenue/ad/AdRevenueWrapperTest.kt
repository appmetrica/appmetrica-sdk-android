package io.appmetrica.analytics.impl.revenue.ad

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.AdType
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.impl.utils.limitation.StringByBytesTrimmer
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.MockedConstructionRule
import io.appmetrica.analytics.testutils.MockedStaticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.math.BigDecimal
import java.util.Currency
import io.appmetrica.analytics.impl.protobuf.backend.AdRevenue as AdRevenueProto

private const val adNetwork = "someAdNetwork"
private const val adPlacementId = "someAdPlacementId"
private const val adPlacementName = "someAdPlacementName"
private const val adUnitId = "someAdUnitId"
private const val adUnitName = "someAdUnitName"
private const val precision = "somePrecision"
private val currency = Currency.getInstance("USD")
private const val currencyAnswer = "USD"
private const val autoCollected = true

private const val bigAdNetwork = "bigAdNetworkbigAdNetwork"
private const val bigAdNetworkTrimmed = "bigAdNetwork"
private const val bigAdPlacementId = "bigAdPlacementIdbigAdPlacementId"
private const val bigAdPlacementIdTrimmed = "bigAdPlacementId"
private const val bigAdPlacementName = "bigAdPlacementNamebigAdPlacementName"
private const val bigAdPlacementNameTrimmed = "bigAdPlacementName"
private const val bigAdUnitId = "bigAdUnitIdbigAdUnitId"
private const val bigAdUnitIdTrimmed = "bigAdUnitId"
private const val bigAdUnitName = "bigAdUnitNamebigAdUnitName"
private const val bigAdUnitNameTrimmed = "bigAdUnitName"
private const val bigPrecision = "bigPrecision"
private const val bigPrecisionTrimmed = "bigPrecision"
private val bigCurrency = Currency.getInstance("EUR")
private const val bigCurrencyTrimmed = "eur"

private val adType = AdType.BANNER

private val payload = mapOf("someKey" to "someValue")
private const val payloadJson = "somekey:someValue"
private val bigPayload = mapOf("someKeyBig" to "someValueBig")
private const val bigPayloadJson = "someKeyBig:someValueBig"
private const val bigPayloadJsonTrimmed = "someKeyBig:some"

private val stringTrimMap = mapOf(
    adNetwork to adNetwork,
    adPlacementId to adPlacementId,
    adPlacementName to adPlacementName,
    adUnitId to adUnitId,
    adUnitName to adUnitName,
    precision to precision,
    currency.currencyCode to currencyAnswer,

    bigAdNetwork to bigAdNetworkTrimmed,
    bigAdPlacementId to bigAdPlacementIdTrimmed,
    bigAdPlacementName to bigAdPlacementNameTrimmed,
    bigAdUnitId to bigAdUnitIdTrimmed,
    bigAdUnitName to bigAdUnitNameTrimmed,
    bigPrecision to bigPrecisionTrimmed,
    bigCurrency.currencyCode to bigCurrencyTrimmed,
)

private val stringByBytesTrimMap = mapOf(
    payloadJson to payloadJson,
    bigPayloadJson to bigPayloadJsonTrimmed,
)

class AdRevenueWrapperTest : CommonTest() {

    private val publicLogger = mock<PublicLogger>()

    @get:Rule
    val stringTrimmer = MockedConstructionRule(StringTrimmer::class.java
    ) { mock, context ->
        `when`(mock.trim(any())).thenAnswer {
            val input = it.arguments[0] as String
            stringTrimMap[input] ?: input
        }
    }

    @get:Rule
    val payloadTrimmer = MockedConstructionRule(StringByBytesTrimmer::class.java) {
        mock, context ->
        `when`(mock.trim(any())).thenAnswer {
            val input = it.arguments[0] as String
            stringByBytesTrimMap[input] ?: input
        }
    }

    @get:Rule
    val jsonHelperMock = MockedStaticRule(JsonHelper::class.java)

    @Test
    fun simpleData() {
        jsonHelperMock.staticMock.`when`<String> {
            JsonHelper.mapToJsonString(payload)
        }.thenReturn(payloadJson)

        val adRevenue = AdRevenue.newBuilder(BigDecimal("123.456789"), currency)
            .withAdNetwork(adNetwork)
            .withAdPlacementId(adPlacementId)
            .withAdPlacementName(adPlacementName)
            .withAdType(adType)
            .withAdUnitId(adUnitId)
            .withAdUnitName(adUnitName)
            .withPrecision(precision)
            .withPayload(payload)
            .build()

        val pair = AdRevenueWrapper(adRevenue, autoCollected, publicLogger).getDataToSend()

        val proto = AdRevenueProto.parseFrom(pair.first)

        val assertions = ProtoObjectPropertyAssertions(proto)

        assertions.checkField("adNetwork", StringUtils.stringToBytesForProtobuf(adNetwork))
        assertions.checkField("adPlacementId", StringUtils.stringToBytesForProtobuf(adPlacementId))
        assertions.checkField("adPlacementName", StringUtils.stringToBytesForProtobuf(adPlacementName))
        assertions.checkField("adUnitId", StringUtils.stringToBytesForProtobuf(adUnitId))
        assertions.checkField("adUnitName", StringUtils.stringToBytesForProtobuf(adUnitName))
        assertions.checkField("precision", StringUtils.stringToBytesForProtobuf(precision))
        assertions.checkField("currency", StringUtils.stringToBytesForProtobuf(currencyAnswer))
        assertions.checkField("dataSource", StringUtils.stringToBytesForProtobuf("autocollected"))
        assertions.checkField("payload", StringUtils.stringToBytesForProtobuf(JsonHelper.mapToJsonString(payload)))
        assertions.checkField("adType", 2)
        assertions.checkFieldRecursively("adRevenue") {
                adRevenueAssertions: ObjectPropertyAssertions<AdRevenueProto.Decimal> ->
            adRevenueAssertions.checkField("mantissa", 123456789L)
            adRevenueAssertions.checkField("exponent", -6)
        }

        assertions.checkAll()

        assertThat(pair.second).isZero()
    }

    @Test
    fun bigData() {
        jsonHelperMock.staticMock.`when`<String> {
            JsonHelper.mapToJsonString(bigPayload)
        }.thenReturn(bigPayloadJson)

        val adRevenue = AdRevenue.newBuilder(BigDecimal("123.456789"), bigCurrency)
            .withAdNetwork(bigAdNetwork)
            .withAdPlacementId(bigAdPlacementId)
            .withAdPlacementName(bigAdPlacementName)
            .withAdType(adType)
            .withAdUnitId(bigAdUnitId)
            .withAdUnitName(bigAdUnitName)
            .withPrecision(bigPrecision)
            .withPayload(bigPayload)
            .build()

        val pair = AdRevenueWrapper(adRevenue, false, publicLogger).getDataToSend()

        val range = 0..10
        val mock = stringTrimmer.constructionMock.constructed().first()
        `when`(mock.trim(any())).thenAnswer {
            (it.arguments[0] as String).substring(range)
        }

        val proto = AdRevenueProto.parseFrom(pair.first)

        val assertions = ProtoObjectPropertyAssertions(proto)

        assertions.checkField("adNetwork", bigAdNetworkTrimmed.toProtobufBytes())
        assertions.checkField("adPlacementId", bigAdPlacementIdTrimmed.toProtobufBytes())
        assertions.checkField("adPlacementName", bigAdPlacementNameTrimmed.toProtobufBytes())
        assertions.checkField("adUnitId", bigAdUnitIdTrimmed.toProtobufBytes())
        assertions.checkField("adUnitName", bigAdUnitNameTrimmed.toProtobufBytes())
        assertions.checkField("precision", bigPrecisionTrimmed.toProtobufBytes())
        assertions.checkField("currency", bigCurrencyTrimmed.toProtobufBytes())
        assertions.checkField("dataSource", StringUtils.stringToBytesForProtobuf("manual"))
        assertions.checkField("payload", bigPayloadJsonTrimmed.toProtobufBytes())
        assertions.checkField("adType", 2)
        assertions.checkFieldRecursively("adRevenue") {
                adRevenueAssertions: ObjectPropertyAssertions<AdRevenueProto.Decimal> ->
            adRevenueAssertions.checkField("mantissa", 123456789L)
            adRevenueAssertions.checkField("exponent", -6)
        }

        assertions.checkAll()

        assertThat(pair.second).isEqualTo(
            sizeDiff(bigAdNetwork, bigAdNetworkTrimmed) +
            sizeDiff(bigAdPlacementId, bigAdPlacementIdTrimmed) +
            sizeDiff(bigAdPlacementName, bigAdPlacementNameTrimmed) +
            sizeDiff(bigAdUnitId, bigAdUnitIdTrimmed) +
            sizeDiff(bigAdUnitName, bigAdUnitNameTrimmed) +
            sizeDiff(bigPrecision, bigPrecisionTrimmed) +
            sizeDiff(bigCurrency.currencyCode, bigCurrencyTrimmed) +
            sizeDiff(bigPayloadJson, bigPayloadJsonTrimmed)
        )
    }

    private fun sizeDiff(big: String, small: String) = big.toProtobufBytes().size - small.toProtobufBytes().size

}

private fun String.toProtobufBytes() = StringUtils.stringToBytesForProtobuf(this)
