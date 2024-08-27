package io.appmetrica.analytics

import android.content.ContentValues
import android.util.Pair
import io.appmetrica.analytics.billinginterface.internal.BillingInfo
import io.appmetrica.analytics.billinginterface.internal.Period
import io.appmetrica.analytics.billinginterface.internal.ProductInfo
import io.appmetrica.analytics.billinginterface.internal.ProductType
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider
import io.appmetrica.analytics.ecommerce.ECommerceAmount
import io.appmetrica.analytics.ecommerce.ECommerceCartItem
import io.appmetrica.analytics.ecommerce.ECommerceOrder
import io.appmetrica.analytics.ecommerce.ECommercePrice
import io.appmetrica.analytics.ecommerce.ECommerceProduct
import io.appmetrica.analytics.ecommerce.ECommerceReferrer
import io.appmetrica.analytics.ecommerce.ECommerceScreen
import io.appmetrica.analytics.impl.ClientIdentifiersHolder
import io.appmetrica.analytics.impl.DeferredDeeplinkState
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.ReportToSend
import io.appmetrica.analytics.impl.client.ClientConfiguration
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result
import io.appmetrica.analytics.impl.ecommerce.client.model.AmountWrapper
import io.appmetrica.analytics.impl.ecommerce.client.model.CartActionInfoEvent
import io.appmetrica.analytics.impl.ecommerce.client.model.CartItemWrapper
import io.appmetrica.analytics.impl.ecommerce.client.model.OrderInfoEvent
import io.appmetrica.analytics.impl.ecommerce.client.model.OrderWrapper
import io.appmetrica.analytics.impl.ecommerce.client.model.PriceWrapper
import io.appmetrica.analytics.impl.ecommerce.client.model.ProductWrapper
import io.appmetrica.analytics.impl.ecommerce.client.model.ReferrerWrapper
import io.appmetrica.analytics.impl.ecommerce.client.model.ScreenWrapper
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductCardInfoEvent
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductDetailInfoEvent
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownScreenInfoEvent
import io.appmetrica.analytics.impl.permissions.LocationFlagStrategy
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoData
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.request.CoreRequestConfig
import io.appmetrica.analytics.impl.startup.AttributionConfig
import io.appmetrica.analytics.impl.startup.CacheControl
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.CollectingFlags.CollectingFlagsBuilder
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.startup.StatSending
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import io.appmetrica.analytics.testutils.TestUtils
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.lang.reflect.Modifier
import java.math.BigDecimal

