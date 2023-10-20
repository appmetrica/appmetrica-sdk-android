package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.parsing.StartupParser;
import io.appmetrica.analytics.impl.startup.parsing.StartupResult;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.bytebuddy.utility.RandomString;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupUnitGeneralTest extends StartupUnitBaseTest {

    @Override
    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void testRetryPolicyConfig() {
        StartupState startupState = mStartupUnit.parseStartupResult(new StartupParser().parseStartupResponse(TEST_RESPONSE.getBytes()), mStartupRequestConfig, 0L);
        assertThat(startupState.getRetryPolicyConfig()).isEqualTo(new RetryPolicyConfig(1000, 2));
    }

    @Test
    public void testRetryPolicyConfigNull() throws JSONException {
        JSONObject responseWithoutRetryPolicy = new JSONObject(TEST_RESPONSE);
        responseWithoutRetryPolicy.remove("retry_policy");
        StartupState startupState = mStartupUnit.parseStartupResult(new StartupParser().parseStartupResponse(responseWithoutRetryPolicy.toString().getBytes()), mStartupRequestConfig, 0L);
        assertThat(startupState.getRetryPolicyConfig()).isEqualTo(new RetryPolicyConfig(600, 1));
    }

    @Test
    public void testStartupError() {
        StartupState startupState = mock(StartupState.class);
        mStartupUnit.setStartupState(startupState);
        StartupError error = StartupError.NETWORK;
        mStartupUnit.onRequestError(error);
        verify(mStartupResultListener).onStartupError(RuntimeEnvironment.getApplication().getPackageName(), error, startupState);
    }

    @Test
    public void testParseStartupResult() throws Exception {
        StartupResult result = mock(StartupResult.class);
        StartupRequestConfig startupRequestConfig = mock(StartupRequestConfig.class);
        long serverTime = 324653726;

        final String uuid = RandomString.make(10);
        final String deviceId = RandomString.make(10);
        final String deviceIdHash = RandomString.make(10);
        final List<String> reportUrls = Arrays.asList("report url");
        final List<String> diagnosticUrls = Arrays.asList("diagnostic url");
        final List<String> hostUrlsFromStartup = Arrays.asList("startup url");
        final List<String> hostUrlsFromClient = Arrays.asList("client startup url");
        final String getAdUrl = "some.get.tst.url";
        final String reportAdUrl = "some.report.tst.url";
        final String certificateUrl = "certificate.url";
        final String encodedClids = "clid0:0,clid1:1";
        final CollectingFlags collectingFlags = mock(CollectingFlags.class);
        final Map<String, String> clientClids = new HashMap<String, String>();
        clientClids.put("clid0", "10");
        final Map<String, String> chosenClids = new HashMap<String, String>();
        chosenClids.put("clid1", "20");
        final Map<String, List<String>> customSdkHosts = new HashMap<>();
        customSdkHosts.put("key1", Arrays.asList("host1", "host2"));
        customSdkHosts.put("key2", Arrays.asList("host3", "host4"));
        final String countryInit = "by";
        final StatSending statSending = mock(StatSending.class);
        final PermissionsCollectingConfig permissionsCollectingConfig = mock(PermissionsCollectingConfig.class);
        final RetryPolicyConfig retryPolicyConfig = mock(RetryPolicyConfig.class);
        final long firstStartupObtainTime = 4385768;
        final BillingConfig autoInappCollectingConfig = mock(BillingConfig.class);
        long obtainTime = 124326487324L;
        doReturn(obtainTime).when(timeProvider).currentTimeSeconds();
        CacheControl cacheControl = mock(CacheControl.class);
        AttributionConfig attributionConfig = mock(AttributionConfig.class);
        StartupUpdateConfig startupUpdateConfig = mock(StartupUpdateConfig.class);
        Map<String, Object> modulesRemoteConfigs = Collections.singletonMap("String", mock(Object.class));

        when(result.getDeviceId()).thenReturn(deviceId);
        when(result.getDeviceIDHash()).thenReturn(deviceIdHash);
        StartupRequestConfig oldStartupRequestConfig = mock(StartupRequestConfig.class);
        when(oldStartupRequestConfig.getOrSetFirstStartupTime(anyLong())).thenReturn(firstStartupObtainTime);
        when(mConfigurationHolder.get()).thenReturn(oldStartupRequestConfig);
        when(mConfigurationHolder.getStartupState()).thenReturn(
            new StartupState.Builder(mock(CollectingFlags.class))
                .withUuid(uuid)
                .build()
        );
        when(result.getReportHostUrls()).thenReturn(reportUrls);
        when(result.getGetAdUrl()).thenReturn(getAdUrl);
        when(result.getReportAdUrl()).thenReturn(reportAdUrl);
        when(result.getCertificateUrl()).thenReturn(certificateUrl);
        when(result.getStartupUrls()).thenReturn(hostUrlsFromStartup);
        when(result.getDiagnosticUrls()).thenReturn(diagnosticUrls);
        when(startupRequestConfig.getStartupHostsFromClient()).thenReturn(hostUrlsFromClient);
        when(result.getEncodedClids()).thenReturn(encodedClids);
        when(result.getCollectionFlags()).thenReturn(collectingFlags);
        when(startupRequestConfig.getClidsFromClient()).thenReturn(clientClids);
        when(startupRequestConfig.getChosenClids()).thenReturn(new ClidsInfo.Candidate(chosenClids, DistributionSource.APP));
        when(result.getCountryInit()).thenReturn(countryInit);
        when(result.getStatSending()).thenReturn(statSending);
        when(result.getPermissionsCollectingConfig()).thenReturn(permissionsCollectingConfig);
        when(result.getRetryPolicyConfig()).thenReturn(retryPolicyConfig);
        when(result.getCacheControl()).thenReturn(cacheControl);
        when(result.getAutoInappCollectingConfig()).thenReturn(autoInappCollectingConfig);
        when(result.getAttributionConfig()).thenReturn(attributionConfig);
        when(result.getCustomSdkHosts()).thenReturn(customSdkHosts);
        when(result.getStartupUpdateConfig()).thenReturn(startupUpdateConfig);
        when(result.getModulesRemoteConfigs()).thenReturn(modulesRemoteConfigs);

        StartupState startupState = mStartupUnit.parseStartupResult(result, startupRequestConfig, serverTime);
        ObjectPropertyAssertions<StartupState> assertions = ObjectPropertyAssertions(startupState)
            .withIgnoredFields("obtainTime", "startupStateModel");
        assertions.checkField("uuid", uuid);
        assertions.checkField("deviceId", deviceId);
        assertions.checkField("deviceIdHash", deviceIdHash);
        assertions.checkField("reportUrls", reportUrls);
        assertions.checkField("getAdUrl", getAdUrl);
        assertions.checkField("reportAdUrl", reportAdUrl);
        assertions.checkField("certificateUrl", certificateUrl);
        assertions.checkField("hostUrlsFromStartup", hostUrlsFromStartup);
        assertions.checkField("hostUrlsFromClient", hostUrlsFromClient);
        assertions.checkField("diagnosticUrls", diagnosticUrls);
        assertions.checkField("encodedClidsFromResponse", encodedClids);
        assertions.checkField("lastClientClidsForStartupRequest", StartupUtils.encodeClids(clientClids));
        assertions.checkField("lastChosenForRequestClids", StartupUtils.encodeClids(chosenClids));
        assertions.checkField("collectingFlags", collectingFlags);
        assertions.checkField("hadFirstStartup", true);
        assertions.checkField("startupDidNotOverrideClids", false);
        assertions.checkField("countryInit", countryInit);
        assertions.checkField("statSending", statSending);
        assertions.checkField("permissionsCollectingConfig", permissionsCollectingConfig);
        assertions.checkField("retryPolicyConfig", retryPolicyConfig);
        assertions.checkField("obtainServerTime", serverTime);
        assertions.checkField("firstStartupServerTime", firstStartupObtainTime);
        assertions.checkField("outdated", false);
        assertions.checkField("cacheControl", cacheControl);
        assertions.checkField("autoInappCollectingConfig", autoInappCollectingConfig);
        assertions.checkField("attributionConfig", attributionConfig);
        assertions.checkField("customSdkHosts", customSdkHosts);
        assertions.checkField("startupUpdateConfig", startupUpdateConfig);
        assertions.checkField("modulesRemoteConfigs", modulesRemoteConfigs);
        assertions.checkAll();

        assertThat(startupState.getObtainTime()).isEqualTo(obtainTime);
    }

    @Test
    public void testHadFirstStartup() {
        StartupResult result = mock(StartupResult.class);
        doReturn(mock(CollectingFlags.class)).when(result).getCollectionFlags();
        assertThat(
            mStartupUnit.parseStartupResult(
                result, mockedStartupRequestConfig(), 0L
            ).getHadFirstStartup()
        ).isTrue();
    }

    @Test
    public void testServerTime() {
        StartupResult result = mock(StartupResult.class);
        doReturn(mock(CollectingFlags.class)).when(result).getCollectionFlags();
        assertThat(
            mStartupUnit.parseStartupResult(
                result, mockedStartupRequestConfig(), mObtainServerTime
            ).getObtainServerTime()
        ).isEqualTo(mObtainServerTime);
    }

    @Test
    public void testFirstStartupServerTime() {
        StartupResult result = mock(StartupResult.class);
        doReturn(mock(CollectingFlags.class)).when(result).getCollectionFlags();
        when(mStartupRequestConfig.getOrSetFirstStartupTime(anyLong())).thenReturn(mFirstStartupServerTime);
        assertThat(
            mStartupUnit.parseStartupResult(
                result, mockedStartupRequestConfig(), 0L
            ).getFirstStartupServerTime()
        ).isEqualTo(mFirstStartupServerTime);
    }

    @Test
    public void testOutdatedStartup() {
        mConfigurationHolder.updateStartupState(
            mConfigurationHolder.getStartupState()
                .buildUpon()
                .withOutdated(true)
                .build()
        );
        assertThat(mStartupUnit.isStartupRequired()).isTrue();
    }

    @Test
    public void testUuidNotParsed() {
        String uuid = "uuid";
        mConfigurationHolder.updateStartupState(
            mConfigurationHolder.getStartupState()
                .buildUpon()
                .withUuid(uuid)
                .build()
        );
        StartupResult result = mock(StartupResult.class);
        when(result.getCollectionFlags()).thenReturn(mock(CollectingFlags.class));
        StartupState startupState = mStartupUnit.parseStartupResult(result, mStartupRequestConfig, 0l);
        assertThat(startupState.getUuid()).isEqualTo(uuid);
    }

    @Test
    public void testEmptyDeviceIDReplacedWithDefault() {
        String deviceID = "deviceID";
        mConfigurationHolder.updateStartupState(
            mConfigurationHolder.getStartupState()
                .buildUpon()
                .withDeviceId(deviceID)
                .build()
        );
        StartupResult result = mock(StartupResult.class);
        doReturn(null).when(result).getDeviceId();
        doReturn(mock(CollectingFlags.class)).when(result).getCollectionFlags();
        StartupState startupState = mStartupUnit.parseStartupResult(result, mStartupRequestConfig, 0l);
        assertThat(startupState.getDeviceId()).isEqualTo(deviceID);
    }

    @Test
    public void testDeviceIDNotParcedIfPresent() {
        String deviceIDOld = "deviceIDOld";
        String deviceIDNew = "deviceIDNew";
        mConfigurationHolder.updateStartupState(
            mConfigurationHolder.getStartupState()
                .buildUpon()
                .withDeviceId(deviceIDOld)
                .build()
        );
        StartupResult result = mock(StartupResult.class);
        doReturn(deviceIDNew).when(result).getDeviceId();
        doReturn(mock(CollectingFlags.class)).when(result).getCollectionFlags();
        StartupState startupState = mStartupUnit.parseStartupResult(result, mStartupRequestConfig, 0l);
        assertThat(startupState.getDeviceId()).isEqualTo(deviceIDOld);
    }

    @Test
    public void testUseDeviceIDHashFromResponse() {
        String deviceIDHash = "deviceIDHash";
        String deviceIDHash2 = "deviceIDHash2";
        mConfigurationHolder.updateStartupState(
            mConfigurationHolder.getStartupState()
                .buildUpon()
                .withDeviceIdHash(deviceIDHash)
                .build()
        );
        StartupResult result = mock(StartupResult.class);
        doReturn(deviceIDHash2).when(result).getDeviceIDHash();
        when(result.getCollectionFlags()).thenReturn(mock(CollectingFlags.class));
        StartupState startupState = mStartupUnit.parseStartupResult(result, mStartupRequestConfig, 0l);
        assertThat(startupState.getDeviceIdHash()).isEqualTo(deviceIDHash2);
    }

    private StartupRequestConfig mockedStartupRequestConfig() {
        StartupRequestConfig config = mock(StartupRequestConfig.class);
        when(config.getChosenClids()).thenReturn(mock(ClidsInfo.Candidate.class));
        return config;
    }
}
