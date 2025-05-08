package io.appmetrica.analytics.impl.request.appenders;

import android.net.Uri;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdTrackingInfoResult;
import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.request.DbNetworkTaskConfig;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.networktasks.internal.AdvIdWithLimitedAppender;
import io.appmetrica.analytics.networktasks.internal.NetworkTaskForSendingDataParamsAppender;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Arrays;
import java.util.Collection;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReportParamsAppenderTest extends CommonTest {

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Mock
    private ReportRequestConfig mReportRequestConfig;
    private DbNetworkTaskConfig mDbNetworkTaskConfig;
    @Mock
    private JsonHelper.OptJSONObject mJson;
    @Mock
    private AdvertisingIdsHolder mAdvertisingIdsHolder;
    @Mock
    private AdvIdWithLimitedAppender mAdvIdWithLimitedAppender;
    @Mock
    private NetworkTaskForSendingDataParamsAppender sendingDataParamsAppender;
    @Mock
    private LiveConfigProvider liveConfigProvider;

    private ReportParamsAppender mParamsAppender;

    private final Uri.Builder mBuilder = new Uri.Builder();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        GlobalServiceLocator.init(RuntimeEnvironment.getApplication());
        when(mAdvertisingIdsHolder.getGoogle()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
        when(mAdvertisingIdsHolder.getHuawei()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
        when(mAdvertisingIdsHolder.getYandex()).thenReturn(new AdTrackingInfoResult(null, IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE, null));
        when(liveConfigProvider.getAdvertisingIdentifiers()).thenReturn(mAdvertisingIdsHolder);
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        when(mJson.getStringOrEmpty("kitBuildType")).thenReturn("internal_snapshot");
        mDbNetworkTaskConfig = new DbNetworkTaskConfig(mJson);
        mParamsAppender.setDbReportRequestConfig(mDbNetworkTaskConfig);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class DatabaseParamsTest {

        @Rule
        public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

        @Mock
        private ReportRequestConfig mReportRequestConfig;
        @Mock
        private JsonHelper.OptJSONObject mJson;
        private DbNetworkTaskConfig mDbNetworkTaskConfig;
        private final Uri.Builder mBuilder = new Uri.Builder();
        private ReportParamsAppender mParamsAppender;
        private final String mJsonKey;
        private final String mRequestKey;
        private final String mValue;

        @ParameterizedRobolectricTestRunner.Parameters(name = "Contains {1}={2}")
        public static Collection<Object[]> data() {
            return Arrays.asList(
                new Object[]{"dId", "deviceid", "54323456"},
                new Object[]{"uId", "uuid", "87445579"},
                new Object[]{"analyticsSdkVersionName", "analytics_sdk_version_name", "v3.5.0"},
                new Object[]{"appVer", "app_version_name", "myapp"},
                new Object[]{"kitBuildNumber", "analytics_sdk_build_number", "1432"},
                new Object[]{"kitBuildType", "analytics_sdk_build_type", "snapshot"},
                new Object[]{"lang", "locale", "mylocale"},
                new Object[]{"root", "is_rooted", "1"});
        }

        public DatabaseParamsTest(String jsonKey, String requestKey, String value) {
            mJsonKey = jsonKey;
            mRequestKey = requestKey;
            mValue = value;
        }

        @Before
        public void setUp() {
            MockitoAnnotations.openMocks(this);
            GlobalServiceLocator.init(RuntimeEnvironment.getApplication());
            mParamsAppender = new ReportParamsAppender(
                mock(AdvIdWithLimitedAppender.class),
                mock(NetworkTaskForSendingDataParamsAppender.class),
                mock(LiveConfigProvider.class)
            );
            when(mJson.getStringOrEmpty("kitBuildType")).thenReturn("internal_snapshot");
            when(mJson.getStringOrEmpty(mJsonKey)).thenReturn(mValue);
            mDbNetworkTaskConfig = new DbNetworkTaskConfig(mJson);
            mParamsAppender.setDbReportRequestConfig(mDbNetworkTaskConfig);
        }

        @Test
        public void test() {
            mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
            assertThat(mBuilder.toString()).contains(mRequestKey + "=" + mValue);
        }
    }

    @Test
    public void testDeviceIdEmpty() {
        when(mReportRequestConfig.getDeviceId()).thenReturn("98766789");
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("deviceid=98766789");
    }

    @Test
    public void testUuidEmpty() {
        when(mReportRequestConfig.getUuid()).thenReturn("55556666");
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("uuid=55556666");
    }

    @Test
    public void testAnalyticsSdkVersionEmpty() {
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).doesNotContain("analytics_sdk_version");
    }

    @Test
    public void testAnalyticsSdkVersionNameEmpty() {
        when(mReportRequestConfig.getAnalyticsSdkVersionName()).thenReturn("3.5.0");
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).doesNotContain("analytics_sdk_version_name");
    }

    @Test
    public void testAppVersionEmpty() {
        when(mReportRequestConfig.getAppVersion()).thenReturn("my.package");
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("app_version_name=my.package");
    }

    @Test
    public void testAnalyticsSdkBuildNumberEmpty() {
        when(mReportRequestConfig.getAnalyticsSdkBuildNumber()).thenReturn("43212");
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).doesNotContain("analytics_sdk_build_number");
    }

    // region another dbConfig
    @Test
    public void testAnalyticsSdkBuildTypeEmpty() {
        when(mJson.getStringOrEmpty("kitBuildType")).thenReturn(null);
        mDbNetworkTaskConfig = new DbNetworkTaskConfig(mJson);
        mParamsAppender.setDbReportRequestConfig(mDbNetworkTaskConfig);
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        String path = mBuilder.toString();
        assertThat(path).doesNotContain("analytics_sdk_build_type");
    }
    // endregion another dbConfig

    @Test
    public void testAppDebuggableEmpty() {
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).doesNotContain("app_debuggable");
    }

    @Test
    public void testLocaleEmpty() {
        when(mReportRequestConfig.getLocale()).thenReturn("ru");
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("locale=ru");
    }

    @Test
    public void testRootStatusEmpty() {
        when(mReportRequestConfig.getDeviceRootStatus()).thenReturn("1");
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("is_rooted=1");
    }

    @Test
    public void testAppFrameworkEmpty() {
        when(mReportRequestConfig.getAppFramework()).thenReturn("myframework");
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("app_framework=myframework");
    }

    @Test
    public void testAttributionIdEmpty() {
        when(mReportRequestConfig.getAttributionId()).thenReturn(14);
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).doesNotContain("attribution_id");
    }

    @Test
    public void testDatabaseOsApiLevel() {
        when(mJson.optInt(eq("osApiLev"), anyInt())).thenReturn(28);
        mDbNetworkTaskConfig = new DbNetworkTaskConfig(mJson);
        mParamsAppender.setDbReportRequestConfig(mDbNetworkTaskConfig);
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("os_api_level=28");
    }

    @Test
    public void testDatabaseAppDebuggable() {
        when(mJson.optString(eq("app_debuggable"), anyString())).thenReturn("1");
        mDbNetworkTaskConfig = new DbNetworkTaskConfig(mJson);
        mParamsAppender.setDbReportRequestConfig(mDbNetworkTaskConfig);
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("app_debuggable=1");
    }

    @Test
    public void testDatabaseAppFramework() {
        when(mJson.optString(eq("app_framework"), anyString())).thenReturn("myframework");
        mDbNetworkTaskConfig = new DbNetworkTaskConfig(mJson);
        mParamsAppender.setDbReportRequestConfig(mDbNetworkTaskConfig);
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("app_framework=myframework");
    }

    @Test
    public void testDatabaseAttributionId() {
        when(mJson.optInt(eq("attribution_id"), anyInt())).thenReturn(15);
        mDbNetworkTaskConfig = new DbNetworkTaskConfig(mJson);
        mParamsAppender.setDbReportRequestConfig(mDbNetworkTaskConfig);
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("attribution_id=15");
    }
    //testing ParamsFromCurrentConfiguration

    @Test
    public void testApiKey() {
        when(mReportRequestConfig.getApiKey()).thenReturn("52345-hgjhgjh");
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("api_key_128=52345-hgjhgjh");
    }

    @Test
    public void testAppId() {
        when(mReportRequestConfig.getPackageName()).thenReturn("mypackage");
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("app_id=mypackage");
    }

    @Test
    public void testAppPlatform() {
        when(mReportRequestConfig.getAppPlatform()).thenReturn("android");
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("app_platform=android");
    }

    @Test
    public void testModel() {
        when(mReportRequestConfig.getModel()).thenReturn("mymodel");
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("model=mymodel");
    }

    @Test
    public void testManufacturer() {
        when(mReportRequestConfig.getManufacturer()).thenReturn("mymanufacturer");
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("manufacturer=mymanufacturer");
    }

    @Test
    public void testScreenWidth() {
        when(mReportRequestConfig.getScreenWidth()).thenReturn(1500);
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("screen_width=1500");
    }

    @Test
    public void testScreenHeight() {
        when(mReportRequestConfig.getScreenHeight()).thenReturn(1200);
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("screen_height=1200");
    }

    @Test
    public void testScreenDpi() {
        when(mReportRequestConfig.getScreenDpi()).thenReturn(80);
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("screen_dpi=80");
    }

    @Test
    public void testScaleFactor() {
        when(mReportRequestConfig.getScaleFactor()).thenReturn(1.5f);
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("scalefactor=1.5");
    }

    @Test
    public void testDeviceType() {
        when(mReportRequestConfig.getDeviceType()).thenReturn("phone");
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("device_type=phone");
    }

    @Test
    public void testRequestId() {
        mParamsAppender = new ReportParamsAppender(
            mAdvIdWithLimitedAppender,
            sendingDataParamsAppender,
            liveConfigProvider
        );
        mParamsAppender.setRequestId(3333);
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("request_id=3333");
    }

    @Test
    public void testAdvIdAppended() {
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        verify(mAdvIdWithLimitedAppender).appendParams(mBuilder, mAdvertisingIdsHolder);
    }

    @Test
    public void paramsShouldContainAppSetId() {
        final String appSetId = "333-444";
        when(mReportRequestConfig.getAppSetId()).thenReturn(appSetId);
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("app_set_id=" + appSetId);
    }

    @Test
    public void paramsShouldContainEmptyAppSetId() {
        when(mReportRequestConfig.getAppSetId()).thenReturn("");
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("app_set_id=&");
    }

    @Test
    public void paramsShouldContainAppSetIdScope() {
        final String scope = "some_scope";
        when(mReportRequestConfig.getAppSetIdScope()).thenReturn(scope);
        mParamsAppender.appendParams(mBuilder, mReportRequestConfig);
        assertThat(mBuilder.toString()).contains("app_set_id_scope=" + scope);
    }

}