@RunWith(ParameterizedRobolectricTestRunner::class)
class ToStringTest(
    clazz: Any?,
    actualValue: Any,
    modifierPreconditions: Int,
    additionalDescription: String?
) : CommonTest() {

    @get:Rule
    val rule = GlobalServiceLocatorRule()

    private var clazz: Class<*>? = null
    private val actualValue: Any
    private val modifierPreconditions: Int

    init {
        if (clazz is Class<*>) {
            this.clazz = clazz
        } else if (clazz is String) {
            this.clazz = Class.forName(clazz as String?)
        } else {
            throw IllegalArgumentException("Clazz must be instance of Class or String")
        }
        this.actualValue = actualValue
        this.modifierPreconditions = modifierPreconditions
    }

    @Test
    fun toStringContainsAllFields() {
        val extractedFieldAndValues = ToStringTestUtils.extractFieldsAndValues(
            clazz,
            actualValue,
            modifierPreconditions
        )
        ToStringTestUtils.testToString(actualValue, extractedFieldAndValues)
    }

    companion object {

        @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {0} {3}")
        @JvmStatic
        fun data(): List<Array<Any>> {
            return listOf(
                arrayOf(
                    CounterConfiguration::class.java,
                    CounterConfiguration(),
                    Modifier.PRIVATE or Modifier.FINAL,
                    ""
                ),
                arrayOf(
                    CoreRequestConfig::class.java,
                    CoreRequestConfig(),
                    Modifier.PRIVATE,
                    ""
                ),
                arrayOf(
                    ReportToSend::class.java,
                    ReportToSend(
                        MockUtils.mockForToString(),
                        true,
                        12,
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    Modifier.PRIVATE,
                    ""
                ),
                arrayOf(
                    ClientConfiguration::class.java,
                    ClientConfiguration(MockUtils.mockForToString(), MockUtils.mockForToString()),
                    Modifier.PRIVATE or Modifier.FINAL,
                    ""
                ),
                arrayOf(
                    ProcessConfiguration::class.java,
                    ProcessConfiguration(MockUtils.mockForToString<ContentValues>(), null),
                    0,
                    "with null receiver"
                ),
                arrayOf(
                    ProcessConfiguration::class.java,
                    ProcessConfiguration(
                        MockUtils.mockForToString<ContentValues>(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    "with non-null receiver"
                ),
                arrayOf(
                    StartupState::class.java,
                    TestUtils.createDefaultStartupState(),
                    Modifier.PUBLIC or Modifier.FINAL,
                    "field existence only"
                ),
                arrayOf(StatSending::class.java, StatSending(200), 0, ""),
                arrayOf(
                    PermissionState::class.java,
                    PermissionState("name", true),
                    0,
                    ""
                ),
                arrayOf(
                    ClientDescription::class.java,
                    ClientDescription(
                        "api key",
                        "packageName",
                        100,
                        "process_session_id",
                        MockUtils.mockForToString()
                    ),
                    0,
                    "for all non null"
                ),
                arrayOf(
                    ClientDescription::class.java,
                    ClientDescription(
                        null,
                        "packageName",
                        null,
                        null,
                        MockUtils.mockForToString()
                    ),
                    0,
                    "for all non null"
                ),
                arrayOf(CollectingFlags::class.java, CollectingFlagsBuilder().build(), 0, ""),
                arrayOf(
                    AdTrackingInfoResult::class.java,
                    AdTrackingInfoResult(
                        null,
                        MockUtils.mockForToString(),
                        null
                    ),
                    0,
                    "for null fields"
                ),
                arrayOf(
                    AdvertisingIdsHolder::class.java,
                    AdvertisingIdsHolder(
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    ClientIdentifiersHolder::class.java,
                    ClientIdentifiersHolder(
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        200L,
                        300L,
                        MockUtils.mockForToString(),
                        null
                    ),
                    0,
                    "check existence only"
                ),
                arrayOf(
                    DeferredDeeplinkState::class.java,
                    DeferredDeeplinkState(
                        "deeplink",
                        MockUtils.mockForToString<MutableMap<String, String>>(),
                        "unparsed_referrer"
                    ),
                    0,
                    "for non-null fields"
                ),
                arrayOf(
                    DeferredDeeplinkState::class.java,
                    DeferredDeeplinkState(null, null, null),
                    0,
                    "for null fields"
                ),
                arrayOf(
                    IdentifiersResult::class.java,
                    IdentifiersResult(
                        "id",
                        MockUtils.mockForToString(),
                        "error_explanation"
                    ),
                    0,
                    "for non-null fields"
                ),
                arrayOf(
                    IdentifiersResult::class.java,
                    IdentifiersResult(
                        null,
                        MockUtils.mockForToString(),
                        null
                    ),
                    0,
                    "for null fields"
                ),
                arrayOf(
                    PreloadInfoData::class.java,
                    PreloadInfoData(
                        MockUtils.mockForToString(),
                        listOf(MockUtils.mockForToString(), MockUtils.mockForToString())
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    PreloadInfoState::class.java,
                    PreloadInfoState(
                        "7657657",
                        JSONObject(),
                        true,
                        true,
                        DistributionSource.APP
                    ),
                    0,
                    "filled"
                ),
                arrayOf(
                    PreloadInfoState::class.java,
                    PreloadInfoState(
                        null,
                        JSONObject(),
                        false,
                        false,
                        DistributionSource.UNDEFINED
                    ),
                    0,
                    "nullable"
                ),
                arrayOf(
                    PreloadInfoData.Candidate::class.java,
                    PreloadInfoData.Candidate(
                        "7657657",
                        JSONObject(),
                        DistributionSource.APP
                    ),
                    0,
                    "filled"
                ),
                arrayOf(
                    PreloadInfoData.Candidate::class.java,
                    PreloadInfoData.Candidate(
                        null,
                        JSONObject(),
                        DistributionSource.UNDEFINED
                    ),
                    0,
                    "empty"
                ),
                arrayOf(
                    CacheControl::class.java,
                    CacheControl(10L),
                    0,
                    ""
                ),
                arrayOf(
                    CachedDataProvider.CachedData::class.java,
                    CachedDataProvider.CachedData<Any>(10L, 20L, "some description"),
                    0,
                    ""
                ),
                arrayOf(
                    ECommerceAmount::class.java,
                    ECommerceAmount(BigDecimal.TEN, "unit"),
                    0,
                    ""
                ),
                arrayOf(
                    ECommerceCartItem::class.java,
                    ECommerceCartItem(
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        BigDecimal.TEN
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    ECommerceOrder::class.java,
                    ECommerceOrder("identifier", MockUtils.mockForToString()),
                    0,
                    ""
                ),
                arrayOf(
                    ECommercePrice::class.java,
                    ECommercePrice(MockUtils.mockForToString()),
                    0,
                    ""
                ),
                arrayOf(ECommerceProduct::class.java, ECommerceProduct("sku"), 0, ""),
                arrayOf(
                    ECommerceReferrer::class.java,
                    ECommerceReferrer(),
                    0,
                    ""
                ),
                arrayOf(
                    ECommerceScreen::class.java,
                    ECommerceScreen(),
                    0,
                    ""
                ),
                arrayOf(
                    AmountWrapper::class.java,
                    AmountWrapper(BigDecimal.TEN, "unit"),
                    0,
                    ""
                ),
                arrayOf(
                    CartItemWrapper::class.java,
                    CartItemWrapper(
                        MockUtils.mockForToString(),
                        BigDecimal.TEN,
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    CartActionInfoEvent::class.java,
                    CartActionInfoEvent(
                        10,
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    OrderInfoEvent::class.java,
                    OrderInfoEvent(
                        OrderInfoEvent.EVENT_TYPE_BEGIN_CHECKOUT,
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    "Begin checkout event"
                ),
                arrayOf(
                    OrderInfoEvent::class.java,
                    OrderInfoEvent(
                        OrderInfoEvent.EVENT_TYPE_PURCHASE,
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    "Purchase event"
                ),
                arrayOf(
                    OrderWrapper::class.java,
                    OrderWrapper(
                        "uid",
                        "identifier",
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    PriceWrapper::class.java,
                    PriceWrapper(MockUtils.mockForToString(), MockUtils.mockForToString()),
                    0,
                    ""
                ),
                arrayOf(
                    ProductWrapper::class.java,
                    ProductWrapper(
                        "sku",
                        "name",
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    ReferrerWrapper::class.java,
                    ReferrerWrapper("type", "identifier", MockUtils.mockForToString()),
                    0,
                    "with non-null fields"
                ),
                arrayOf(
                    ReferrerWrapper::class.java,
                    ReferrerWrapper(null, null, null),
                    0,
                    "with null fields"
                ),
                arrayOf(
                    ScreenWrapper::class.java,
                    ScreenWrapper(
                        "name",
                        MockUtils.mockForToString(),
                        "search_query",
                        MockUtils.mockForToString()
                    ),
                    0,
                    "with non-null fields"
                ),
                arrayOf(
                    ScreenWrapper::class.java,
                    ScreenWrapper(null, null, null, null),
                    0,
                    "with null fields"
                ),
                arrayOf(
                    ShownProductDetailInfoEvent::class.java,
                    ShownProductDetailInfoEvent(
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    "with non-null referrer"
                ),
                arrayOf(
                    ShownProductDetailInfoEvent::class.java,
                    ShownProductDetailInfoEvent(
                        MockUtils.mockForToString(),
                        null,
                        MockUtils.mockForToString()
                    ),
                    0,
                    "with null referrer"
                ),
                arrayOf(
                    ShownScreenInfoEvent::class.java,
                    ShownScreenInfoEvent(MockUtils.mockForToString(), MockUtils.mockForToString()),
                    0,
                    ""
                ),
                arrayOf(
                    ShownProductCardInfoEvent::class.java,
                    ShownProductCardInfoEvent(
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    TrimmingResult::class.java,
                    TrimmingResult(
                        MockUtils.mockForToString<Any>(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    Result::class.java,
                    Result(
                        MockUtils.mockForToString<Any>(),
                        MockUtils.mockForToString()
                    ),
                    0,
                    ""
                ),
                arrayOf(
                    BytesTruncatedInfo::class.java,
                    BytesTruncatedInfo(100),
                    0,
                    ""
                ),
                arrayOf(
                    BytesTruncatedInfo::class.java,
                    CollectionTrimInfo(100, 200),
                    0,
                    "with CollectionTrimInfo"
                ),
                arrayOf(
                    CollectionTrimInfo::class.java,
                    CollectionTrimInfo(100, 200),
                    0,
                    ""
                ),
                arrayOf(
                    LocationFlagStrategy::class.java,
                    LocationFlagStrategy(),
                    0,
                    "using class reflective access and value mocking"
                ),
                arrayOf(
                    ReferrerInfo::class.java,
                    ReferrerInfo("referrer", 100L, 200L, ReferrerInfo.Source.GP),
                    0,
                    ""
                ),
                arrayOf(
                    BillingConfig::class.java,
                    BillingConfig(43875678, 8768),
                    0,
                    "filled value"
                ),
                arrayOf(
                    BillingInfo::class.java,
                    BillingInfo(ProductType.INAPP, "sku", "token", 41, 42),
                    0,
                    "filled value"
                ),
                arrayOf(
                    Period::class.java,
                    Period(1, Period.TimeUnit.MONTH),
                    0,
                    "filled value"
                ),
                arrayOf(
                    ProductInfo::class.java,
                    ProductInfo(
                        ProductType.INAPP, "sku2", 2, 2, "by", 4, Period(1, Period.TimeUnit.WEEK), 1,
                        Period(
                            3,
                            Period.TimeUnit.DAY
                        ),
                        "signature2", "token2", 11, false, "json"
                    ),
                    0,
                    "filled value"
                ),
                arrayOf(
                    AttributionConfig::class.java,
                    AttributionConfig(ArrayList()),
                    0,
                    "empty value"
                ),
                arrayOf(
                    AttributionConfig::class.java,
                    AttributionConfig(
                        listOf(
                            Pair("key1", AttributionConfig.Filter("value1")),
                            Pair("key 2", null)
                        )
                    ),
                    0,
                    "filled value"
                ),
                arrayOf(
                    StartupParamsItem::class.java,
                    StartupParamsItem("id", StartupParamsItemStatus.OK, "error details"),
                    0,
                    "filled value"
                ),
                arrayOf(
                    StartupParamsItem::class.java,
                    StartupParamsItem(null, StartupParamsItemStatus.FEATURE_DISABLED, null),
                    0,
                    "value with nulls"
                )
            )
        }
    }
}
