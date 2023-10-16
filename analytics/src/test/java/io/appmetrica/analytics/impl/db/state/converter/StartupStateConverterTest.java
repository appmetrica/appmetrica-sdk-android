package io.appmetrica.analytics.impl.db.state.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf;
import io.appmetrica.analytics.impl.startup.AttributionConfig;
import io.appmetrica.analytics.impl.startup.CacheControl;
import io.appmetrica.analytics.impl.startup.CollectingFlags;
import io.appmetrica.analytics.impl.startup.PermissionsCollectingConfig;
import io.appmetrica.analytics.impl.startup.StartupStateModel;
import io.appmetrica.analytics.impl.startup.StartupUpdateConfig;
import io.appmetrica.analytics.impl.startup.StatSending;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupStateConverterTest extends CommonTest {

    private static final boolean OUTDATED = true;
    @Mock
    private FlagsConverter mFlagsConverter;
    @Mock
    private StatSendingConverter mStatSendingConverter;
    @Mock
    private PermissionsCollectingConfigConverter mPermissionsCollectingConfigConverter;
    @Mock
    private CacheControlConverter mCacheControlConverter;
    @Mock
    private AutoInappCollectingConfigConverter autoInappCollectingConfigConverter;
    @Mock
    private AttributionConfigConverter attributionConfigConverter;
    @Mock
    private StartupUpdateConfigConverter startupUpdateConfigConverter;

    @Mock
    private CacheControl mCacheControl;
    @Mock
    private StartupStateProtobuf.StartupState.CacheControl mNanoCacheControl;
    @Mock
    private BillingConfig autoInappCollectingConfig;
    @Mock
    private StartupStateProtobuf.StartupState.AutoInappCollectingConfig autoInappCollectingNanoConfig;
    @Mock
    private AttributionConfig attributionConfig;
    @Mock
    private StartupStateProtobuf.StartupState.Attribution attributionNanoConfig;
    @Mock
    private CustomSdkHostsConverter customSdkHostsConverter;
    @Mock
    private StartupUpdateConfig startupUpdateConfig;
    @Mock
    private StartupStateProtobuf.StartupState.StartupUpdateConfig startupUpdateNanoConfig;
    @Mock
    private ModulesRemoteConfigsConverter modulesRemoteConfigsConverter;

    @InjectMocks
    private StartupStateConverter mConverter;

    private static final long OBTAIN_SERVER_TIME = 123456789;
    private static final String UUID = "testUuid";
    private static final String DEVICE_ID = "deviceID";
    private static final String DEVICE_ID_HASH = "deviceIDHash";
    private static final long OBTAIN_TIME = 11;
    private static final boolean HAD_FIRST_STARTUP = true;
    private static final String[] REPORT_URLS = new String[]{"reportUrl1", "reportUrl2"};
    private static final String GET_AD_URL = "testGetAdUrl";
    private static final String CERTIFICATE_URL = "testCertificateUrl";
    private static final String REPORT_AD_URL = "testReport";
    private static final String[] DIAGNOSTIC_URLS = new String[]{"diagnosticUrl1", "diagnosticUrl2"};
    private static final String[] HOST_URLS_FROM_STARTUP = new String[]{"fromStartup1", "fromStartup2"};
    private static final String[] HOST_URLS_FROM_CLIENT = new String[]{"fromClient1", "fromClient2"};
    private static final String ENCODED_CLIDS_FROM_RESPONSE = "testEncodedClidsFromResponse";
    private static final String LAST_CLIENT_CLIDS_FOR_STARTUP_REQUEST = "testLastClientClidsForStartupRequest";
    private static final String LAST_CHOSEN_CLIDS_FOR_REQUEST = "testLastChosenClidsForRequest";
    private static final boolean STARTUP_DID_NOT_OVERRIDE_CLIDS = false;
    private static final long FIRST_STARTUP_SERVER_TIME = 1312131;
    private static final String COUNTRY_INIT = "by";
    private static final int MAX_RETRY_INTERVAL_COUNT = 1000;
    private static final int EXPONENTIAL_MULTIPLIER = 2;
    private final Map<String, List<String>> customSdkHostsModel = new HashMap<>();
    private final StartupStateProtobuf.StartupState.CustomSdkHostsPair[] customSdkHostsProto =
        new StartupStateProtobuf.StartupState.CustomSdkHostsPair[2];
    private Map<String, Object> modulesModel = new HashMap<>();
    private StartupStateProtobuf.StartupState.ModulesRemoteConfigsEntry[] modulesProto =
        new StartupStateProtobuf.StartupState.ModulesRemoteConfigsEntry[2];

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        customSdkHostsModel.put("am", Arrays.asList("host1", "host2"));
        customSdkHostsModel.put("ads", Arrays.asList("ads.host1"));
        customSdkHostsProto[0] = mock(StartupStateProtobuf.StartupState.CustomSdkHostsPair.class);
        customSdkHostsProto[1] = mock(StartupStateProtobuf.StartupState.CustomSdkHostsPair.class);
        mConverter = new StartupStateConverter();
        MockitoAnnotations.openMocks(this);

        when(autoInappCollectingConfigConverter.toModel(autoInappCollectingNanoConfig)).thenReturn(autoInappCollectingConfig);
        when(autoInappCollectingConfigConverter.fromModel(autoInappCollectingConfig)).thenReturn(autoInappCollectingNanoConfig);
        when(customSdkHostsConverter.toModel(customSdkHostsProto)).thenReturn(customSdkHostsModel);
        when(customSdkHostsConverter.fromModel(customSdkHostsModel)).thenReturn(customSdkHostsProto);

        when(mCacheControlConverter.toModel(mNanoCacheControl)).thenReturn(mCacheControl);
        when(mCacheControlConverter.fromModel(mCacheControl)).thenReturn(mNanoCacheControl);

        when(attributionConfigConverter.toModel(attributionNanoConfig)).thenReturn(attributionConfig);
        when(attributionConfigConverter.fromModel(attributionConfig)).thenReturn(attributionNanoConfig);
        when(startupUpdateConfigConverter.toModel(startupUpdateNanoConfig)).thenReturn(startupUpdateConfig);
        when(startupUpdateConfigConverter.fromModel(startupUpdateConfig)).thenReturn(startupUpdateNanoConfig);
        when(modulesRemoteConfigsConverter.fromModel(modulesModel)).thenReturn(modulesProto);
        when(modulesRemoteConfigsConverter.toModel(modulesProto)).thenReturn(modulesModel);
    }

    @Test
    public void testToModel() throws IllegalAccessException {
        StartupStateProtobuf.StartupState.StatSending statSending = new StartupStateProtobuf.StartupState.StatSending();
        StartupStateProtobuf.StartupState.Flags flags = new StartupStateProtobuf.StartupState.Flags();
        StartupStateProtobuf.StartupState.PermissionsCollectingConfig permissionsCollectingConfig = new StartupStateProtobuf.StartupState.PermissionsCollectingConfig();

        StartupStateProtobuf.StartupState stateProto = new StartupStateProtobuf.StartupState();

        stateProto.uuid = UUID;
        stateProto.obtainTime = OBTAIN_TIME;
        stateProto.hadFirstStartup = HAD_FIRST_STARTUP;
        stateProto.reportUrls = REPORT_URLS;
        stateProto.getAdUrl = GET_AD_URL;
        stateProto.certificateUrl = CERTIFICATE_URL;
        stateProto.reportAdUrl = REPORT_AD_URL;
        stateProto.hostUrlsFromStartup = HOST_URLS_FROM_STARTUP;
        stateProto.hostUrlsFromClient = HOST_URLS_FROM_CLIENT;
        stateProto.diagnosticUrls = DIAGNOSTIC_URLS;
        stateProto.customSdkHosts = customSdkHostsProto;

        stateProto.flags = flags;

        stateProto.encodedClidsFromResponse = ENCODED_CLIDS_FROM_RESPONSE;
        stateProto.lastClientClidsForStartupRequest = LAST_CLIENT_CLIDS_FOR_STARTUP_REQUEST;
        stateProto.lastChosenForRequestClids = LAST_CHOSEN_CLIDS_FOR_REQUEST;
        stateProto.startupDidNotOverrideClids = STARTUP_DID_NOT_OVERRIDE_CLIDS;
        stateProto.permissionsCollectingConfig = permissionsCollectingConfig;
        stateProto.statSending = statSending;
        stateProto.obtainServerTime = OBTAIN_SERVER_TIME;
        stateProto.countryInit = COUNTRY_INIT;
        stateProto.firstStartupServerTime = FIRST_STARTUP_SERVER_TIME;
        stateProto.outdated = OUTDATED;

        stateProto.maxRetryIntervalSeconds = MAX_RETRY_INTERVAL_COUNT;
        stateProto.retryExponentialMultiplier = EXPONENTIAL_MULTIPLIER;

        stateProto.cacheControl = mNanoCacheControl;

        stateProto.autoInappCollectingConfig = autoInappCollectingNanoConfig;
        stateProto.attribution = attributionNanoConfig;
        stateProto.startupUpdateConfig = startupUpdateNanoConfig;
        stateProto.modulesRemoteConfigs = modulesProto;

        ObjectPropertyAssertions<StartupStateModel> assertions
            = ObjectPropertyAssertions(mConverter.toModel(stateProto));

        assertions.withIgnoredFields(
            "locationCollectionConfigs",
            "socketConfig",
            "collectingFlags",
            "permissionsCollectingConfig",
            "statSending",
            "requests",
            "bleCollectingConfig"
        );

        assertions.checkField("uuid", UUID);
        assertions.checkField("obtainTime", OBTAIN_TIME);
        assertions.checkField("hadFirstStartup", HAD_FIRST_STARTUP);
        assertions.checkField("reportUrls", Arrays.asList(REPORT_URLS));
        assertions.checkField("getAdUrl", GET_AD_URL);
        assertions.checkField("certificateUrl", CERTIFICATE_URL);
        assertions.checkField("reportAdUrl", REPORT_AD_URL);
        assertions.checkField("hostUrlsFromStartup", Arrays.asList(HOST_URLS_FROM_STARTUP));
        assertions.checkField("hostUrlsFromClient", Arrays.asList(HOST_URLS_FROM_CLIENT));
        assertions.checkField("diagnosticUrls", Arrays.asList(DIAGNOSTIC_URLS));
        assertions.checkField("encodedClidsFromResponse", ENCODED_CLIDS_FROM_RESPONSE);
        assertions.checkField("lastClientClidsForStartupRequest", LAST_CLIENT_CLIDS_FOR_STARTUP_REQUEST);
        assertions.checkField("lastChosenForRequestClids", LAST_CHOSEN_CLIDS_FOR_REQUEST);
        assertions.checkField("startupDidNotOverrideClids", STARTUP_DID_NOT_OVERRIDE_CLIDS);
        assertions.checkField("obtainServerTime", OBTAIN_SERVER_TIME);
        assertions.checkField("countryInit", COUNTRY_INIT);
        assertions.checkField("firstStartupServerTime", FIRST_STARTUP_SERVER_TIME);
        assertions.checkField("retryPolicyConfig", new RetryPolicyConfig(MAX_RETRY_INTERVAL_COUNT, EXPONENTIAL_MULTIPLIER));
        assertions.checkField("outdated", OUTDATED);
        assertions.checkField("cacheControl", mCacheControl);
        assertions.checkField("autoInappCollectingConfig", autoInappCollectingConfig);
        assertions.checkField("attributionConfig", attributionConfig);
        assertions.checkField("customSdkHosts", customSdkHostsModel);
        assertions.checkField("startupUpdateConfig", startupUpdateConfig);
        assertions.checkField("modulesRemoteConfigs", modulesModel);

        assertions.checkAll();

        verify(mFlagsConverter).toModel(flags);
        verify(mStatSendingConverter).toModel(statSending);
        verify(mPermissionsCollectingConfigConverter).toModel(permissionsCollectingConfig);
        verify(customSdkHostsConverter).toModel(customSdkHostsProto);
    }

    @Test
    public void testToProto() throws IllegalAccessException {
        StatSending statSending = mock(StatSending.class);

        PermissionsCollectingConfig permissionsCollectingConfig = new PermissionsCollectingConfig(55, 66);

        CollectingFlags flagsModel = new CollectingFlags.CollectingFlagsBuilder().build();

        StartupStateModel stateModel = new StartupStateModel.StartupStateBuilder(flagsModel)
            .withUuid(StartupStateConverterTest.UUID)
            .withObtainTime(StartupStateConverterTest.OBTAIN_TIME)
            .withHadFirstStartup(true)
            .withReportUrls(Arrays.asList(StartupStateConverterTest.REPORT_URLS))
            .withGetAdUrl(StartupStateConverterTest.GET_AD_URL)
            .withReportAdUrl(StartupStateConverterTest.REPORT_AD_URL)
            .withCertificateUrl(StartupStateConverterTest.CERTIFICATE_URL)
            .withDiagnosticUrls(Arrays.asList(StartupStateConverterTest.DIAGNOSTIC_URLS))
            .withHostUrlsFromStartup(Arrays.asList(StartupStateConverterTest.HOST_URLS_FROM_STARTUP))
            .withHostUrlsFromClient(Arrays.asList(StartupStateConverterTest.HOST_URLS_FROM_CLIENT))
            .withEncodedClidsFromResponse(StartupStateConverterTest.ENCODED_CLIDS_FROM_RESPONSE)
            .withLastClientClidsForStartupRequest(StartupStateConverterTest.LAST_CLIENT_CLIDS_FOR_STARTUP_REQUEST)
            .withLastChosenForRequestClids(LAST_CHOSEN_CLIDS_FOR_REQUEST)
            .withStartupDidNotOverrideClids(StartupStateConverterTest.STARTUP_DID_NOT_OVERRIDE_CLIDS)
            .withCountryInit(StartupStateConverterTest.COUNTRY_INIT)
            .withPermissionsCollectingConfig(permissionsCollectingConfig)
            .withStatSending(statSending)
            .withHadFirstStartup(StartupStateConverterTest.HAD_FIRST_STARTUP)
            .withObtainServerTime(OBTAIN_SERVER_TIME)
            .withFirstStartupServerTime(FIRST_STARTUP_SERVER_TIME)
            .withOutdated(OUTDATED)
            .withRetryPolicyConfig(new RetryPolicyConfig(MAX_RETRY_INTERVAL_COUNT, EXPONENTIAL_MULTIPLIER))
            .withCacheControl(mCacheControl)
            .withAutoInappCollectingConfig(autoInappCollectingConfig)
            .withAttributionConfig(attributionConfig)
            .withCustomSdkHosts(customSdkHostsModel)
            .withStartupUpdateConfig(startupUpdateConfig)
            .withModulesRemoteConfigs(modulesModel)
            .build();

        StartupStateProtobuf.StartupState proto = mConverter.fromModel(stateModel);
        ObjectPropertyAssertions<StartupStateProtobuf.StartupState> assertions
            = ObjectPropertyAssertions(proto);

        assertions.withFinalFieldOnly(false);
        assertions.withIgnoredFields(
            "locationCollectionConfigs",
            "socketConfig",
            "flags",
            "permissionsCollectingConfig",
            "statSending",
            "requests",
            "bleCollectingConfig"
        );

        assertions.checkField("uuid", UUID);
        assertions.checkField("obtainTime", OBTAIN_TIME);
        assertions.checkField("hadFirstStartup", HAD_FIRST_STARTUP);
        assertions.checkField("reportUrls", REPORT_URLS);
        assertions.checkField("getAdUrl", GET_AD_URL);
        assertions.checkField("certificateUrl", CERTIFICATE_URL);
        assertions.checkField("reportAdUrl", REPORT_AD_URL);
        assertions.checkField("hostUrlsFromStartup", HOST_URLS_FROM_STARTUP);
        assertions.checkField("hostUrlsFromClient", HOST_URLS_FROM_CLIENT);
        assertions.checkField("diagnosticUrls", DIAGNOSTIC_URLS);
        assertions.checkField("encodedClidsFromResponse", ENCODED_CLIDS_FROM_RESPONSE);
        assertions.checkField("lastClientClidsForStartupRequest", LAST_CLIENT_CLIDS_FOR_STARTUP_REQUEST);
        assertions.checkField("lastChosenForRequestClids", LAST_CHOSEN_CLIDS_FOR_REQUEST);
        assertions.checkField("startupDidNotOverrideClids", STARTUP_DID_NOT_OVERRIDE_CLIDS);
        assertions.checkField("obtainServerTime", OBTAIN_SERVER_TIME);
        assertions.checkField("countryInit", COUNTRY_INIT);
        assertions.checkField("firstStartupServerTime", FIRST_STARTUP_SERVER_TIME);
        assertions.checkField("maxRetryIntervalSeconds", MAX_RETRY_INTERVAL_COUNT);
        assertions.checkField("retryExponentialMultiplier", EXPONENTIAL_MULTIPLIER);
        assertions.checkField("outdated", OUTDATED);
        assertions.checkField("cacheControl", mNanoCacheControl);
        assertions.checkField("autoInappCollectingConfig", autoInappCollectingNanoConfig);
        assertions.checkField("attribution", attributionNanoConfig);
        assertions.checkField("customSdkHosts", customSdkHostsProto);
        assertions.checkField("startupUpdateConfig", startupUpdateNanoConfig);
        assertions.checkField("modulesRemoteConfigs", modulesProto);

        assertions.checkAll();

        verify(mFlagsConverter).fromModel(flagsModel);
        verify(mStatSendingConverter).fromModel(statSending);
        verify(mPermissionsCollectingConfigConverter).fromModel(permissionsCollectingConfig);
        verify(customSdkHostsConverter).fromModel(customSdkHostsModel);
    }

    @Test
    public void testToProtoWithNullable() {
        when(startupUpdateConfigConverter.fromModel(any(StartupUpdateConfig.class))).thenReturn(startupUpdateNanoConfig);
        StartupStateModel stateModel = new StartupStateModel.StartupStateBuilder(new CollectingFlags.CollectingFlagsBuilder().build())
            .withCertificateUrl(null)
            .withReportUrls(null)
            .withHostUrlsFromStartup(null)
            .withHostUrlsFromClient(null)
            .withStatSending(null)
            .withPermissionsCollectingConfig(null)
            .withRetryPolicyConfig(null)
            .withCacheControl(null)
            .withAutoInappCollectingConfig(null)
            .withAttributionConfig(null)
            .withCustomSdkHosts(null)
            .build();

        StartupStateProtobuf.StartupState stateProto = mConverter.fromModel(stateModel);
        assertThat(stateProto).hasNoNullFieldsOrPropertiesExcept(
            "foregroundLocationCollectionConfig",
            "socketConfig",
            "flags",
            "permissionsCollectingConfig",
            "statSending",
            "bleCollectingConfig",
            "throttlingConfig",
            "cacheControl",
            "autoInappCollectingConfig",
            "attribution"
        );
    }

    @Test
    public void testToModelWithNullable() throws Exception {
        StartupStateProtobuf.StartupState stateProto = new StartupStateProtobuf.StartupState();
        stateProto.statSending = null;
        stateProto.permissionsCollectingConfig = null;
        stateProto.cacheControl = null;
        stateProto.autoInappCollectingConfig = null;
        stateProto.attribution = null;
        stateProto.startupUpdateConfig = null;
        stateProto.modulesRemoteConfigs = null;

        StartupStateModel stateModel = mConverter.toModel(stateProto);
        assertThat(stateModel).extracting(
            "statSending",
            "permissionsCollectingConfig",
            "cacheControl",
            "autoInappCollectingConfig",
            "attributionConfig"
        ).containsOnlyNulls();
        assertThat(stateModel.customSdkHosts).isEmpty();
        ObjectPropertyAssertions(stateModel.startupUpdateConfig)
            .checkField("intervalSeconds", "getIntervalSeconds", 86400)
            .checkAll();
    }

}
