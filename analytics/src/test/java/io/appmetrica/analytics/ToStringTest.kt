package io.appmetrica.analytics

import android.content.ContentValues
import android.util.Pair
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.ecommerce.ECommerceAmount
import io.appmetrica.analytics.ecommerce.ECommerceCartItem
import io.appmetrica.analytics.ecommerce.ECommerceOrder
import io.appmetrica.analytics.ecommerce.ECommercePrice
import io.appmetrica.analytics.ecommerce.ECommerceProduct
import io.appmetrica.analytics.ecommerce.ECommerceReferrer
import io.appmetrica.analytics.ecommerce.ECommerceScreen
import io.appmetrica.analytics.impl.AppMetricaConfigExtension
import io.appmetrica.analytics.impl.ClientIdentifiersHolder
import io.appmetrica.analytics.impl.DeferredDeeplinkState
import io.appmetrica.analytics.impl.DistributionSource
import io.appmetrica.analytics.impl.ReportToSend
import io.appmetrica.analytics.impl.client.ClientConfiguration
import io.appmetrica.analytics.impl.client.ProcessConfiguration
import io.appmetrica.analytics.impl.client.connection.ServiceDescription
import io.appmetrica.analytics.impl.component.clients.ClientDescription
import io.appmetrica.analytics.impl.db.storage.TempCacheEntry
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
import io.appmetrica.analytics.impl.id.AdvIdGetterController
import io.appmetrica.analytics.impl.permissions.CompositePermissionStrategy
import io.appmetrica.analytics.impl.permissions.LocationFlagStrategy
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoData
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.request.CoreRequestConfig
import io.appmetrica.analytics.impl.request.StartupRequestConfig
import io.appmetrica.analytics.impl.selfreporting.SelfReportingLazyEvent
import io.appmetrica.analytics.impl.startup.AttributionConfig
import io.appmetrica.analytics.impl.startup.CacheControl
import io.appmetrica.analytics.impl.startup.CollectingFlags
import io.appmetrica.analytics.impl.startup.StartupState
import io.appmetrica.analytics.impl.startup.StartupStateModel
import io.appmetrica.analytics.impl.startup.StatSending
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult
import io.appmetrica.analytics.internal.AppMetricaService
import io.appmetrica.analytics.internal.CounterConfiguration
import io.appmetrica.analytics.internal.IdentifiersResult
import io.appmetrica.analytics.testutils.BaseToStringTest
import org.json.JSONObject
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import java.lang.reflect.Modifier
import java.math.BigDecimal

