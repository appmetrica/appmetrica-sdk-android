package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.FeaturesInternal;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.impl.startup.StartupUpdateConfig;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.skyscreamer.jsonassert.JSONAssert;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClientIdentifiersProviderTest extends CommonTest {

    private ClientIdentifiersProvider mClientIdentifiersProvider;

    private final String uuid = "test uuid";
    private final String deviceId = "test device id";
    private final String deviceIdHash = "test device id hash";
    private final String reportAdUrl = "test report ad url";
    private final String getAdUrl = "test get ad url";
    private final Map<String, String> startupClientClidsForRequest = new HashMap<String, String>();
    private final Map<String, String> clientClidsForRequest = new HashMap<String, String>();
    private final Map<String, String> responseClids = new HashMap<String, String>();
    private final Map<String, List<String>> customSdkHosts = new HashMap<>();
    @Mock
    private StartupUnit mStartupUnit;
    @Mock
    private AdvertisingIdGetter mAdvertisingIdGetter;
    private final long obtainTime = 32847687;
    private final int updateInterval = 2173654;
    private BillingConfig autoInappCollectingConfig;
    private final String mGaid = "some gaid";
    private final String mHoaid = "some hoaid";
    private final String yandexAdvId = "some yandex adv_id";
    private final IdentifierStatus mGaidStatus = IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE;
    private final IdentifierStatus mHoaidStatus = IdentifierStatus.FEATURE_DISABLED;
    private final IdentifierStatus yandexStatus = IdentifierStatus.INVALID_ADV_ID;
    private String mGaidError = "gaid error";
    private String mHoaidError = "hoaid error";
    private String yandexError = "yandex error";
    private Context mContext;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        AdTrackingInfoResult googleResult = new AdTrackingInfoResult(new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, mGaid, false), mGaidStatus, mGaidError);
        AdTrackingInfoResult huaweiResult = new AdTrackingInfoResult(new AdTrackingInfo(AdTrackingInfo.Provider.HMS, mHoaid, false), mHoaidStatus, mHoaidError);
        AdTrackingInfoResult yandexResult = new AdTrackingInfoResult(new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, yandexAdvId, false), yandexStatus, yandexError);
        autoInappCollectingConfig = new BillingConfig(1, 2);
        clientClidsForRequest.put("clid0", "0");
        clientClidsForRequest.put("clid1", "1");
        startupClientClidsForRequest.put("clid0", "0");
        startupClientClidsForRequest.put("clid1", "1");
        responseClids.put("clid2", "2");
        responseClids.put("clid3", "3");
        customSdkHosts.put("ad", Arrays.asList("host1", "host2"));
        customSdkHosts.put("am", Arrays.asList("host3"));
        StartupState startupState = new StartupState.Builder(
                new CollectingFlags.CollectingFlagsBuilder()
                        .withSslPinning(false)
                        .build()
        )
                .withUuid(uuid)
                .withDeviceId(deviceId)
                .withDeviceIdHash(deviceIdHash)
                .withReportAdUrl(reportAdUrl)
                .withGetAdUrl(getAdUrl)
                .withCustomSdkHosts(customSdkHosts)
                .withEncodedClidsFromResponse(StartupUtils.encodeClids(responseClids))
                .withLastClientClidsForStartupRequest(StartupUtils.encodeClids(startupClientClidsForRequest))
                .withAutoInappCollectingConfig(autoInappCollectingConfig)
                .withObtainTime(obtainTime)
                .withStartupUpdateConfig(new StartupUpdateConfig(updateInterval))
                .build();
        when(mStartupUnit.getStartupState()).thenReturn(startupState);
        when(mAdvertisingIdGetter.getIdentifiersForced(mContext))
                .thenReturn(new AdvertisingIdsHolder(googleResult, huaweiResult, yandexResult));
        mClientIdentifiersProvider = new ClientIdentifiersProvider(mStartupUnit, mAdvertisingIdGetter, mContext);
    }

    @Test
    public void testCreateClientIdentifiersHolder() throws Exception {
        ClientIdentifiersHolder clientIdentifiersHolder = mClientIdentifiersProvider.createClientIdentifiersHolder(clientClidsForRequest);
        ObjectPropertyAssertions<ClientIdentifiersHolder> assertions = ObjectPropertyAssertions(clientIdentifiersHolder)
                .withPrivateFields(true)
                .withIgnoredFields("mServerTimeOffset", "modulesConfig");
        assertions.checkField("mUuidData", "getUuid", new IdentifiersResult(uuid, IdentifierStatus.OK, null));
        assertions.checkField("mDeviceIdData", "getDeviceId", new IdentifiersResult(deviceId, IdentifierStatus.OK, null));
        assertions.checkField("mDeviceIdHashData", "getDeviceIdHash", new IdentifiersResult(deviceIdHash, IdentifierStatus.OK, null));
        assertions.checkField("mReportAdUrlData", "getReportAdUrl", new IdentifiersResult(reportAdUrl, IdentifierStatus.OK, null));
        assertions.checkField("mGetAdUrlData", "getGetAdUrl", new IdentifiersResult(getAdUrl, IdentifierStatus.OK, null));
        assertions.checkField("mResponseClidsData", "getResponseClids", new IdentifiersResult(JsonHelper.clidsToString(responseClids), IdentifierStatus.OK, null));
        assertions.checkField("mClientClidsForRequestData", "getClientClidsForRequest", new IdentifiersResult(JsonHelper.clidsToString(clientClidsForRequest), IdentifierStatus.OK, null));
        assertions.checkField("mGaidData", "getGaid", new IdentifiersResult(mGaid, mGaidStatus, mGaidError));
        assertions.checkField("mHoaidData", "getHoaid", new IdentifiersResult(mHoaid, mHoaidStatus, mHoaidError));
        assertions.checkField("yandexAdvIdData", "getYandexAdvId", new IdentifiersResult(yandexAdvId, yandexStatus, yandexError));
        assertions.checkFieldRecursively("customSdkHostsData", "getCustomSdkHosts", new Consumer<ObjectPropertyAssertions<IdentifiersResult>>() {
            @Override
            public void accept(ObjectPropertyAssertions<IdentifiersResult> featuresAssertions) {
                try {
                    featuresAssertions.checkFieldMatchPredicate("id", new Predicate<String>() {
                        @Override
                        public boolean test(String s) {
                            try {
                                JSONAssert.assertEquals(JsonHelper.customSdkHostsToString(customSdkHosts), s, true);
                                return true;
                            } catch (Throwable ignored) { }
                            return false;
                        }
                    });
                    featuresAssertions.checkField("status", IdentifierStatus.OK);
                    featuresAssertions.checkFieldIsNull("errorExplanation");
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
        assertions.checkField("nextStartupTime", "getNextStartupTime", obtainTime + updateInterval);
        assertions.checkFieldRecursively("features", new Consumer<ObjectPropertyAssertions<FeaturesInternal>>() {
            @Override
            public void accept(ObjectPropertyAssertions<FeaturesInternal> featuresAssertions) {
                featuresAssertions.withFinalFieldOnly(false).withPrivateFields(true);
                try {
                    featuresAssertions.checkField("sslPinning", "getSslPinning", false);
                    featuresAssertions.checkField("status", "getStatus", IdentifierStatus.OK);
                    featuresAssertions.checkFieldIsNull("errorExplanation", "getErrorExplanation");
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
        assertions.checkAll();
    }

    @Test
    public void testStartupChangedInStartupUnit() throws Exception {
        final String newUuid = "new uuid";
        final String newDeviceId = "new deviceId";
        final String newDeviceIdHash = "new deviceIdHash";
        final String newReportAdUrl = "new report.ad.url";
        final String newGetAdUrl = "new get.ad.url";
        final Map<String, String> newResponseClids = new HashMap<String, String>();
        newResponseClids.put("newclid0", "0");
        newResponseClids.put("newclid1", "1");
        final Map<String, String> newRequestClids = new HashMap<String, String>();
        newRequestClids.put("newclid2", "2");
        newRequestClids.put("newclid3", "3");
        final Map<String, List<String>> newCustomSdkHosts = new HashMap<>();
        newCustomSdkHosts.put("new_key", Arrays.asList("new.host1", "new.host2"));
        final long newObtainTime = 77788844;
        final int newUpdateInterval = 5656;

        StartupState newState = new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder()
                .withSslPinning(true)
                .build()
        )
                .withUuid(newUuid)
                .withDeviceId(newDeviceId)
                .withDeviceIdHash(newDeviceIdHash)
                .withReportAdUrl(newReportAdUrl)
                .withGetAdUrl(newGetAdUrl)
                .withCustomSdkHosts(newCustomSdkHosts)
                .withEncodedClidsFromResponse(StartupUtils.encodeClids(newResponseClids))
                .withLastClientClidsForStartupRequest(StartupUtils.encodeClids(newRequestClids))
                .withObtainTime(newObtainTime)
                .withStartupUpdateConfig(new StartupUpdateConfig(newUpdateInterval))
                .build();
        when(mStartupUnit.getStartupState()).thenReturn(newState);
        ClientIdentifiersHolder clientIdentifiersHolder = mClientIdentifiersProvider.createClientIdentifiersHolder(clientClidsForRequest);
        ObjectPropertyAssertions<ClientIdentifiersHolder> assertions = ObjectPropertyAssertions(clientIdentifiersHolder)
                .withPrivateFields(true)
                .withIgnoredFields("mServerTimeOffset", "modulesConfig");
        assertions.checkField("mUuidData", "getUuid", new IdentifiersResult(newUuid, IdentifierStatus.OK, null));
        assertions.checkField("mDeviceIdData", "getDeviceId", new IdentifiersResult(newDeviceId, IdentifierStatus.OK, null));
        assertions.checkField("mDeviceIdHashData", "getDeviceIdHash", new IdentifiersResult(newDeviceIdHash, IdentifierStatus.OK, null));
        assertions.checkField("mReportAdUrlData", "getReportAdUrl", new IdentifiersResult(newReportAdUrl, IdentifierStatus.OK, null));
        assertions.checkField("mGetAdUrlData", "getGetAdUrl", new IdentifiersResult(newGetAdUrl, IdentifierStatus.OK, null));
        assertions.checkField("mResponseClidsData", "getResponseClids", new IdentifiersResult(JsonHelper.clidsToString(newResponseClids), IdentifierStatus.OK, null));
        assertions.checkField("mClientClidsForRequestData", "getClientClidsForRequest", new IdentifiersResult(JsonHelper.clidsToString(clientClidsForRequest), IdentifierStatus.OK, null));
        assertions.checkField("mGaidData", "getGaid", new IdentifiersResult(mGaid, mGaidStatus, mGaidError));
        assertions.checkField("mHoaidData", "getHoaid", new IdentifiersResult(mHoaid, mHoaidStatus, mHoaidError));
        assertions.checkField("yandexAdvIdData", "getYandexAdvId", new IdentifiersResult(yandexAdvId, yandexStatus, yandexError));
        assertions.checkField("customSdkHostsData", "getCustomSdkHosts", new IdentifiersResult(
                JsonHelper.customSdkHostsToString(newCustomSdkHosts),
                IdentifierStatus.OK,
                null
        ));
        assertions.checkField("nextStartupTime", "getNextStartupTime", newObtainTime + newUpdateInterval);
        assertions.checkFieldRecursively("features", new Consumer<ObjectPropertyAssertions<FeaturesInternal>>() {
            @Override
            public void accept(ObjectPropertyAssertions<FeaturesInternal> featuresAssertions) {
                featuresAssertions.withFinalFieldOnly(false).withPrivateFields(true);
                try {
                    featuresAssertions.checkField("sslPinning", "getSslPinning", true);
                    featuresAssertions.checkField("status", "getStatus", IdentifierStatus.OK);
                    featuresAssertions.checkFieldIsNull("errorExplanation", "getErrorExplanation");
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
        assertions.checkAll();
    }

    @Test
    public void emptyCustomSdkHosts() {
        when(mStartupUnit.getStartupState()).thenReturn(new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().build()).build());
        ClientIdentifiersHolder clientIdentifiersHolder = mClientIdentifiersProvider.createClientIdentifiersHolder(clientClidsForRequest);
        assertThat(clientIdentifiersHolder.getCustomSdkHosts()).isEqualToComparingFieldByField(new IdentifiersResult(
                null,
                IdentifierStatus.UNKNOWN,
                "no identifier in startup state"
        ));
    }

    @Test
    public void absentSslPinning() {
        when(mStartupUnit.getStartupState()).thenReturn(new StartupState.Builder(new CollectingFlags.CollectingFlagsBuilder().build()).build());
        ClientIdentifiersHolder clientIdentifiersHolder = mClientIdentifiersProvider.createClientIdentifiersHolder(clientClidsForRequest);
        assertThat(clientIdentifiersHolder.getFeatures()).isEqualToComparingFieldByField(new FeaturesInternal(
                null,
                IdentifierStatus.UNKNOWN,
                "no identifier in startup state"
        ));
    }
}
