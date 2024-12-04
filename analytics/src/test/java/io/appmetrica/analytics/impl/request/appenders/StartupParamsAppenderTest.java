package io.appmetrica.analytics.impl.request.appenders;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfo;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.DistributionSource;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.clids.ClidsInfo;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetter;
import io.appmetrica.analytics.impl.modules.ModulesRemoteConfigArgumentsCollector;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.request.Obfuscator;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.networktasks.internal.CommonUrlParts;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class StartupParamsAppenderTest extends CommonTest {

    private static final String FAKE_ID = "ru.yandex.test.metrica_clids";

    private static final String COUNTRY_INIT = "country_init";
    private static final String DETECT_LOCALE = "detect_locale";

    @Mock
    private StartupRequestConfig startupRequestConfig;
    @Mock
    private Obfuscator obfuscator;
    @Mock
    private ReferrerHolder referrerHolder;
    @Mock
    private AdvertisingIdGetter advertisingIdGetter;
    @Mock
    private AdvertisingIdsHolder advertisingIdsHolder;
    @Mock
    private AdTrackingInfoResult google;
    @Mock
    private AdTrackingInfoResult huawei;
    @Mock
    private AdTrackingInfoResult yandex;
    @Mock
    private ModulesRemoteConfigArgumentsCollector modulesArgumentsCollector;
    private final ClidsInfo.Candidate noClidsInfo = new ClidsInfo.Candidate(null, DistributionSource.APP);
    private final ClidsInfo.Candidate filledClidsInfo = new ClidsInfo.Candidate(Collections.singletonMap("clid0", "0"), DistributionSource.APP);
    private Uri.Builder mBuilder = new Uri.Builder();

    private Context context;
    private StartupParamsAppender startupParamsAppender;

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();
    private GlobalServiceLocator globalServiceLocator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        globalServiceLocator = GlobalServiceLocator.getInstance();
        context = globalServiceLocator.getContext();
        when(globalServiceLocator.getAdvertisingIdGetter()).thenReturn(advertisingIdGetter);
        when(advertisingIdGetter.getIdentifiers(context)).thenReturn(advertisingIdsHolder);
        when(advertisingIdsHolder.getGoogle()).thenReturn(google);
        when(advertisingIdsHolder.getHuawei()).thenReturn(huawei);
        when(advertisingIdsHolder.getYandex()).thenReturn(yandex);
        when(startupRequestConfig.getChosenClids()).thenReturn(noClidsInfo);
        when(startupRequestConfig.getReferrerHolder()).thenReturn(referrerHolder);
        when(GlobalServiceLocator.getInstance().getAdvertisingIdGetter().getIdentifiers())
            .thenReturn(advertisingIdsHolder);
        startupParamsAppender = new StartupParamsAppender(obfuscator, modulesArgumentsCollector);
    }

    @Test
    public void packageName() {
        when(startupRequestConfig.isIdentifiersValid()).thenReturn(false);
        doReturn(FAKE_ID).when(startupRequestConfig).getPackageName();
        when(obfuscator.obfuscate("app_id")).thenReturn("aid");
        startupParamsAppender.appendParams(mBuilder, startupRequestConfig);
        Uri request = mBuilder.build();
        assertThat(request.getQueryParameter("aid")).isEqualTo(FAKE_ID);
    }

    @Test
    public void testAppDebuggable() {
        doReturn("1").when(startupRequestConfig).isAppDebuggable();
        assertContainsParameter("app_debuggable", "1");
    }

    @Test
    public void testAppSystem() {
        doReturn("1").when(startupRequestConfig).isAppSystem();
        assertContainsParameter("app_system_flag", "1");
    }

    @Test
    public void testDeviceId() {
        doReturn("did").when(startupRequestConfig).getDeviceId();
        assertContainsParameter("deviceid", "did");
    }

    @Test
    public void testAppPlatform() {
        doReturn("platform").when(startupRequestConfig).getAppPlatform();
        assertContainsParameter("app_platform", "platform");
    }

    @Test
    public void testProtocolVersion() {
        doReturn("protocol").when(startupRequestConfig).getProtocolVersion();
        assertContainsParameter("protocol_version", "protocol");
    }

    @Test
    public void testAnalyticsSdkVersionName() {
        doReturn("3.5.0").when(startupRequestConfig).getAnalyticsSdkVersionName();
        assertContainsParameter("analytics_sdk_version_name", "3.5.0");
    }

    @Test
    public void testModel() {
        doReturn("mymodel").when(startupRequestConfig).getModel();
        assertContainsParameter("model", "mymodel");
    }

    @Test
    public void testManufacturer() {
        doReturn("mymanufacturer").when(startupRequestConfig).getManufacturer();
        assertContainsParameter("manufacturer", "mymanufacturer");
    }

    @Test
    public void testOsVersion() {
        doReturn("8.1").when(startupRequestConfig).getOsVersion();
        assertContainsParameter("os_version", "8.1");
    }

    @Test
    public void testScreenWidth() {
        doReturn(1500).when(startupRequestConfig).getScreenWidth();
        assertContainsParameter("screen_width", "1500");
    }

    @Test
    public void testScreenHeight() {
        doReturn(1200).when(startupRequestConfig).getScreenHeight();
        assertContainsParameter("screen_height", "1200");
    }

    @Test
    public void testScreenDpi() {
        doReturn(80).when(startupRequestConfig).getScreenDpi();
        assertContainsParameter("screen_dpi", "80");
    }

    @Test
    public void testScaleFactor() {
        doReturn(1.5f).when(startupRequestConfig).getScaleFactor();
        assertContainsParameter("scalefactor", "1.5");
    }

    @Test
    public void testLocale() {
        doReturn("mylocale").when(startupRequestConfig).getLocale();
        assertContainsParameter("locale", "mylocale");
    }

    @Test
    public void testDeviceType() {
        doReturn("phone").when(startupRequestConfig).getDeviceType();
        assertContainsParameter("device_type", "phone");
    }

    @Test
    public void testAppId() {
        doReturn("package_name").when(startupRequestConfig).getPackageName();
        assertContainsParameter("app_id", "package_name");
    }

    @Test
    public void testUuid() {
        doReturn("9876543210").when(startupRequestConfig).getUuid();
        assertContainsParameter("uuid", "9876543210");
    }

    @Test
    public void testNullUuid() {
        doReturn(null).when(startupRequestConfig).getUuid();
        assertDoesNotContainParameter("uuid");
    }

    @Test
    public void testEmptyUuid() {
        doReturn("").when(startupRequestConfig).getUuid();
        assertDoesNotContainParameter("uuid");
    }

    @Test
    public void testDetectLocale() {
        doReturn(false).when(startupRequestConfig).hasSuccessfulStartup();
        when(obfuscator.obfuscate(COUNTRY_INIT)).thenReturn("ci");
        assertContainsParameter(DETECT_LOCALE, "1");
        assertThat(mBuilder.toString()).doesNotContain("ci");
    }

    @Test
    public void testCountryInitLocale() {
        doReturn(true).when(startupRequestConfig).hasSuccessfulStartup();
        doReturn("us").when(startupRequestConfig).getCountryInit();
        when(obfuscator.obfuscate(DETECT_LOCALE)).thenReturn("dl");
        assertContainsParameter(COUNTRY_INIT, "us");
        assertThat(mBuilder.toString()).doesNotContain("dl");
    }

    @Test
    public void testAppIsNotDebuggable() {
        doReturn("0").when(startupRequestConfig).isAppDebuggable();
        assertContainsParameter("app_debuggable", "0");
    }

    @Test
    public void testAppIsNotSystem() {
        doReturn("0").when(startupRequestConfig).isAppSystem();
        assertContainsParameter("app_system_flag", "0");
    }

    @Test
    public void testReferrerAndSourceHasClidsAndReferrer() {
        String referrer = "some_referrer";
        String source = "broadcast";
        doReturn(filledClidsInfo).when(startupRequestConfig).getChosenClids();
        doReturn(referrer).when(startupRequestConfig).getDistributionReferrer();
        doReturn(source).when(startupRequestConfig).getInstallReferrerSource();
        when(obfuscator.obfuscate("install_referrer")).thenReturn("obfuscated_referrer");
        when(obfuscator.obfuscate("install_referrer_source")).thenReturn("obfuscated_referrer_source");
        startupParamsAppender.appendParams(mBuilder, startupRequestConfig);
        assertThat(mBuilder.toString()).contains("obfuscated_referrer=some_referrer").contains("obfuscated_referrer_source=broadcast");
    }

    @Test
    public void testReferrerAndSourceHasClidsAndReferrerUnknownSource() {
        String referrer = "some_referrer";
        doReturn(filledClidsInfo).when(startupRequestConfig).getChosenClids();
        doReturn(referrer).when(startupRequestConfig).getDistributionReferrer();
        doReturn(null).when(startupRequestConfig).getInstallReferrerSource();
        when(obfuscator.obfuscate("install_referrer")).thenReturn("obfuscated_referrer");
        when(obfuscator.obfuscate("install_referrer_source")).thenReturn("obfuscated_referrer_source");
        startupParamsAppender.appendParams(mBuilder, startupRequestConfig);
        assertThat(mBuilder.toString()).contains("obfuscated_referrer=some_referrer").contains("obfuscated_referrer_source=null");
    }

    @Test
    public void testReferrerAndSourceHasClidsNoReferrerInConfig() {
        String referrer = "some_referrer";
        doReturn(filledClidsInfo).when(startupRequestConfig).getChosenClids();
        doReturn(null).when(startupRequestConfig).getDistributionReferrer();
        doReturn(null).when(startupRequestConfig).getInstallReferrerSource();
        when(referrerHolder.getReferrerInfo()).thenReturn(new ReferrerInfo(referrer, 10, 20, ReferrerInfo.Source.GP));
        when(obfuscator.obfuscate("install_referrer")).thenReturn("obfuscated_referrer");
        when(obfuscator.obfuscate("install_referrer_source")).thenReturn("obfuscated_referrer_source");
        startupParamsAppender.appendParams(mBuilder, startupRequestConfig);
        assertThat(mBuilder.toString()).contains("obfuscated_referrer=some_referrer").contains("obfuscated_referrer_source=gpl");
    }

    @Test
    public void testReferrerAndSourceHasClidsNoReferrerAtAll() {
        String source = "broadcast";
        doReturn(filledClidsInfo).when(startupRequestConfig).getChosenClids();
        doReturn(null).when(startupRequestConfig).getDistributionReferrer();
        doReturn(source).when(startupRequestConfig).getInstallReferrerSource();
        when(obfuscator.obfuscate("install_referrer")).thenReturn("obfuscated_referrer");
        when(obfuscator.obfuscate("install_referrer_source")).thenReturn("obfuscated_referrer_source");
        startupParamsAppender.appendParams(mBuilder, startupRequestConfig);
        assertThat(mBuilder.toString()).doesNotContain("obfuscated_referrer").doesNotContain("obfuscated_referrer_source");
    }

    @Test
    public void testReferrerAndSourceNoClidsNoReferrerInconfig() {
        doReturn(null).when(startupRequestConfig).getDistributionReferrer();
        doReturn(null).when(startupRequestConfig).getInstallReferrerSource();
        when(referrerHolder.getReferrerInfo()).thenReturn(
                new ReferrerInfo("some_referrer", 10, 20, ReferrerInfo.Source.HMS)
        );
        when(obfuscator.obfuscate("install_referrer")).thenReturn("obfuscated_referrer");
        when(obfuscator.obfuscate("install_referrer_source")).thenReturn("obfuscated_referrer_source");
        startupParamsAppender.appendParams(mBuilder, startupRequestConfig);
        assertThat(mBuilder.toString()).doesNotContain("obfuscated_referrer").doesNotContain("obfuscated_referrer_source");
    }

    @Test
    public void testReferrerAndSourceNoClidsNoReferrerAtAll() {
        String source = "broadcast";
        doReturn(null).when(startupRequestConfig).getDistributionReferrer();
        doReturn(source).when(startupRequestConfig).getInstallReferrerSource();
        when(obfuscator.obfuscate("install_referrer")).thenReturn("obfuscated_referrer");
        when(obfuscator.obfuscate("install_referrer_source")).thenReturn("obfuscated_referrer_source");
        startupParamsAppender.appendParams(mBuilder, startupRequestConfig);
        assertThat(mBuilder.toString()).doesNotContain("obfuscated_referrer").doesNotContain("obfuscated_referrer_source");
    }

    @Test
    public void paramsShouldContainAppSetId() {
        final String appSetId = "333-444";
        when(startupRequestConfig.getAppSetId()).thenReturn(appSetId);
        assertContainsParameter("app_set_id", appSetId);
    }

    @Test
    public void paramsShouldContainEmptyAppSetId() {
        when(startupRequestConfig.getAppSetId()).thenReturn("");
        assertContainsParameter("app_set_id", "");
    }

    @Test
    public void paramsShouldContainAppSetIdScope() {
        final String scope = "some_scope";
        when(startupRequestConfig.getAppSetIdScope()).thenReturn(scope);
        assertContainsParameter("app_set_id_scope", scope);
    }

    private void assertContainsParameter(@NonNull String parameter, @NonNull String value) {
        final String obfuscatedParameter = "obfuscated_" + parameter;
        when(obfuscator.obfuscate(parameter)).thenReturn(obfuscatedParameter);
        startupParamsAppender.appendParams(mBuilder, startupRequestConfig);
        assertThat(mBuilder.toString()).contains(obfuscatedParameter + "=" + value);
    }

    private void assertDoesNotContainParameter(@NonNull String parameter) {
        final String obfuscatedParameter = "obfuscated_" + parameter;
        when(obfuscator.obfuscate(parameter)).thenReturn(obfuscatedParameter);
        startupParamsAppender.appendParams(mBuilder, startupRequestConfig);
        assertThat(mBuilder.toString()).doesNotContain(obfuscatedParameter);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class SimpleNumericParametersTest extends CommonTest {

        private static final String FIRST_MODULE_BLOCK_ID = "module_block_1";
        private static final String SECOND_MODULE_BLOCK_ID = "module_block_2";
        private static final int FIRST_MODULE_BLOCK_VALUE = 1;
        private static final int SECOND_MODULE_BLOCK_VALUE = 2;

        @Mock
        private StartupRequestConfig mStartupRequestConfig;
        @Mock
        private Obfuscator mObfuscator;
        @Mock
        private ModulesRemoteConfigArgumentsCollector modulesRemoteConfigArgumentsCollector;
        @Mock
        private AdvertisingIdsHolder advertisingIdsHolder;
        private Uri.Builder mBuilder = new Uri.Builder();
        private StartupParamsAppender mStartupParamsAppender;
        private final String mParameter;
        private final String mValue;

        @Rule
        public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

        @ParameterizedRobolectricTestRunner.Parameters(name = "Contains {0}={1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(
                new Object[] {"queries", 1},
                new Object[] {"query_hosts", 2},
                new Object[] {"time", 1},
                new Object[] {"stat_sending", 1},
                new Object[] {"retry_policy", 1},
                new Object[] {"cache_control", 1},
                new Object[] {"permissions_collecting", 1},
                new Object[] {"auto_inapp_collecting", 1},
                new Object[] {"attribution", 1},
                new Object[] {"startup_update", 1},
                new Object[] {"external_attribution", 1},
                new Object[] {FIRST_MODULE_BLOCK_ID, FIRST_MODULE_BLOCK_VALUE},
                new Object[] {SECOND_MODULE_BLOCK_ID, SECOND_MODULE_BLOCK_VALUE}
            );
        }

        public SimpleNumericParametersTest(String parameter, int value) {
            mParameter = parameter;
            mValue = String.valueOf(value);
        }

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            GlobalServiceLocator.init(RuntimeEnvironment.getApplication());
            when(mStartupRequestConfig.getChosenClids()).thenReturn(new ClidsInfo.Candidate(null, DistributionSource.APP));
            when(GlobalServiceLocator.getInstance().getAdvertisingIdGetter().getIdentifiers()).thenReturn(advertisingIdsHolder);
            when(advertisingIdsHolder.getGoogle())
                .thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
            when(advertisingIdsHolder.getHuawei())
                .thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
            when(advertisingIdsHolder.getYandex())
                .thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
            Map<String, Integer> modulesFeatures = new HashMap<>();
            modulesFeatures.put(FIRST_MODULE_BLOCK_ID, FIRST_MODULE_BLOCK_VALUE);
            modulesFeatures.put(SECOND_MODULE_BLOCK_ID, SECOND_MODULE_BLOCK_VALUE);
            when(modulesRemoteConfigArgumentsCollector.collectBlocks()).thenReturn(modulesFeatures);
            mStartupParamsAppender = new StartupParamsAppender(mObfuscator, modulesRemoteConfigArgumentsCollector);
        }

        @Test
        public void testContainsBlock() {
            final String obfuscatedParameter = "obfuscated_" + mParameter;
            when(mObfuscator.obfuscate(mParameter)).thenReturn(obfuscatedParameter);
            mStartupParamsAppender.appendParams(mBuilder, mStartupRequestConfig);
            String expectedParameter =
                    (mParameter.equals(FIRST_MODULE_BLOCK_ID) || mParameter.equals(SECOND_MODULE_BLOCK_ID)) ?
                            mParameter : obfuscatedParameter;
            assertThat(mBuilder.toString()).contains(expectedParameter + "=" + mValue);
        }
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class FeaturesTest extends CommonTest {

        private static final String FIRST_MODULE_FEATURE = "first_module_feature";
        private static final String SECOND_MODULE_FEATURE = "second_module_feature";

        @Mock
        private StartupRequestConfig mStartupRequestConfig;
        @Mock
        private Obfuscator mObfuscator;
        @Mock
        private ModulesRemoteConfigArgumentsCollector modulesArgumentsCollector;
        @Mock
        private AdvertisingIdsHolder advertisingIdsHolder;
        private Uri.Builder mBuilder = new Uri.Builder();
        private StartupParamsAppender mStartupParamsAppender;
        private final String mFeature;

        @Rule
        public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        public static Collection<Object[]> data() {
            return Arrays.asList(
                    new Object[]{"permissions_collecting"},
                    new Object[]{"features_collecting"},
                    new Object[]{"google_aid"},
                    new Object[]{"huawei_oaid"},
                    new Object[]{"sim_info"},
                    new Object[] {"ssl_pinning"},
                    new Object[]{FIRST_MODULE_FEATURE},
                    new Object[]{SECOND_MODULE_FEATURE}
                    );
        }

        public FeaturesTest(String feature) {
            mFeature = feature;
        }

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            when(mStartupRequestConfig.getChosenClids()).thenReturn(new ClidsInfo.Candidate(null, DistributionSource.APP));
            when(GlobalServiceLocator.getInstance().getAdvertisingIdGetter().getIdentifiers()).thenReturn(advertisingIdsHolder);
            when(advertisingIdsHolder.getGoogle())
                .thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
            when(advertisingIdsHolder.getHuawei())
                .thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
            when(advertisingIdsHolder.getYandex())
                .thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
            when(modulesArgumentsCollector.collectFeatures())
                    .thenReturn(Arrays.asList(FIRST_MODULE_FEATURE, SECOND_MODULE_FEATURE));
            mStartupParamsAppender = new StartupParamsAppender(mObfuscator, modulesArgumentsCollector);
        }

        @Test
        public void testContainsFeature() {
            assertContainsParameter(mFeature);
        }

        private void assertContainsParameter(@NonNull String parameter) {
            final String obfuscatedParameter = "obfuscated_" + parameter;
            when(mObfuscator.obfuscate(parameter)).thenReturn(obfuscatedParameter);
            mStartupParamsAppender.appendParams(mBuilder, mStartupRequestConfig);
            String expectedFeature =
                    (parameter.equals(FIRST_MODULE_FEATURE) || parameter.equals(SECOND_MODULE_FEATURE)) ?
                            parameter : obfuscatedParameter;
            assertThat(mBuilder.toString()).contains(expectedFeature);
        }

    }

    @RunWith(RobolectricTestRunner.class)
    public static class GAIDTest extends CommonTest {

        Uri.Builder builder = new Uri.Builder();
        @Mock
        DataSendingRestrictionControllerImpl restrictionController;
        @Mock
        StartupRequestConfig requestConfig;
        @Mock
        Obfuscator obfuscator;
        @Mock
        AdvertisingIdsHolder mAdvertisingIdsHolder;
        @Mock
        ModulesRemoteConfigArgumentsCollector modulesArgumentsCollector;

        @Rule
        public MockedConstructionRule<LiveConfigProvider> liveConfigProviderMockedConstructionRule =
        new MockedConstructionRule<>(LiveConfigProvider.class);

        private LiveConfigProvider liveConfigProvider;

        StartupParamsAppender startupParamsAppender;

        private final String gaidParameterName;
        private String obfuscatedGaidParametedName;
        private final String oaidParameterName;
        private String obfuscatedOaidParametedName;
        private final String yandexAdvIdParameterName;
        private String obfuscatedYandexAdvIdParametedName;

        AdTrackingInfo googleAdvInfo = new AdTrackingInfo(
                AdTrackingInfo.Provider.GOOGLE,
                "someGoogleAdvID",
                false
        );
        AdTrackingInfo huaweiAdvInfo = new AdTrackingInfo(
                AdTrackingInfo.Provider.HMS,
                "someHmsAdvID",
                false
        );
        AdTrackingInfo yandexAdvIdInfo = new AdTrackingInfo(
                AdTrackingInfo.Provider.YANDEX,
                "someYandexAdvID",
                false
        );

        public GAIDTest() {
            gaidParameterName = CommonUrlParts.ADV_ID;
            obfuscatedGaidParametedName = "obfuscated_" + gaidParameterName;

            oaidParameterName = CommonUrlParts.HUAWEI_OAID;
            obfuscatedOaidParametedName = "obfuscated_" + oaidParameterName;

            yandexAdvIdParameterName = CommonUrlParts.YANDEX_ADV_ID;
            obfuscatedYandexAdvIdParametedName = "obfuscated_" + yandexAdvIdParameterName;
        }

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            when(obfuscator.obfuscate(gaidParameterName)).thenReturn(obfuscatedGaidParametedName);
            when(obfuscator.obfuscate(oaidParameterName)).thenReturn(obfuscatedOaidParametedName);
            when(obfuscator.obfuscate(yandexAdvIdParameterName)).thenReturn(obfuscatedYandexAdvIdParametedName);
            when(mAdvertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
            when(mAdvertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
            when(mAdvertisingIdsHolder.getYandex()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
            when(requestConfig.getChosenClids()).thenReturn(new ClidsInfo.Candidate(null, DistributionSource.APP));

            startupParamsAppender = new StartupParamsAppender(obfuscator, modulesArgumentsCollector);

            liveConfigProvider = liveConfigProviderMockedConstructionRule.getConstructionMock().constructed().get(0);
            when(liveConfigProvider.getAdvertisingIdentifiers()).thenReturn(mAdvertisingIdsHolder);
        }

        @Test
        public void testGaid() {
            doReturn(false).when(restrictionController).isRestrictedForReporter();
            when(mAdvertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(
                    googleAdvInfo,
                    IdentifierStatus.OK,
                    null
            ));
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedGaidParametedName, googleAdvInfo.advId);
        }

        @Test
        public void testOaid() {
            doReturn(false).when(restrictionController).isRestrictedForReporter();
            when(mAdvertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult(
                    huaweiAdvInfo,
                    IdentifierStatus.OK,
                    null
            ));
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedOaidParametedName, huaweiAdvInfo.advId);
        }

        @Test
        public void yandexAdvId() {
            doReturn(false).when(restrictionController).isRestrictedForReporter();
            when(mAdvertisingIdsHolder.getYandex()).thenReturn(new AdTrackingInfoResult(
                    yandexAdvIdInfo,
                    IdentifierStatus.OK,
                    null
            ));
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedYandexAdvIdParametedName, yandexAdvIdInfo.advId);
        }

        @Test
        public void testNoGaid() {
            doReturn(false).when(restrictionController).isRestrictedForReporter();
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedGaidParametedName, "");
        }

        @Test
        public void noGaidIfMissingAdvIdsHolder() {
            when(liveConfigProvider.getAdvertisingIdentifiers()).thenReturn(null);
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedGaidParametedName, "");
        }

        @Test
        public void noHoaid() {
            doReturn(false).when(restrictionController).isRestrictedForReporter();
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedOaidParametedName, "");
        }

        @Test
        public void noHoaidIfMissingAdvIdsHolder() {
            when(liveConfigProvider.getAdvertisingIdentifiers()).thenReturn(null);
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedOaidParametedName, "");
        }

        @Test
        public void noYandexAdvId() {
            doReturn(false).when(restrictionController).isRestrictedForReporter();
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedYandexAdvIdParametedName, "");
        }

        @Test
        public void noYandexAdvIdIfMissingAdbIdsHolder() {
            when(liveConfigProvider.getAdvertisingIdentifiers()).thenReturn(null);
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedYandexAdvIdParametedName, "");
        }

        @Test
        public void dataSendingRestricted() {
            doReturn(true).when(restrictionController).isRestrictedForReporter();
            when(mAdvertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(
                    googleAdvInfo,
                    IdentifierStatus.OK,
                    null
            ));
            when(mAdvertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult(
                    huaweiAdvInfo,
                    IdentifierStatus.OK,
                    null
            ));
            when(mAdvertisingIdsHolder.getYandex()).thenReturn(new AdTrackingInfoResult(
                    yandexAdvIdInfo,
                    IdentifierStatus.OK,
                    null
            ));
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedGaidParametedName, "");
            checkQueryParameter(builder, obfuscatedOaidParametedName, "");
            checkQueryParameter(builder, obfuscatedYandexAdvIdParametedName, "");
        }

        @Test
        public void testEmptyGAID() {
            doReturn(false).when(restrictionController).isRestrictedForReporter();
            when(mAdvertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(
                    new AdTrackingInfo(AdTrackingInfo.Provider.GOOGLE, "", false),
                    IdentifierStatus.OK,
                    null
            ));
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedGaidParametedName, "");
        }

        @Test
        public void emptyOaid() {
            doReturn(false).when(restrictionController).isRestrictedForReporter();
            when(mAdvertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult(
                    new AdTrackingInfo(AdTrackingInfo.Provider.HMS, "", false),
                    IdentifierStatus.OK,
                    null
            ));
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedOaidParametedName, "");
        }

        @Test
        public void empyYandexAdvId() {
            doReturn(false).when(restrictionController).isRestrictedForReporter();
            when(mAdvertisingIdsHolder.getYandex()).thenReturn(new AdTrackingInfoResult(
                    new AdTrackingInfo(AdTrackingInfo.Provider.YANDEX, "", false),
                    IdentifierStatus.OK,
                    null
            ));
            startupParamsAppender.appendAdvIdIfAllowed(builder, restrictionController, liveConfigProvider);
            checkQueryParameter(builder, obfuscatedYandexAdvIdParametedName, "");
        }

        void checkQueryParameter(Uri.Builder builder, String name, String gaid) {
            Uri uri = builder.build();
            assertThat(uri.getQueryParameterNames()).contains(name);
            assertThat(uri.getQueryParameter(name)).isEqualTo(gaid);
        }

    }
}