@RunWith(Parameterized::class)
internal class ToStringTest(
    actualValue: Any?,
    modifierPreconditions: Int,
    excludedFields: Set<String>?,
    additionalDescription: String?,
) : BaseToStringTest(
    actualValue,
    modifierPreconditions,
    excludedFields,
    additionalDescription,
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>> = listOf(
            CounterConfiguration().toTestCase(Modifier.PRIVATE or Modifier.FINAL),
            CoreRequestConfig().toTestCase(Modifier.PRIVATE),
            ReportToSend(mock(), true, 12, mock(), mock()).toTestCase(Modifier.PRIVATE),
            ClientConfiguration(mock(), mock()).toTestCase(Modifier.PRIVATE or Modifier.FINAL),
            ProcessConfiguration(mock<ContentValues>(), null).toTestCase(additionalDescription = "with null receiver"),

            ProcessConfiguration(
                mock<ContentValues>(),
                mock()
            ).toTestCase(additionalDescription = "with non-null receiver"),

            StartupState.Builder(CollectingFlags.CollectingFlagsBuilder().build()).build()
                .toTestCase(
                    Modifier.PUBLIC or Modifier.FINAL,
                    additionalDescription = "field existence only"
                ),

            StatSending(200).toTestCase(),
            PermissionState("name", true).toTestCase(),

            ClientDescription("api key", "packageName", 100, "process_session_id", mock()).toTestCase(
                additionalDescription = "for all non null"
            ),

            ClientDescription(null, "packageName", null, null, mock()).toTestCase(
                additionalDescription = "for all non null"
            ),

            CollectingFlags.CollectingFlagsBuilder().build().toTestCase(),
            AdTrackingInfoResult(null, mock(), null).toTestCase(additionalDescription = "for null fields"),
            AdvertisingIdsHolder(mock(), mock(), mock()).toTestCase(),

            ClientIdentifiersHolder(
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                mock(),
                200L,
                300L,
                mock(),
                null
            ).toTestCase(additionalDescription = "check existence only"),

            DeferredDeeplinkState(
                "deeplink",
                MockUtils.mockForToString<MutableMap<String, String>>(),
                "unparsed_referrer"
            ).toTestCase(additionalDescription = "for non-null fields"),

            DeferredDeeplinkState(null, null, null).toTestCase(additionalDescription = "for null fields"),

            IdentifiersResult("id", mock(), "error_explanation").toTestCase(
                additionalDescription = "for non-null fields"
            ),

            IdentifiersResult(null, mock(), null).toTestCase(additionalDescription = "for null fields"),
            PreloadInfoData(mock(), listOf(mock(), mock())).toTestCase(),

            PreloadInfoState("7657657", JSONObject(), true, true, DistributionSource.APP).toTestCase(
                additionalDescription = "filled"
            ),

            PreloadInfoState(null, JSONObject(), false, false, DistributionSource.UNDEFINED).toTestCase(
                additionalDescription = "nullable"
            ),

            PreloadInfoData.Candidate("7657657", JSONObject(), DistributionSource.APP)
                .toTestCase(additionalDescription = "filled"),

            PreloadInfoData.Candidate(null, JSONObject(), DistributionSource.UNDEFINED)
                .toTestCase(additionalDescription = "empty"),

            CacheControl(10L).toTestCase(),
            ECommerceAmount(BigDecimal.TEN, "unit").toTestCase(),
            ECommerceCartItem(mock(), mock(), BigDecimal.TEN).toTestCase(),
            ECommerceOrder("identifier", mock()).toTestCase(),
            ECommercePrice(mock()).toTestCase(),
            ECommerceProduct("sku").toTestCase(),
            ECommerceReferrer().toTestCase(),
            ECommerceScreen().toTestCase(),
            AmountWrapper(BigDecimal.TEN, "unit").toTestCase(),
            CartItemWrapper(mock(), BigDecimal.TEN, mock(), mock()).toTestCase(),
            CartActionInfoEvent(10, mock(), mock()).toTestCase(),

            OrderInfoEvent(
                OrderInfoEvent.EVENT_TYPE_BEGIN_CHECKOUT,
                mock(),
                mock()
            ).toTestCase(additionalDescription = "Begin checkout event"),

            OrderInfoEvent(
                OrderInfoEvent.EVENT_TYPE_PURCHASE,
                mock(),
                mock()
            ).toTestCase(additionalDescription = "Purchase event"),

            OrderWrapper(
                "uid",
                "identifier",
                mock(),
                mock()
            ).toTestCase(),

            PriceWrapper(mock(), mock()).toTestCase(),

            ProductWrapper(
                "sku",
                "name",
                mock(),
                mock(),
                mock(),
                mock(),
                mock()
            ).toTestCase(),

            ReferrerWrapper("type", "identifier", mock()).toTestCase(additionalDescription = "with non-null fields"),
            ReferrerWrapper(null, null, null).toTestCase(additionalDescription = "with null fields"),

            ScreenWrapper(
                "name",
                mock(),
                "search_query",
                mock()
            ).toTestCase(additionalDescription = "with non-null fields"),

            ScreenWrapper(null, null, null, null).toTestCase(additionalDescription = "with null fields"),

            ShownProductDetailInfoEvent(
                mock(),
                mock(),
                mock()
            ).toTestCase(additionalDescription = "with non-null referrer"),

            ShownProductDetailInfoEvent(
                mock(),
                null,
                mock()
            ).toTestCase(additionalDescription = "with null referrer"),

            ShownScreenInfoEvent(mock(), mock()).toTestCase(),

            ShownProductCardInfoEvent(
                mock(),
                mock(),
                mock()
            ).toTestCase(),

            TrimmingResult(
                MockUtils.mockForToString<Any>(),
                mock()
            ).toTestCase(),

            Result(
                MockUtils.mockForToString<Any>(),
                mock()
            ).toTestCase(),

            BytesTruncatedInfo(100).toTestCase(),
            CollectionTrimInfo(100, 200).toTestCase(),

            LocationFlagStrategy()
                .toTestCase(additionalDescription = "using class reflective access and value mocking"),

            ReferrerInfo("referrer", 100L, 200L, ReferrerInfo.Source.GP).toTestCase(),
            AttributionConfig(ArrayList()).toTestCase(additionalDescription = "empty value"),

            AttributionConfig(listOf(Pair("key1", AttributionConfig.Filter("value1")), Pair("key 2", null))).toTestCase(
                additionalDescription = "filled value"
            ),

            StartupParamsItem("id", StartupParamsItemStatus.OK, "error details").toTestCase(
                additionalDescription = "filled value"
            ),

            StartupParamsItem(null, StartupParamsItemStatus.FEATURE_DISABLED, null).toTestCase(
                additionalDescription = "value with nulls"
            ),

            AdvIdGetterController.CanTrackIdentifiers(
                AdvIdGetterController.State.ALLOWED,
                AdvIdGetterController.State.FORBIDDEN_BY_REMOTE_CONFIG,
                AdvIdGetterController.State.FORBIDDEN_BY_CLIENT_CONFIG
            ).toTestCase(additionalDescription = "filled value"),

            ModuleEvent.newBuilder(10).build().toTestCase(additionalDescription = "empty value"),

            ModuleEvent.newBuilder(10)
                .withCategory(ModuleEvent.Category.SYSTEM)
                .withName("Event name")
                .withValue("Event value")
                .withAttributes(mapOf("key" to "value"))
                .withExtras(mapOf("key" to "value".toByteArray()))
                .withEnvironment(mapOf("key" to "value"))
                .build().toTestCase(additionalDescription = "filled value"),

            AppMetricaConfigExtension(listOf("subscriber"), true).toTestCase(additionalDescription = "filled value"),
            SelfReportingLazyEvent("Event value", "Event value").toTestCase(additionalDescription = "filled value"),

            ServiceDescription("packageName", "serviceScheme", AppMetricaService::class.java).toTestCase(
                additionalDescription = "filled value"
            ),

            CompositePermissionStrategy(mock<PermissionStrategy>(), mock<PermissionStrategy>()).toTestCase(),
            TempCacheEntry(100500, "scope", 200500, "Some entry content".toByteArray()).toTestCase(),

            StartupStateModel.StartupStateBuilder(CollectingFlags.CollectingFlagsBuilder().build()).build().toTestCase(
                modifierPreconditions = Modifier.PUBLIC or Modifier.FINAL,
                excludedFields = setOf("deviceID", "deviceIDHash")
            ),

            StartupRequestConfig(mock(), mock()).toTestCase(
                modifierPreconditions = Modifier.PUBLIC or Modifier.FINAL,
                excludedFields = setOf("mReferrerHolder", "defaultStartupHostsProvider")
            )
        )
    }
}
