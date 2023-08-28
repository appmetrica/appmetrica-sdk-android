package io.appmetrica.analytics;

import android.content.ContentValues;
import android.os.ResultReceiver;
import android.util.Pair;
import io.appmetrica.analytics.billinginterface.internal.BillingInfo;
import io.appmetrica.analytics.billinginterface.internal.Period;
import io.appmetrica.analytics.billinginterface.internal.ProductInfo;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.coreapi.internal.constants.DeviceTypeValues;
import io.appmetrica.analytics.coreapi.internal.device.ScreenInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState;
import io.appmetrica.analytics.coreutils.internal.cache.CachedDataProvider;
import io.appmetrica.analytics.ecommerce.ECommerceAmount;
import io.appmetrica.analytics.ecommerce.ECommerceCartItem;
import io.appmetrica.analytics.ecommerce.ECommerceOrder;
import io.appmetrica.analytics.ecommerce.ECommercePrice;
import io.appmetrica.analytics.ecommerce.ECommerceProduct;
import io.appmetrica.analytics.ecommerce.ECommerceReferrer;
import io.appmetrica.analytics.ecommerce.ECommerceScreen;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.DeferredDeeplinkState;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.ReportToSend;
import io.appmetrica.analytics.impl.client.ClientConfiguration;
import io.appmetrica.analytics.impl.client.ProcessConfiguration;
import io.appmetrica.analytics.impl.component.clients.ClientDescription;
import io.appmetrica.analytics.impl.ecommerce.client.converter.ECommerceEventConverter;
import io.appmetrica.analytics.impl.ecommerce.client.converter.Result;
import io.appmetrica.analytics.impl.ecommerce.client.model.AmountWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartActionInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.CartItemWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.OrderInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.OrderWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.PriceWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ProductWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ReferrerWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ScreenWrapper;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductCardInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownProductDetailInfoEvent;
import io.appmetrica.analytics.impl.ecommerce.client.model.ShownScreenInfoEvent;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoData;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoState;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.request.CoreRequestConfig;
import io.appmetrica.analytics.impl.startup.AttributionConfig;
import io.appmetrica.analytics.impl.startup.CacheControl;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.StatSending;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedInfo;
import io.appmetrica.analytics.impl.utils.limitation.BytesTruncatedProvider;
import io.appmetrica.analytics.impl.utils.limitation.CollectionTrimInfo;
import io.appmetrica.analytics.impl.utils.limitation.TrimmingResult;
import io.appmetrica.analytics.networktasks.internal.AppInfo;
import io.appmetrica.analytics.networktasks.internal.NetworkAppContext;
import io.appmetrica.analytics.networktasks.internal.NetworkServiceLocator;
import io.appmetrica.analytics.networktasks.internal.SdkInfo;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ToStringTest extends CommonTest {

    @Rule
    public GlobalServiceLocatorRule rule = new GlobalServiceLocatorRule();

    private Class clazz;
    private Object actualValue;
    private int modifierPreconditions;

    public ToStringTest(Object clazz, Object actualValue, int modifierPreconditions, String additionalDescription)
            throws Exception {
        if (clazz instanceof Class) {
            this.clazz = ((Class) clazz);
        } else if (clazz instanceof String) {
            this.clazz = Class.forName(((String) clazz));
        } else {
            throw new IllegalArgumentException("Clazz must be instance of Class or String");
        }
        this.actualValue = actualValue;
        this.modifierPreconditions = modifierPreconditions;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index} - {0} {3}")
    public static Collection<Object[]> data() throws Exception {
        try (
            MockedStatic<NetworkServiceLocator> mockedNetworkServiceLocator =
                Mockito.mockStatic(NetworkServiceLocator.class)
        ) {
            NetworkServiceLocator networkServiceLocator = mock(NetworkServiceLocator.class);
            NetworkAppContext networkAppContext = mock(NetworkAppContext.class);
            SdkInfo sdkInfo = mock(SdkInfo.class);
            AppInfo appInfo = mock(AppInfo.class);
            when(networkAppContext.getSdkInfo()).thenReturn(sdkInfo);
            when(networkAppContext.getAppInfo()).thenReturn(appInfo);
            when(networkServiceLocator.getNetworkAppContext()).thenReturn(networkAppContext);
            when(NetworkServiceLocator.getInstance()).thenReturn(networkServiceLocator);
            return Arrays.asList(new Object[][]{
                    {
                            CounterConfiguration.class,
                            new CounterConfiguration(),
                            Modifier.PRIVATE | Modifier.FINAL,
                            ""
                    },
                    {
                            CoreRequestConfig.class,
                            new CoreRequestConfig(),
                            Modifier.PRIVATE,
                            ""
                    },
                    {
                            ReportToSend.class,
                            ToStringTestUtils.mockValue(ReportToSend.class),
                            Modifier.PRIVATE,
                            ""
                    },
                    {
                            ClientConfiguration.class,
                            new ClientConfiguration(
                                    ToStringTestUtils.mockField(ProcessConfiguration.class),
                                    ToStringTestUtils.mockField(CounterConfiguration.class)
                            ),
                            Modifier.PRIVATE | Modifier.FINAL,
                            ""
                    },
                    {
                            ProcessConfiguration.class,
                            new ProcessConfiguration(ToStringTestUtils.mockField(ContentValues.class), null),
                            0,
                            "with null receiver"
                    },
                    {
                            ProcessConfiguration.class,
                            new ProcessConfiguration(ToStringTestUtils.mockField(ContentValues.class), ToStringTestUtils.mockField(ResultReceiver.class)),
                            0,
                            "with non-null receiver"
                    },
                    {
                            StartupState.class,
                            TestUtils.createDefaultStartupState(),
                            Modifier.PUBLIC | Modifier.FINAL,
                            "field existence only"
                    },
                    {StatSending.class, new StatSending(200), 0, ""},
                    {PermissionState.class, new PermissionState("name", true), 0, ""},
                    {
                            ClientDescription.class,
                            new ClientDescription(
                                    "api key", "packageName", 100, "process_session_id",
                                    ToStringTestUtils.mockField(CounterConfigurationReporterType.class)
                            ),
                            0,
                            "for all non null"
                    },
                    {
                            ClientDescription.class,
                            new ClientDescription(
                                    null, "packageName", null, null, ToStringTestUtils.mockField(CounterConfigurationReporterType.class)
                            ),
                            0,
                            "for all non null"
                    },
                    {CollectingFlags.class, new CollectingFlags.CollectingFlagsBuilder().build(), 0, ""},
                    {
                            AdTrackingInfoResult.class,
                            new AdTrackingInfoResult(null, ToStringTestUtils.mockField(IdentifierStatus.class), null),
                            0,
                            "for null fields"
                    },
                    {
                            AdvertisingIdsHolder.class,
                            new AdvertisingIdsHolder(
                                    ToStringTestUtils.mockField(AdTrackingInfoResult.class),
                                    ToStringTestUtils.mockField(AdTrackingInfoResult.class),
                                    ToStringTestUtils.mockField(AdTrackingInfoResult.class)
                            ),
                            0,
                            ""
                    },
                    {ClientIdentifiersHolder.class, ToStringTestUtils.mockValue(ClientIdentifiersHolder.class), 0, "check existence only"},
                    {
                            DeferredDeeplinkState.class,
                            new DeferredDeeplinkState(
                                    "deeplink", ToStringTestUtils.mockField(Map.class), "unparsed_referrer"
                            ),
                            0,
                            "for non-null fields"
                    },
                    {
                            DeferredDeeplinkState.class,
                            new DeferredDeeplinkState(null, null, null),
                            0,
                            "for null fields"
                    },
                    {
                            IdentifiersResult.class,
                            new IdentifiersResult("id", ToStringTestUtils.mockField(IdentifierStatus.class), "error_explanation"),
                            0,
                            "for non-null fields"
                    },
                    {
                            IdentifiersResult.class,
                            new IdentifiersResult(null, ToStringTestUtils.mockField(IdentifierStatus.class), null),
                            0,
                            "for null fields"
                    },
                    {
                            PreloadInfoData.class,
                            new PreloadInfoData(
                                    ToStringTestUtils.mockField(PreloadInfoState.class),
                                    Arrays.asList(
                                            ToStringTestUtils.mockField(PreloadInfoData.Candidate.class),
                                            ToStringTestUtils.mockField(PreloadInfoData.Candidate.class)
                                    )
                            ),
                            0,
                            ""
                    },
                    {
                            PreloadInfoState.class,
                            new PreloadInfoState(
                                    "7657657",
                                    new JSONObject(),
                                    true,
                                    true,
                                    DistributionSource.APP
                            ),
                            0,
                            "filled"
                    },
                    {
                            PreloadInfoState.class,
                            new PreloadInfoState(
                                    null,
                                    new JSONObject(),
                                    false,
                                    false,
                                    DistributionSource.UNDEFINED
                            ),
                            0,
                            "nullable"
                    },
                    {
                            PreloadInfoData.Candidate.class,
                            new PreloadInfoData.Candidate(
                                    "7657657",
                                    new JSONObject(),
                                    DistributionSource.APP
                            ),
                            0,
                            "filled"
                    },
                    {
                            PreloadInfoData.Candidate.class,
                            new PreloadInfoData.Candidate(
                                    null,
                                    new JSONObject(),
                                    DistributionSource.UNDEFINED
                            ),
                            0,
                            "empty"
                    },
                    {
                            CacheControl.class,
                            new CacheControl(10L),
                            0,
                            ""
                    },
                    {CachedDataProvider.CachedData.class, new CachedDataProvider.CachedData<Object>(10L, 20L, "some description"), 0, ""},
                    {
                            ECommerceAmount.class,
                            new ECommerceAmount(BigDecimal.TEN, "unit"),
                            0,
                            ""
                    },
                    {
                            ECommerceCartItem.class,
                            new ECommerceCartItem(
                                    ToStringTestUtils.mockField(ECommerceProduct.class),
                                    ToStringTestUtils.mockField(ECommercePrice.class),
                                    BigDecimal.TEN
                            ),
                            0,
                            ""
                    },
                    {
                            ECommerceOrder.class,
                            new ECommerceOrder("identifier", ToStringTestUtils.mockField(List.class)),
                            0,
                            ""
                    },
                    {ECommercePrice.class, new ECommercePrice(ToStringTestUtils.mockField(ECommerceAmount.class)), 0, ""},
                    {ECommerceProduct.class, new ECommerceProduct("sku"), 0, ""},
                    {ECommerceReferrer.class, new ECommerceReferrer(), 0, ""},
                    {ECommerceScreen.class, new ECommerceScreen(), 0, ""},
                    {AmountWrapper.class, new AmountWrapper(BigDecimal.TEN, "unit"), 0, ""},
                    {
                            CartItemWrapper.class,
                            new CartItemWrapper(
                                    ToStringTestUtils.mockField(ProductWrapper.class), BigDecimal.TEN,
                                    ToStringTestUtils.mockField(PriceWrapper.class), ToStringTestUtils.mockField(ReferrerWrapper.class)
                            ),
                            0,
                            ""
                    },
                    {
                            CartActionInfoEvent.class,
                            new CartActionInfoEvent(
                                    10, ToStringTestUtils.mockField(CartItemWrapper.class), ToStringTestUtils.mockField(ECommerceEventConverter.class)
                            ),
                            0,
                            ""
                    },
                    {
                            OrderInfoEvent.class,
                            new OrderInfoEvent(
                                    OrderInfoEvent.EVENT_TYPE_BEGIN_CHECKOUT,
                                    ToStringTestUtils.mockField(OrderWrapper.class),
                                    ToStringTestUtils.mockField(ECommerceEventConverter.class)
                            ),
                            0,
                            "Begin checkout event"
                    },
                    {
                            OrderInfoEvent.class,
                            new OrderInfoEvent(
                                    OrderInfoEvent.EVENT_TYPE_PURCHASE,
                                    ToStringTestUtils.mockField(OrderWrapper.class),
                                    ToStringTestUtils.mockField(ECommerceEventConverter.class)
                            ),
                            0,
                            "Purchase event"
                    },
                    {
                            OrderWrapper.class,
                            new OrderWrapper("uid", "identifier", ToStringTestUtils.mockField(List.class), ToStringTestUtils.mockField(Map.class)),
                            0,
                            ""
                    },
                    {
                            PriceWrapper.class,
                            new PriceWrapper(ToStringTestUtils.mockField(AmountWrapper.class), ToStringTestUtils.mockField(List.class)),
                            0,
                            ""
                    },
                    {
                            ProductWrapper.class,
                            new ProductWrapper(
                                    "sku", "name", ToStringTestUtils.mockField(List.class), ToStringTestUtils.mockField(Map.class),
                                    ToStringTestUtils.mockField(PriceWrapper.class), ToStringTestUtils.mockField(PriceWrapper.class), ToStringTestUtils.mockField(List.class)
                            ),
                            0,
                            ""
                    },
                    {
                            ReferrerWrapper.class,
                            new ReferrerWrapper("type", "identifier", ToStringTestUtils.mockField(ScreenWrapper.class)),
                            0,
                            "with non-null fields"
                    },
                    {
                            ReferrerWrapper.class,
                            new ReferrerWrapper(null, null, null),
                            0,
                            "with null fields"
                    },
                    {
                            ScreenWrapper.class,
                            new ScreenWrapper("name", ToStringTestUtils.mockField(List.class), "search_query", ToStringTestUtils.mockField(Map.class)),
                            0,
                            "with non-null fields"
                    },
                    {
                            ScreenWrapper.class,
                            new ScreenWrapper(null, null, null, null),
                            0,
                            "with null fields"
                    },
                    {
                            ShownProductDetailInfoEvent.class,
                            new ShownProductDetailInfoEvent(
                                    ToStringTestUtils.mockField(ProductWrapper.class),
                                    ToStringTestUtils.mockField(ReferrerWrapper.class),
                                    ToStringTestUtils.mockField(ECommerceEventConverter.class)
                            ),
                            0,
                            "with non-null referrer"
                    },
                    {
                            ShownProductDetailInfoEvent.class,
                            new ShownProductDetailInfoEvent(
                                    ToStringTestUtils.mockField(ProductWrapper.class), null, ToStringTestUtils.mockField(ECommerceEventConverter.class)
                            ),
                            0,
                            "with null referrer"
                    },
                    {
                            ShownScreenInfoEvent.class,
                            new ShownScreenInfoEvent(
                                    ToStringTestUtils.mockField(ScreenWrapper.class), ToStringTestUtils.mockField(ECommerceEventConverter.class)
                            ),
                            0,
                            ""
                    },
                    {
                            ShownProductCardInfoEvent.class,
                            new ShownProductCardInfoEvent(
                                    ToStringTestUtils.mockField(ProductWrapper.class), ToStringTestUtils.mockField(ScreenWrapper.class),
                                    ToStringTestUtils.mockField(ECommerceEventConverter.class)
                            ),
                            0,
                            ""
                    },
                    {
                            TrimmingResult.class,
                            new TrimmingResult<Object, BytesTruncatedProvider>(
                                    ToStringTestUtils.mockField(Object.class), ToStringTestUtils.mockField(BytesTruncatedProvider.class)
                            ),
                            0,
                            ""
                    },
                    {
                            Result.class,
                            new Result<Object, BytesTruncatedProvider>(
                                    ToStringTestUtils.mockField(Object.class), ToStringTestUtils.mockField(BytesTruncatedProvider.class)
                            ),
                            0,
                            ""
                    },
                    {
                            BytesTruncatedInfo.class,
                            new BytesTruncatedInfo(100),
                            0,
                            ""
                    },
                    {
                            BytesTruncatedInfo.class,
                            new CollectionTrimInfo(100, 200),
                            0,
                            "with CollectionTrimInfo"
                    },
                    {
                            CollectionTrimInfo.class,
                            new CollectionTrimInfo(100, 200),
                            0,
                            ""
                    },
                    {
                            "io.appmetrica.analytics.impl.permissions.LocationFlagStrategy",
                            ToStringTestUtils.mockValue("io.appmetrica.analytics.impl.permissions.LocationFlagStrategy"),
                            0,
                            "using class reflective access and value mocking"
                    },
                    {
                            "io.appmetrica.analytics.impl.component.session.Session$SessionRequestParams",
                            ToStringTestUtils.mockValue("io.appmetrica.analytics.impl.component.session.Session$SessionRequestParams"),
                            0,
                            "using class reflective access and value mocking"
                    },
                    {
                            ReferrerInfo.class,
                            new ReferrerInfo("referrer", 100L, 200L, ReferrerInfo.Source.GP),
                            0,
                            ""
                    },
                    {
                            BillingConfig.class,
                            new BillingConfig(43875678, 8768),
                            0,
                            "filled value"
                    },
                    {
                            BillingInfo.class,
                            new BillingInfo(ProductType.INAPP, "sku", "token", 41, 42),
                            0,
                            "filled value"
                    },
                    {
                            Period.class,
                            new Period(1, Period.TimeUnit.MONTH),
                            0,
                            "filled value"
                    },
                    {
                            ProductInfo.class,
                            new ProductInfo(ProductType.INAPP, "sku2", 2, 2, "by", 4, new Period(1, Period.TimeUnit.WEEK), 1, new Period(3, Period.TimeUnit.DAY), "signature2", "token2", 11, false, "json"),
                            0,
                            "filled value"
                    },
                    {
                            AttributionConfig.class,
                            new AttributionConfig(new ArrayList<Pair<String, AttributionConfig.Filter>>()),
                            0,
                            "empty value"
                    },
                    {
                            AttributionConfig.class,
                            new AttributionConfig(Arrays.asList(
                                    new Pair<String, AttributionConfig.Filter>("key1", new AttributionConfig.Filter("value1")),
                                    new Pair<String, AttributionConfig.Filter>("key 2", null)
                            )),
                            0,
                            "filled value"
                    },
                    {
                            ScreenInfo.class,
                            new ScreenInfo(0, 0, 0, 0f, DeviceTypeValues.PHONE),
                            0,
                            "empty value"
                    },
                    {
                            StartupParamsItem.class,
                            new StartupParamsItem("id", StartupParamsItemStatus.OK, "error details"),
                            0,
                            "filled value"
                    },
                    {
                            StartupParamsItem.class,
                            new StartupParamsItem(null, StartupParamsItemStatus.FEATURE_DISABLED, null),
                            0,
                            "value with nulls"
                    }
            });
        }
    }

    @Test
    public void toStringContainsAllFields() throws Exception {
        List<Predicate<String>> extractedFieldAndValues =
                ToStringTestUtils.extractFieldsAndValues(clazz, actualValue, modifierPreconditions);
        ToStringTestUtils.testToString(actualValue, extractedFieldAndValues);
    }

}
