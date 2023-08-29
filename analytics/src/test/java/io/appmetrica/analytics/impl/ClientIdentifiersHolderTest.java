package io.appmetrica.analytics.impl;

import android.os.Bundle;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.FeaturesInternal;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.StartupUpdateConfig;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.impl.utils.TimeUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClientIdentifiersHolderTest extends CommonTest {

    private final String mUuid = "test uuid";
    private final String mDeviceId = "test device id";
    private final String mDeviceIdHash = "test device id hash";
    private final String mReportAdUrl = "test report ad url";
    private final String mGetAdUrl = "test get ad url";
    private final Map<String, String> mRequestClids = new HashMap<String, String>();
    private final Map<String, String> mResponseClids = new HashMap<String, String>();
    private final Map<String, List<String>> customSdkHosts = new HashMap<String, List<String>>();
    private final long mServerTimeOffset = 765764576;
    private final long nextStartupTime = 123543;
    private final Map<String, String> clientClids = new HashMap<String, String>();
    @Mock
    private FeaturesInternal features;
    private BillingConfig autoInappCollectingConfig;

    private final IdentifiersResult mUuidData = new IdentifiersResult(mUuid, IdentifierStatus.OK, null);
    private final IdentifiersResult mDeviceIdData = new IdentifiersResult(mDeviceId, IdentifierStatus.OK, null);
    private final IdentifiersResult mDeviceIdHashData = new IdentifiersResult(mDeviceIdHash, IdentifierStatus.OK, null);
    private final IdentifiersResult mReportAdUrlData = new IdentifiersResult(mReportAdUrl, IdentifierStatus.OK, null);
    private final IdentifiersResult mGetAdUrlData = new IdentifiersResult(mGetAdUrl, IdentifierStatus.OK, null);
    private IdentifiersResult mClientClidsForRequestData;
    private IdentifiersResult mResponseClidsData;
    private IdentifiersResult customSdkHostsData;
    private final String mGaidAdvId = "google adv id";
    private final String mHoaidAdvId = "huawei adv id";
    private final String yandexAdvId = "yandex adv id";
    private final IdentifierStatus mGaidStatus = IdentifierStatus.FEATURE_DISABLED;
    private final IdentifierStatus mHoaidStatus = IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE;
    private final IdentifierStatus yandexStatus = IdentifierStatus.NO_STARTUP;
    private final String mGaidError = "gaid error";
    private final String mHoaidError = "hoaid error";
    private final String yandexError = "yandex error";
    private final IdentifiersResult mGaidData = new IdentifiersResult(mGaidAdvId, mGaidStatus, mGaidError);
    private final IdentifiersResult mHoaidData = new IdentifiersResult(mHoaidAdvId, mHoaidStatus, mHoaidError);
    private final IdentifiersResult yandexAdvIdData = new IdentifiersResult(yandexAdvId, yandexStatus, yandexError);

    @Rule
    public final MockedStaticRule<TimeUtils> sTimeUtils = new MockedStaticRule<>(TimeUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        autoInappCollectingConfig = new BillingConfig(1, 2);
        mRequestClids.put("clid0", "0");
        mRequestClids.put("clid1", "1");
        mResponseClids.put("clid2", "2");
        mResponseClids.put("clid3", "3");
        clientClids.put("clid4", "4");
        clientClids.put("clid5", "5");
        customSdkHosts.put("am", Arrays.asList("https://am.host1", "https://am.host2"));
        customSdkHosts.put("ads", Arrays.asList("https://ads.host1"));

        mClientClidsForRequestData = new IdentifiersResult(JsonHelper.clidsToString(clientClids), IdentifierStatus.OK, null);
        mResponseClidsData = new IdentifiersResult(JsonHelper.clidsToString(mResponseClids), IdentifierStatus.OK, null);
        customSdkHostsData = new IdentifiersResult(JsonHelper.customSdkHostsToString(customSdkHosts), IdentifierStatus.OK, null);
        when(TimeUtils.getServerTimeOffset()).thenReturn(mServerTimeOffset);
    }

    @Test
    public void testByFieldConstructor() throws Exception {
        ClientIdentifiersHolder clientIdentifiersHolder = new ClientIdentifiersHolder(
                mUuidData,
                mDeviceIdData,
                mDeviceIdHashData,
                mReportAdUrlData,
                mGetAdUrlData,
                mResponseClidsData,
                mClientClidsForRequestData,
                mGaidData,
                mHoaidData,
                yandexAdvIdData,
                customSdkHostsData,
                mServerTimeOffset,
                nextStartupTime,
                features
        );

        ObjectPropertyAssertions<ClientIdentifiersHolder> assertions = ObjectPropertyAssertions(clientIdentifiersHolder).withPrivateFields(true);
        assertions.checkField("mUuidData", "getUuid", mUuidData);
        assertions.checkField("mDeviceIdData", "getDeviceId", mDeviceIdData);
        assertions.checkField("mDeviceIdHashData", "getDeviceIdHash", mDeviceIdHashData);
        assertions.checkField("mReportAdUrlData", "getReportAdUrl", mReportAdUrlData);
        assertions.checkField("mGetAdUrlData", "getGetAdUrl", mGetAdUrlData);
        assertions.checkField("mResponseClidsData", "getResponseClids", mResponseClidsData);
        assertions.checkField("mClientClidsForRequestData", "getClientClidsForRequest", mClientClidsForRequestData);
        assertions.checkField("mGaidData", "getGaid", mGaidData);
        assertions.checkField("mHoaidData", "getHoaid", mHoaidData);
        assertions.checkField("yandexAdvIdData", "getYandexAdvId", yandexAdvIdData);
        assertions.checkField("customSdkHostsData", "getCustomSdkHosts", customSdkHostsData);
        assertions.checkField("mServerTimeOffset", "getServerTimeOffset", mServerTimeOffset);
        assertions.checkField("nextStartupTime", "getNextStartupTime", nextStartupTime);
        assertions.checkField("features", "getFeatures", features);
        assertions.checkAll();
    }

    @Test
    public void testNullAdTrackingInfo() {
        AdvertisingIdsHolder advertisingIdsHolder = mock(AdvertisingIdsHolder.class);
        when(advertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(null, mGaidStatus, mGaidError));
        when(advertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult(null, mHoaidStatus, mHoaidError));
        when(advertisingIdsHolder.getYandex()).thenReturn(new AdTrackingInfoResult(null, yandexStatus, yandexError));
        ClientIdentifiersHolder clientIdentifiersHolder = new ClientIdentifiersHolder(
                TestUtils.createDefaultStartupState(),
                advertisingIdsHolder,
                clientClids
        );
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(clientIdentifiersHolder.getGaid()).isEqualTo(new IdentifiersResult(null, mGaidStatus, mGaidError));
        softly.assertThat(clientIdentifiersHolder.getHoaid()).isEqualTo(new IdentifiersResult(null, mHoaidStatus, mHoaidError));
        softly.assertThat(clientIdentifiersHolder.getYandexAdvId()).isEqualTo(new IdentifiersResult(null, yandexStatus, yandexError));
        softly.assertAll();
    }

    @Test
    public void testStartup() throws Exception {
        final long obtainTime = 3476576;
        final int updateInterval = 4343;
        StartupState startupState = new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder()
                        .withSslPinning(true)
                        .build()
        )
                .withUuid(mUuid)
                .withDeviceId(mDeviceId)
                .withDeviceIdHash(mDeviceIdHash)
                .withReportAdUrl(mReportAdUrl)
                .withGetAdUrl(mGetAdUrl)
                .withCustomSdkHosts(customSdkHosts)
                .withEncodedClidsFromResponse(StartupUtils.encodeClids(mResponseClids))
                .withLastClientClidsForStartupRequest(StartupUtils.encodeClids(mRequestClids))
                .withAutoInappCollectingConfig(autoInappCollectingConfig)
                .withObtainTime(obtainTime)
                .withStartupUpdateConfig(new StartupUpdateConfig(updateInterval))
                .build();
        AdvertisingIdsHolder advertisingIdsHolder = mock(AdvertisingIdsHolder.class);
        when(advertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(
                new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, mGaidAdvId, false),
                mGaidStatus,
                mGaidError
        ));
        when(advertisingIdsHolder.getHuawei()).thenReturn(
                new AdTrackingInfoResult(new AdTrackingInfo(AdTrackingInfo.Provider.HMS, mHoaidAdvId, false),
                        mHoaidStatus,
                        mHoaidError
                ));
        when(advertisingIdsHolder.getYandex()).thenReturn(
                new AdTrackingInfoResult(new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, yandexAdvId, false),
                        yandexStatus,
                        yandexError
                ));
        ClientIdentifiersHolder clientIdentifiersHolder = new ClientIdentifiersHolder(startupState, advertisingIdsHolder, clientClids);
        ObjectPropertyAssertions<ClientIdentifiersHolder> assertions = ObjectPropertyAssertions(clientIdentifiersHolder).withPrivateFields(true);
        assertions.checkField("mUuidData", "getUuid", mUuidData);
        assertions.checkField("mDeviceIdData", "getDeviceId", mDeviceIdData);
        assertions.checkField("mDeviceIdHashData", "getDeviceIdHash", mDeviceIdHashData);
        assertions.checkField("mReportAdUrlData", "getReportAdUrl", mReportAdUrlData);
        assertions.checkField("mGetAdUrlData", "getGetAdUrl", mGetAdUrlData);
        assertions.checkField("mResponseClidsData", "getResponseClids", mResponseClidsData);
        assertions.checkField("mClientClidsForRequestData", "getClientClidsForRequest", mClientClidsForRequestData);
        assertions.checkField("mGaidData", "getGaid", mGaidData);
        assertions.checkField("mHoaidData", "getHoaid", mHoaidData);
        assertions.checkField("yandexAdvIdData", "getYandexAdvId", yandexAdvIdData);
        assertions.checkFieldRecursively("customSdkHostsData", new Consumer<ObjectPropertyAssertions<IdentifiersResult>>() {
            @Override
            public void accept(ObjectPropertyAssertions<IdentifiersResult> customSdkHostsAssertions) {
                try {
                    customSdkHostsAssertions.checkFieldMatchPredicate("id", new Predicate<String>() {
                        @Override
                        public boolean test(String s) {
                            try {
                                JSONAssert.assertEquals(JsonHelper.customSdkHostsToString(customSdkHosts), s, true);
                                return true;
                            } catch (Throwable ignored) {}
                            return false;
                        }
                    });
                    customSdkHostsAssertions.checkField("status", IdentifierStatus.OK);
                    customSdkHostsAssertions.checkFieldIsNull("errorExplanation");
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
        assertions.checkField("mServerTimeOffset", "getServerTimeOffset", mServerTimeOffset);
        assertions.checkField("nextStartupTime", "getNextStartupTime", obtainTime + updateInterval);
        assertions.<FeaturesInternal>checkFieldRecursively("features", new Consumer<ObjectPropertyAssertions<FeaturesInternal>>() {
            @Override
            public void accept(ObjectPropertyAssertions<FeaturesInternal> innerAssertions) {
                try {
                    innerAssertions.withPrivateFields(true).withFinalFieldOnly(false);
                    innerAssertions.checkField("sslPinning", "getSslPinning", true);
                    innerAssertions.checkField("status", "getStatus", IdentifierStatus.OK);
                    innerAssertions.checkFieldIsNull("errorExplanation", "getErrorExplanation");
                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        assertions.checkAll();
    }

    @Test
    public void testBundleSerializationFilled() throws Exception {
        ClientIdentifiersHolder clientIdentifiersHolder = new ClientIdentifiersHolder(
                mUuidData,
                mDeviceIdData,
                mDeviceIdHashData,
                mReportAdUrlData,
                mGetAdUrlData,
                mResponseClidsData,
                mClientClidsForRequestData,
                mGaidData, mHoaidData, yandexAdvIdData,
                customSdkHostsData,
                mServerTimeOffset,
                nextStartupTime,
                features
        );
        Bundle bundle = new Bundle();
        clientIdentifiersHolder.toBundle(bundle);
        ClientIdentifiersHolder actual = new ClientIdentifiersHolder(bundle);
        ObjectPropertyAssertions<ClientIdentifiersHolder> assertions = ObjectPropertyAssertions(actual).withPrivateFields(true);
        assertions.checkField("mUuidData", "getUuid", mUuidData);
        assertions.checkField("mDeviceIdData", "getDeviceId", mDeviceIdData);
        assertions.checkField("mDeviceIdHashData", "getDeviceIdHash", mDeviceIdHashData);
        assertions.checkField("mReportAdUrlData", "getReportAdUrl", mReportAdUrlData);
        assertions.checkField("mGetAdUrlData", "getGetAdUrl", mGetAdUrlData);
        assertions.checkField("mResponseClidsData", "getResponseClids", mResponseClidsData);
        assertions.checkField("mClientClidsForRequestData", "getClientClidsForRequest", mClientClidsForRequestData);
        assertions.checkField("mGaidData", "getGaid", mGaidData);
        assertions.checkField("mHoaidData", "getHoaid", mHoaidData);
        assertions.checkField("yandexAdvIdData", "getYandexAdvId", yandexAdvIdData);
        assertions.checkFieldRecursively("customSdkHostsData", new Consumer<ObjectPropertyAssertions<IdentifiersResult>>() {
            @Override
            public void accept(ObjectPropertyAssertions<IdentifiersResult> customSdkHostsAssertions) {
                try {
                    customSdkHostsAssertions.checkFieldMatchPredicate("id", new Predicate<String>() {
                        @Override
                        public boolean test(String s) {
                            try {
                                JSONAssert.assertEquals(JsonHelper.customSdkHostsToString(customSdkHosts), s, true);
                                return true;
                            } catch (Throwable ignored) {}
                            return false;
                        }
                    });
                    customSdkHostsAssertions.checkField("status", IdentifierStatus.OK);
                    customSdkHostsAssertions.checkFieldIsNull("errorExplanation");
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
        assertions.checkField("mServerTimeOffset", "getServerTimeOffset", mServerTimeOffset);
        assertions.checkField("nextStartupTime", "getNextStartupTime", nextStartupTime);
        assertions.checkField("features", "getFeatures", features);
        assertions.checkAll();
    }
}
