package io.appmetrica.analytics.impl.startup;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.impl.DefaultValues;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class StartupStateModelTest extends CommonTest {

    @Mock
    private CollectingFlags mCollectingFlags;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateFromBuilder() throws Exception {
        final String uuid = "uuid";
        final String deviceID = "12345678";
        final String deviceIDHash = "aaaaaaa";
        final List<String> reportUrls = Arrays.asList("url1", "url2");
        final String getAdUrl = "get.ad.url";
        final String reportAdUrl = "report.ad.url";
        final String certificateUrl = "certificate.url";
        final List<String> hostUrlsFromStartup = Arrays.asList("host.url1.fromstartup", "host.url2.fromstartup");
        final List<String> hostUrlsFromClient = Arrays.asList("host.url1.fromclient", "host.url2.fromclient");
        final List<String> diagnosticUrls = Arrays.asList("diagnostic.url1", "diagnostic.url2");
        final Map<String, List<String>> customSdkHosts = new HashMap<>();
        customSdkHosts.put("key1", Arrays.asList("host1", "host2"));
        customSdkHosts.put("key2", Arrays.asList("host3", "host4"));
        final String encodedClidsFromResponse = "clid0:0,clid1:1";
        final String lastClientClidsForStartupRequest = "clid1:1,clid2:2";
        final String lastChosenForRequestClids = "clid2:2,clid3:3";
        final long obtainTime = 4444;
        final boolean hadFirstStartup = true;
        final boolean startupDidNotOverrideClids = true;
        final String countryInit = "BY";
        final StatSending statSending = mock(StatSending.class);
        final PermissionsCollectingConfig permissionsCollectingConfig = mock(PermissionsCollectingConfig.class);
        final long obtainServerTime = 88888;
        final long firstStartupServerTime = 55555;
        final boolean outdated = true;
        final RetryPolicyConfig retryPolicyConfig = mock(RetryPolicyConfig.class);
        CacheControl cacheControl = mock(CacheControl.class);
        final BillingConfig autoInappCollectingConfig = mock(BillingConfig.class);
        final AttributionConfig attributionConfig = mock(AttributionConfig.class);
        final StartupUpdateConfig startupUpdateConfig = mock(StartupUpdateConfig.class);
        final Map<String, Object> modulesRemoteConfigs = Collections.singletonMap("Identifier", mock(Object.class));
        final ExternalAttributionConfig externalAttributionConfig = mock(ExternalAttributionConfig.class);

        StartupStateModel startupState = new StartupStateModel.StartupStateBuilder(mCollectingFlags)
            .withUuid(uuid)
            .withReportUrls(reportUrls)
            .withGetAdUrl(getAdUrl)
            .withReportAdUrl(reportAdUrl)
            .withCertificateUrl(certificateUrl)
            .withHostUrlsFromStartup(hostUrlsFromStartup)
            .withHostUrlsFromClient(hostUrlsFromClient)
            .withDiagnosticUrls(diagnosticUrls)
            .withEncodedClidsFromResponse(encodedClidsFromResponse)
            .withLastClientClidsForStartupRequest(lastClientClidsForStartupRequest)
            .withLastChosenForRequestClids(lastChosenForRequestClids)
            .withObtainTime(obtainTime)
            .withHadFirstStartup(hadFirstStartup)
            .withStartupDidNotOverrideClids(startupDidNotOverrideClids)
            .withCountryInit(countryInit)
            .withStatSending(statSending)
            .withPermissionsCollectingConfig(permissionsCollectingConfig)
            .withObtainServerTime(obtainServerTime)
            .withFirstStartupServerTime(firstStartupServerTime)
            .withOutdated(outdated)
            .withRetryPolicyConfig(retryPolicyConfig)
            .withCacheControl(cacheControl)
            .withAutoInappCollectingConfig(autoInappCollectingConfig)
            .withAttributionConfig(attributionConfig)
            .withCustomSdkHosts(customSdkHosts)
            .withStartupUpdateConfig(startupUpdateConfig)
            .withModulesRemoteConfigs(modulesRemoteConfigs)
            .withExternalAttributionConfig(externalAttributionConfig)
            .build();

        ObjectPropertyAssertions<StartupStateModel> assertions =
            ObjectPropertyAssertions(startupState)
                .withFinalFieldOnly(false);

        assertions.checkField("uuid", uuid);
        assertions.checkField("reportUrls", reportUrls);
        assertions.checkField("getAdUrl", getAdUrl);
        assertions.checkField("reportAdUrl", reportAdUrl);
        assertions.checkField("certificateUrl", certificateUrl);
        assertions.checkField("hostUrlsFromStartup", hostUrlsFromStartup);
        assertions.checkField("hostUrlsFromClient", hostUrlsFromClient);
        assertions.checkField("diagnosticUrls", diagnosticUrls);
        assertions.checkField("encodedClidsFromResponse", encodedClidsFromResponse);
        assertions.checkField("lastClientClidsForStartupRequest", lastClientClidsForStartupRequest);
        assertions.checkField("lastChosenForRequestClids", lastChosenForRequestClids);
        assertions.checkField("obtainTime", obtainTime);
        assertions.checkField("hadFirstStartup", hadFirstStartup);
        assertions.checkField("startupDidNotOverrideClids", startupDidNotOverrideClids);
        assertions.checkField("countryInit", countryInit);
        assertions.checkField("statSending", statSending);
        assertions.checkField("permissionsCollectingConfig", permissionsCollectingConfig);
        assertions.checkField("obtainServerTime", obtainServerTime);
        assertions.checkField("firstStartupServerTime", firstStartupServerTime);
        assertions.checkField("outdated", outdated);
        assertions.checkField("retryPolicyConfig", retryPolicyConfig);
        assertions.checkField("collectingFlags", mCollectingFlags);
        assertions.checkField("cacheControl", cacheControl);
        assertions.checkField("autoInappCollectingConfig", autoInappCollectingConfig);
        assertions.checkField("attributionConfig", attributionConfig);
        assertions.checkField("customSdkHosts", customSdkHosts);
        assertions.checkField("startupUpdateConfig", startupUpdateConfig);
        assertions.checkField("modulesRemoteConfigs", modulesRemoteConfigs);
        assertions.checkField("externalAttributionConfig", externalAttributionConfig);

        assertions.checkAll();
    }

    @Test
    public void testCreateFromEmptyConfig() throws Exception {
        RetryPolicyConfig defaultRetryPolicConfig = new RetryPolicyConfig(600, 1);
        StartupStateModel startupState = new StartupStateModel.StartupStateBuilder(mCollectingFlags).build();

        ObjectPropertyAssertions<StartupStateModel> assertions =
            ObjectPropertyAssertions(startupState)
                .withFinalFieldOnly(false);

        assertions.checkField("uuid", (String) null);
        assertions.checkField("reportUrls", (List) null);
        assertions.checkField("getAdUrl", (String) null);
        assertions.checkField("reportAdUrl", (String) null);
        assertions.checkField("certificateUrl", (String) null);
        assertions.checkField("hostUrlsFromStartup", (List) null);
        assertions.checkField("hostUrlsFromClient", (List) null);
        assertions.checkField("diagnosticUrls", (List) null);
        assertions.checkField("encodedClidsFromResponse", (String) null);
        assertions.checkField("lastClientClidsForStartupRequest", (String) null);
        assertions.checkField("lastChosenForRequestClids", (String) null);
        assertions.checkField("obtainTime", 0L);
        assertions.checkField("hadFirstStartup", false);
        assertions.checkField("startupDidNotOverrideClids", false);
        assertions.checkField("countryInit", (String) null);
        assertions.checkField("statSending", (StatSending) null);
        assertions.checkField("permissionsCollectingConfig", (PermissionsCollectingConfig) null);
        assertions.checkField("obtainServerTime", 0L);
        assertions.checkField("firstStartupServerTime", 0L);
        assertions.checkField("outdated", false);
        assertions.checkField("retryPolicyConfig", defaultRetryPolicConfig);
        assertions.checkField("collectingFlags", mCollectingFlags);
        assertions.checkField("cacheControl", (CacheControl) null);
        assertions.checkField("autoInappCollectingConfig", null);
        assertions.checkFieldIsNull("attributionConfig");
        assertions.checkFieldIsNull("customSdkHosts");
        assertions.checkFieldComparingFieldByFieldRecursively("startupUpdateConfig",
            new StartupUpdateConfig(DefaultValues.STARTUP_UPDATE_CONFIG.interval));
        assertions.checkField("modulesRemoteConfigs", Collections.emptyMap());
        assertions.checkField("externalAttributionConfig", (ExternalAttributionConfig) null);
        assertions.checkAll();
    }

    @Test
    public void testBuildUpon() throws Exception {
        final String uuid = "uuid";
        final String deviceID = "12345678";
        final String deviceIDHash = "aaaaaaa";
        final List<String> reportUrls = Arrays.asList("url1", "url2");
        final String getAdUrl = "get.ad.url";
        final String reportAdUrl = "report.ad.url";
        final String certificateUrl = "certificate.url";
        final List<String> hostUrlsFromStartup = Arrays.asList("host.url1.fromstartup", "host.url2.fromstartup");
        final List<String> hostUrlsFromClient = Arrays.asList("host.url1.fromclient", "host.url2.fromclient");
        final List<String> diagnosticUrls = Arrays.asList("diagnostic.url1", "diagnostic.url2");
        final Map<String, List<String>> customSdkHosts = new HashMap<>();
        customSdkHosts.put("key1", Arrays.asList("host1", "host2"));
        customSdkHosts.put("key2", Arrays.asList("host3", "host4"));
        final String encodedClidsFromResponse = "clid0:0,clid1:1";
        final String lastClientClidsForStartupRequest = "clid1:1,clid2:2";
        final String lastChosenForRequestClids = "clid2:2,clid3:3";
        final long obtainTime = 4444;
        final boolean hadFirstStartup = true;
        final boolean startupDidNotOverrideClids = true;
        final String countryInit = "BY";
        final StatSending statSending = mock(StatSending.class);
        final PermissionsCollectingConfig permissionsCollectingConfig = mock(PermissionsCollectingConfig.class);
        final long obtainServerTime = 88888;
        final long firstStartupServerTime = 55555;
        final boolean outdated = true;
        final RetryPolicyConfig retryPolicyConfig = mock(RetryPolicyConfig.class);
        CacheControl cacheControl = mock(CacheControl.class);
        final BillingConfig autoInappCollectingConfig = mock(BillingConfig.class);
        final AttributionConfig attributionConfig = mock(AttributionConfig.class);
        final StartupUpdateConfig startupUpdateConfig = mock(StartupUpdateConfig.class);
        final Map<String, Object> modulesRemoteConfigs = Collections.singletonMap("Identifier", mock(Object.class));
        final ExternalAttributionConfig externalAttributionConfig = mock(ExternalAttributionConfig.class);

        @SuppressWarnings("deprecation")
        StartupStateModel startupState = new StartupStateModel.StartupStateBuilder(mCollectingFlags)
            .withUuid(uuid)
            .withReportUrls(reportUrls)
            .withGetAdUrl(getAdUrl)
            .withReportAdUrl(reportAdUrl)
            .withCertificateUrl(certificateUrl)
            .withHostUrlsFromStartup(hostUrlsFromStartup)
            .withHostUrlsFromClient(hostUrlsFromClient)
            .withDiagnosticUrls(diagnosticUrls)
            .withEncodedClidsFromResponse(encodedClidsFromResponse)
            .withLastClientClidsForStartupRequest(lastClientClidsForStartupRequest)
            .withLastChosenForRequestClids(lastChosenForRequestClids)
            .withObtainTime(obtainTime)
            .withHadFirstStartup(hadFirstStartup)
            .withStartupDidNotOverrideClids(startupDidNotOverrideClids)
            .withCountryInit(countryInit)
            .withStatSending(statSending)
            .withPermissionsCollectingConfig(permissionsCollectingConfig)
            .withObtainServerTime(obtainServerTime)
            .withFirstStartupServerTime(firstStartupServerTime)
            .withOutdated(outdated)
            .withRetryPolicyConfig(retryPolicyConfig)
            .withCacheControl(cacheControl)
            .withAutoInappCollectingConfig(autoInappCollectingConfig)
            .withAttributionConfig(attributionConfig)
            .withCustomSdkHosts(customSdkHosts)
            .withStartupUpdateConfig(startupUpdateConfig)
            .withModulesRemoteConfigs(modulesRemoteConfigs)
            .withExternalAttributionConfig(externalAttributionConfig)
            .build()
            .buildUpon()
            .build();

        ObjectPropertyAssertions<StartupStateModel> assertions =
            ObjectPropertyAssertions(startupState)
                .withFinalFieldOnly(false);

        assertions.checkField("uuid", uuid);
        assertions.checkField("reportUrls", reportUrls);
        assertions.checkField("getAdUrl", getAdUrl);
        assertions.checkField("reportAdUrl", reportAdUrl);
        assertions.checkField("certificateUrl", certificateUrl);
        assertions.checkField("hostUrlsFromStartup", hostUrlsFromStartup);
        assertions.checkField("hostUrlsFromClient", hostUrlsFromClient);
        assertions.checkField("diagnosticUrls", diagnosticUrls);
        assertions.checkField("encodedClidsFromResponse", encodedClidsFromResponse);
        assertions.checkField("lastClientClidsForStartupRequest", lastClientClidsForStartupRequest);
        assertions.checkField("lastChosenForRequestClids", lastChosenForRequestClids);
        assertions.checkField("obtainTime", obtainTime);
        assertions.checkField("hadFirstStartup", hadFirstStartup);
        assertions.checkField("startupDidNotOverrideClids", startupDidNotOverrideClids);
        assertions.checkField("countryInit", countryInit);
        assertions.checkField("statSending", statSending);
        assertions.checkField("permissionsCollectingConfig", permissionsCollectingConfig);
        assertions.checkField("obtainServerTime", obtainServerTime);
        assertions.checkField("firstStartupServerTime", firstStartupServerTime);
        assertions.checkField("outdated", outdated);
        assertions.checkField("retryPolicyConfig", retryPolicyConfig);
        assertions.checkField("collectingFlags", mCollectingFlags);
        assertions.checkField("cacheControl", cacheControl);
        assertions.checkField("autoInappCollectingConfig", autoInappCollectingConfig);
        assertions.checkField("attributionConfig", attributionConfig);
        assertions.checkField("customSdkHosts", customSdkHosts);
        assertions.checkField("startupUpdateConfig", startupUpdateConfig);
        assertions.checkField("modulesRemoteConfigs", modulesRemoteConfigs);
        assertions.checkField("externalAttributionConfig", externalAttributionConfig);

        assertions.checkAll();
    }
}
