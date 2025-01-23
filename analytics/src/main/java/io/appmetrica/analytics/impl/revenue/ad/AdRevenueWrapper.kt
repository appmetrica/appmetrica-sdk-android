package io.appmetrica.analytics.impl.revenue.ad

import io.appmetrica.analytics.AdRevenue
import io.appmetrica.analytics.AdType
import io.appmetrica.analytics.coreutils.internal.StringUtils
import io.appmetrica.analytics.impl.adrevenue.AdRevenueDataSource
import io.appmetrica.analytics.impl.utils.DecimalProtoModel
import io.appmetrica.analytics.impl.utils.JsonHelper
import io.appmetrica.analytics.impl.utils.limitation.EventLimitationProcessor
import io.appmetrica.analytics.impl.utils.limitation.StringByBytesTrimmer
import io.appmetrica.analytics.impl.utils.limitation.StringTrimmer
import io.appmetrica.analytics.impl.utils.limitation.Trimmer
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.impl.protobuf.backend.AdRevenue as AdRevenueProto
import io.appmetrica.analytics.impl.protobuf.backend.AdRevenue.Decimal as DecimalProto

private val adTypeMapping = mapOf(
    AdType.NATIVE to AdRevenueProto.NATIVE,
    AdType.BANNER to AdRevenueProto.BANNER,
    AdType.REWARDED to AdRevenueProto.REWARDED,
    AdType.INTERSTITIAL to AdRevenueProto.INTERSTITIAL,
    AdType.MREC to AdRevenueProto.MREC,
    AdType.APP_OPEN to AdRevenueProto.APP_OPEN,
    AdType.OTHER to AdRevenueProto.OTHER,
)

internal class AdRevenueWrapper(
    private val revenue: AdRevenue,
    private val autoCollected: Boolean,
    logger: PublicLogger
) {

    private val genericStringTrimmer: Trimmer<String> = StringTrimmer(
        EventLimitationProcessor.AD_REVENUE_GENERIC_STRING_MAX_SIZE, "ad revenue strings", logger
    )
    private val payloadTrimmer: Trimmer<String> = StringByBytesTrimmer(
        EventLimitationProcessor.AD_REVENUE_PAYLOAD_MAX_SIZE, "ad revenue payload", logger
    )

    fun getDataToSend(): Pair<ByteArray, Int> {
        var bytesTruncated = 0
        val proto = AdRevenueProto()

        listOf(
            revenue.adNetwork to { value: ByteArray -> proto.adNetwork = value },
            revenue.adPlacementId to { value: ByteArray -> proto.adPlacementId = value },
            revenue.adPlacementName to { value: ByteArray -> proto.adPlacementName = value },
            revenue.adUnitId to { value: ByteArray -> proto.adUnitId = value },
            revenue.adUnitName to { value: ByteArray -> proto.adUnitName = value },
            revenue.precision to { value: ByteArray -> proto.precision = value },
            revenue.currency.currencyCode to { value: ByteArray -> proto.currency = value },
        ).forEach {
            bytesTruncated += convertStringField(it.first, it.second)
        }

        proto.adType = adTypeMapping[revenue.adType] ?: AdRevenueProto.UNKNOWN

        proto.adRevenue = DecimalProto().apply {
            val model = DecimalProtoModel.fromDecimal(revenue.adRevenue)
            mantissa = model.mantissa
            exponent = model.exponent
        }

        if (revenue.payload != null) {
            val payload = JsonHelper.mapToJsonString(revenue.payload)
            val payloadResult = StringUtils.stringToBytesForProtobuf(payloadTrimmer.trim(payload))
            proto.payload = payloadResult
            bytesTruncated += StringUtils.stringToBytesForProtobuf(payload).size - payloadResult.size
        }

        if (autoCollected) {
            proto.dataSource = AdRevenueDataSource.AUTOCOLLECTED.value.toByteArray()
        }

        return MessageNano.toByteArray(proto) to bytesTruncated
    }

    private fun convertStringField(value: String?, saveFunction: (value: ByteArray) -> Unit): Int {
        val result = genericStringTrimmer.trim(value)
        val initialBytes = StringUtils.stringToBytesForProtobuf(value)
        val resultBytes = StringUtils.stringToBytesForProtobuf(result)
        saveFunction(resultBytes)
        return initialBytes.size - resultBytes.size
    }
}
