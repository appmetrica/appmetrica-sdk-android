package io.appmetrica.analytics.impl.request;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ReportArgumentsTest extends CommonTest {

    private static final int SESSION_TIMEOUT = 100;
    private static final int MAX_REPORTS_COUNT = 200;
    private static final int DISPATCH_PERIOD_SECONDS = 300;
    private static final int APP_BUILD_NUMBER = 178;
    private static final String API_KEY = UUID.randomUUID().toString();
    private static final boolean REPORT_LOCATION_ENABLED = true;
    private static final boolean FIRST_ACTIVATION_AS_UPDATE = true;
    private static final boolean LOG_ENABLED = true;
    private static final boolean STATISTICS_SENDING = false;
    private static final String CUSTOM_VERSION = "customVersion";
    private static final String DEVICE_TYPE = "phone";

    public static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 10;
    public static final boolean DEFAULT_REPORT_LOCATION_ENABLED = BuildConfig.DEFAULT_LOCATION_COLLECTING;
    public static final boolean DEFAULT_FIRST_ACTIVATION_AS_UPDATE = false;
    public static final boolean DEFAULT_LOG_ENABLED = false;
    public static final boolean DEFAULT_STATISTICS_SENDING = true;
    public static final int DEFAULT_MAX_REPORTS_IN_DB_COUNT = 1000;

    public static final int DEFAULT_DISPATCH_PERIOD_SECONDS = 90;
    public static final int DEFAULT_MAX_REPORTS_COUNT = 7;
    private final Map<String, String> clids = new HashMap<String, String>();
    private static final int MAX_REPORTS_IN_DB_COUNT = 250;
    private static final boolean REVENUE_AUTO_TRACKING_ENABLED = true;

    @Before
    public void setUp() {
        clids.put("clid0", "0");
        clids.put("clid1", "1");
    }

    @Test
    public void testEqualToOtherArguments() {
        assertThat(
                ReportRequestConfig.Arguments.empty().mergeFrom(fillConfiguration())
        ).isEqualToComparingFieldByField(fillConfiguration());
    }

    @Test
    public void testNotEqualToOtherArguments() {
        assertThat(
                ReportRequestConfig.Arguments.empty().compareWithOtherArguments(fillConfiguration())
        ).isFalse();
    }

    @Test
    public void testFromConfigurationOnly() {
        assertArguments(
                ReportRequestConfig.Arguments.empty().mergeFrom(fillConfiguration()),
                DEVICE_TYPE,
                CUSTOM_VERSION,
                String.valueOf(APP_BUILD_NUMBER),
                API_KEY,
                REPORT_LOCATION_ENABLED,
                new Location("provider"),
                FIRST_ACTIVATION_AS_UPDATE,
                SESSION_TIMEOUT,
                MAX_REPORTS_COUNT,
                DISPATCH_PERIOD_SECONDS,
                LOG_ENABLED,
                STATISTICS_SENDING,
                clids,
                MAX_REPORTS_IN_DB_COUNT,
                REVENUE_AUTO_TRACKING_ENABLED
        );
    }

    @Test
    public void testChooseFromConfiguration() {
        assertArguments(
                ReportRequestConfig.Arguments.empty().mergeFrom(fillConfiguration()),
                DEVICE_TYPE,
                CUSTOM_VERSION,
                String.valueOf(APP_BUILD_NUMBER),
                API_KEY,
                REPORT_LOCATION_ENABLED,
                new Location("provider"),
                FIRST_ACTIVATION_AS_UPDATE,
                SESSION_TIMEOUT,
                MAX_REPORTS_COUNT,
                DISPATCH_PERIOD_SECONDS,
                LOG_ENABLED,
                STATISTICS_SENDING,
                clids,
                MAX_REPORTS_IN_DB_COUNT,
                REVENUE_AUTO_TRACKING_ENABLED
        );
    }

    @Test
    public void chooseFromOldAttributes() {
        ReportRequestConfig.Arguments oldArguments = new ReportRequestConfig.Arguments(
                "phone",
                "oldCustomVersion",
                "old178",
                "oldApiKey",
                false,
                new Location("oldProvider"),
                false,
                100,
                200,
                300,
                false,
                true,
                clids,
                250

        );
        assertThat(oldArguments.compareWithOtherArguments(CommonArgumentsTestUtils.emptyReporterArguments())).isTrue();
    }

    @Test
    public void testAllParametersAreEmpty() {
        assertArguments(
                ReportRequestConfig.Arguments.empty(),
                null,
                null,
                null,
                null,
                DEFAULT_REPORT_LOCATION_ENABLED,
                null,
                DEFAULT_FIRST_ACTIVATION_AS_UPDATE,
                DEFAULT_SESSION_TIMEOUT_SECONDS,
                DEFAULT_MAX_REPORTS_COUNT,
                DEFAULT_DISPATCH_PERIOD_SECONDS,
                DEFAULT_LOG_ENABLED,
                DEFAULT_STATISTICS_SENDING,
                null,
                DEFAULT_MAX_REPORTS_IN_DB_COUNT,
                REVENUE_AUTO_TRACKING_ENABLED
        );
    }

    private void assertArguments(@NonNull ReportRequestConfig.Arguments arguments,
                                 @Nullable String deviceType,
                                 @Nullable String appVersion,
                                 @Nullable String appBuildNumber,
                                 @Nullable String apiKey,
                                 @Nullable Boolean reportLocationEnabled,
                                 @Nullable Location manualLocation,
                                 @Nullable Boolean firstActivationAsUpdate,
                                 @Nullable Integer sessionTimeout,
                                 @Nullable Integer maxReportsCount,
                                 @Nullable Integer dispatchPeriod,
                                 @Nullable Boolean logEnabled,
                                 @Nullable Boolean statisticsSending,
                                 @Nullable Map<String, String> clids,
                                 @Nullable Integer maxReportsInDbCount,
                                 @Nullable Boolean revenueAutoTrackingEnabled) {
        SoftAssertions softAssertion = new SoftAssertions();
        softAssertion.assertThat(arguments.deviceType).as("deviceType").isEqualTo(deviceType);
        softAssertion.assertThat(arguments.appVersion).as("appVersion").isEqualTo(appVersion);
        softAssertion.assertThat(arguments.appBuildNumber).as("appBuildNumber").isEqualTo(appBuildNumber);
        softAssertion.assertThat(arguments.apiKey).as("apiKey").isEqualTo(apiKey);
        softAssertion.assertThat(arguments.locationTracking).as("reportLocationEnabled").isEqualTo(reportLocationEnabled);
        //assertThat(arguments.manualLocation).isEqualTo(manualLocation); todo (avitenko) remove strange location marshalling
        softAssertion.assertThat(arguments.firstActivationAsUpdate).as("firstActivationAsUpdate").isEqualTo(firstActivationAsUpdate);
        softAssertion.assertThat(arguments.sessionTimeout).as("sessionTimeout").isEqualTo(sessionTimeout);
        softAssertion.assertThat(arguments.maxReportsCount).as("maxReportsCount").isEqualTo(maxReportsCount);
        softAssertion.assertThat(arguments.dispatchPeriod).as("dispatchPeriod").isEqualTo(dispatchPeriod);
        softAssertion.assertThat(arguments.logEnabled).as("logEnabled").isEqualTo(logEnabled);
        softAssertion.assertThat(arguments.statisticsSending).as("statisticsSending").isEqualTo(statisticsSending);
        softAssertion.assertThat(arguments.clidsFromClient).as("clidsFromClient").isEqualTo(clids);
        softAssertion.assertThat(arguments.maxReportsInDbCount).as("maxReportsInDbCount").isEqualTo(maxReportsInDbCount);
        softAssertion.assertAll();
    }

    private CommonArguments.ReporterArguments fillConfiguration() {
        CounterConfiguration counterConfiguration = new CounterConfiguration();
        counterConfiguration.setDeviceType(DEVICE_TYPE);
        counterConfiguration.setCustomAppVersion(CUSTOM_VERSION);
        counterConfiguration.setAppBuildNumber(APP_BUILD_NUMBER);
        counterConfiguration.setApiKey(API_KEY);
        counterConfiguration.setLocationTracking(REPORT_LOCATION_ENABLED);
//        counterConfiguration.setManualLocation(new Location("provider"));
        counterConfiguration.setFirstActivationAsUpdate(FIRST_ACTIVATION_AS_UPDATE);
        counterConfiguration.setSessionTimeout(SESSION_TIMEOUT);
        counterConfiguration.setMaxReportsCount(MAX_REPORTS_COUNT);
        counterConfiguration.setDispatchPeriod(DISPATCH_PERIOD_SECONDS);
        counterConfiguration.setLogEnabled(LOG_ENABLED);
        counterConfiguration.setStatisticsSending(STATISTICS_SENDING);
        counterConfiguration.setMaxReportsInDbCount(MAX_REPORTS_IN_DB_COUNT);
        counterConfiguration.setRevenueAutoTrackingEnabled(REVENUE_AUTO_TRACKING_ENABLED);
        return new CommonArguments.ReporterArguments(counterConfiguration, clids);
    }

    @NonNull
    public static ReportRequestConfig.Arguments createEmptyArguments() {
        return new ReportRequestConfig.Arguments(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

}
