package io.appmetrica.analytics.billing.impl.sender

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions
import io.appmetrica.analytics.billing.impl.protobuf.backend.Revenue
import io.appmetrica.analytics.billinginterface.internal.Period
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.function.Consumer

@RunWith(RobolectricTestRunner::class)
class ProductInfoConverterTest : CommonTest() {

    private val converter = ProductInfoConverter()

    @Test
    fun correct() {
        val productInfo = ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "EUR",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.MONTH),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        ProtoObjectPropertyAssertions(proto)
            .checkField("quantity", 2)
            .checkField("priceMicros", 2L)
            .checkField("currency", "EUR".toByteArray())
            .checkField("productId", "sku2".toByteArray())
            .checkFieldRecursively(
                "receipt",
                Consumer<ObjectPropertyAssertions<Revenue.Receipt>> { assertions ->
                    try {
                        assertions
                            .checkField("data", "originalJson".toByteArray())
                            .checkField("signature", "signature2".toByteArray())
                            .checkAll()
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }
            )
            .checkField("autoCollected", true)
            .checkField("guessedBuyerDevice", Revenue.THIS)
            .checkField("inAppType", Revenue.SUBSCRIPTION)
            .checkFieldRecursively(
                "transactionInfo",
                Consumer<ObjectPropertyAssertions<Revenue.Receipt>> { assertions ->
                    try {
                        assertions
                            .checkField("id", "token2".toByteArray())
                            .checkField("time", 1214324L)
                            .checkField("secondaryId", "".toByteArray())
                            .checkField("secondaryTime", 0L)
                            .checkField("state", 0)
                            .checkAll()
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }
            )
            .checkFieldRecursively(
                "subscriptionInfo",
                Consumer<ObjectPropertyAssertions<Revenue.Receipt>> { assertions ->
                    try {
                        assertions
                            .checkField("autoRenewing", false)
                            .checkFieldRecursively(
                                "subscriptionPeriod",
                                Consumer<ObjectPropertyAssertions<Revenue.Receipt>> { assertions ->
                                    try {
                                        assertions
                                            .checkField("number", 1)
                                            .checkField("timeUnit", Revenue.SubscriptionInfo.Period.MONTH)
                                            .checkAll()
                                    } catch (e: Exception) {
                                        throw RuntimeException(e)
                                    }
                                }
                            )
                            .checkFieldRecursively(
                                "introductoryInfo",
                                Consumer<ObjectPropertyAssertions<Revenue.Receipt>> { assertions ->
                                    try {
                                        assertions
                                            .checkField("priceMicros", 4L)
                                            .checkFieldRecursively(
                                                "period",
                                                Consumer<ObjectPropertyAssertions<Revenue.Receipt>> { assertions ->
                                                    try {
                                                        assertions
                                                            .checkField("number", 1)
                                                            .checkField("timeUnit", Revenue.SubscriptionInfo.Period.DAY)
                                                            .checkAll()
                                                    } catch (e: Exception) {
                                                        throw RuntimeException(e)
                                                    }
                                                }
                                            )
                                            .checkField("numberOfPeriods", 1)
                                            .checkField("id", "".toByteArray())
                                            .checkAll()
                                    } catch (e: Exception) {
                                        throw RuntimeException(e)
                                    }
                                }
                            )
                            .checkAll()
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }
            )
            .checkField("payload", "".toByteArray())
            .checkAll()
    }

    @Test
    fun periodNull() {
        val productInfo = ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "EUR",
            4,
            null,
            1,
            null,
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.subscriptionInfo.subscriptionPeriod).isNull()
        assertThat(proto.subscriptionInfo.introductoryInfo.period).isNull()
    }

    @Test
    fun incorrectCurrency() {
        val productInfo = ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.MONTH),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.currency).isEqualTo("".toByteArray())
    }

    @Test
    fun inappType() {
        val productInfo = ProductInfo(
            ProductType.INAPP,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.MONTH),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.inAppType).isEqualTo(Revenue.PURCHASE)
    }

    @Test
    fun subsType() {
        val productInfo = ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.MONTH),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.inAppType).isEqualTo(Revenue.SUBSCRIPTION)
    }

    @Test
    fun unknownType() {
        val productInfo = ProductInfo(
            ProductType.UNKNOWN,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.MONTH),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.inAppType).isEqualTo(Revenue.PURCHASE)
    }

    @Test
    fun timeUnitDay() {
        val productInfo = ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.DAY),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit).isEqualTo(Revenue.SubscriptionInfo.Period.DAY)
    }

    @Test
    fun timeUnitWeek() {
        val productInfo = ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.WEEK),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit).isEqualTo(Revenue.SubscriptionInfo.Period.WEEK)
    }

    @Test
    fun timeUnitMonth() {
        val productInfo = ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.MONTH),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit).isEqualTo(Revenue.SubscriptionInfo.Period.MONTH)
    }

    @Test
    fun timeUnitYear() {
        val productInfo = ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.YEAR),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit).isEqualTo(Revenue.SubscriptionInfo.Period.YEAR)
    }

    @Test
    fun timeUnitUnknown() {
        val productInfo = ProductInfo(
            ProductType.SUBS,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.TIME_UNIT_UNKNOWN),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.subscriptionInfo.subscriptionPeriod.timeUnit)
            .isEqualTo(Revenue.SubscriptionInfo.Period.TIME_UNIT_UNKNOWN)
    }

    @Test
    fun typeIsNotSubs() {
        val productInfo = ProductInfo(
            ProductType.INAPP,
            "sku2",
            2,
            2,
            "test",
            4,
            Period(1, Period.TimeUnit.DAY),
            1,
            Period(1, Period.TimeUnit.TIME_UNIT_UNKNOWN),
            "signature2",
            "token2",
            1214324123,
            false,
            "originalJson"
        )
        val data = converter.fromModel(productInfo)
        val proto = Revenue.parseFrom(data)

        assertThat(proto.subscriptionInfo).isNull()
    }
}
