package io.appmetrica.analytics.impl.component;

import android.location.Location;
import io.appmetrica.analytics.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReporterArgumentsTest extends CommonTest {

    @Mock
    private CounterConfiguration mReporterConfiguration;
    private final String mDeviceType = "deviceType";
    private final String mAppVersion = "appVersion";
    private final String mAppBuildNumber = "appBuildNumber";
    private final String mApiKey = "apiKey";
    private final Boolean mLocationTracking = true;
    @Mock
    private Location mManualLocation;
    private final Boolean mFirstActivationAsUpdate = true;
    private final Integer mSessionTimeout = 15;
    private final Integer mMaxReportsCount = 20;
    private final Integer mDispatchPeriod = 17;
    private final Boolean mLogEnabled = true;
    private final Boolean mStatisticsSending = true;
    private final Map<String, String> mClidsFromClient = new HashMap<String, String>();
    private CommonArguments.ReporterArguments mArguments;
    private CommonArguments.ReporterArguments mOtherArguments;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mClidsFromClient.put("clid0", "0");
        when(mReporterConfiguration.getDeviceType()).thenReturn(mDeviceType);
        when(mReporterConfiguration.getAppVersion()).thenReturn(mAppVersion);
        when(mReporterConfiguration.getAppBuildNumber()).thenReturn(mAppBuildNumber);
        when(mReporterConfiguration.getApiKey()).thenReturn(mApiKey);
        when(mReporterConfiguration.getManualLocation()).thenReturn(mManualLocation);
        when(mReporterConfiguration.isLocationTrackingEnabled()).thenReturn(mLocationTracking);
        when(mReporterConfiguration.isFirstActivationAsUpdate()).thenReturn(mFirstActivationAsUpdate);
        when(mReporterConfiguration.getSessionTimeout()).thenReturn(mSessionTimeout);
        when(mReporterConfiguration.getMaxReportsCount()).thenReturn(mMaxReportsCount);
        when(mReporterConfiguration.getDispatchPeriod()).thenReturn(mDispatchPeriod);
        when(mReporterConfiguration.isLogEnabled()).thenReturn(mLogEnabled);
        when(mReporterConfiguration.getStatisticsSending()).thenReturn(mStatisticsSending);
    }

    @Test
    public void testMergeFromWasNull() {
        mArguments = new CommonArguments.ReporterArguments();
        mOtherArguments = new CommonArguments.ReporterArguments(mReporterConfiguration, mClidsFromClient);
        CommonArguments.ReporterArguments newArguments = mArguments.mergeFrom(mOtherArguments);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(newArguments.deviceType).isEqualTo(mDeviceType);
        softly.assertThat(newArguments.appVersion).isEqualTo(mAppVersion);
        softly.assertThat(newArguments.appBuildNumber).isEqualTo(mAppBuildNumber);
        softly.assertThat(newArguments.apiKey).isEqualTo(mApiKey);
        softly.assertThat(newArguments.manualLocation).isEqualTo(mManualLocation);
        softly.assertThat(newArguments.locationTracking).isEqualTo(mLocationTracking);
        softly.assertThat(newArguments.firstActivationAsUpdate).isEqualTo(mFirstActivationAsUpdate);
        softly.assertThat(newArguments.sessionTimeout).isEqualTo(mSessionTimeout);
        softly.assertThat(newArguments.maxReportsCount).isEqualTo(mMaxReportsCount);
        softly.assertThat(newArguments.dispatchPeriod).isEqualTo(mDispatchPeriod);
        softly.assertThat(newArguments.logEnabled).isEqualTo(mLogEnabled);
        softly.assertThat(newArguments.statisticsSending).isEqualTo(mStatisticsSending);
        softly.assertThat(newArguments.clidsFromClient).isEqualTo(mClidsFromClient);
        softly.assertAll();
    }

    @Test
    public void testMergeFromWasNotNull() {
        mArguments = new CommonArguments.ReporterArguments(mReporterConfiguration, mClidsFromClient);

        when(mReporterConfiguration.getDeviceType()).thenReturn("new device type");
        when(mReporterConfiguration.getAppVersion()).thenReturn("new app version");
        when(mReporterConfiguration.getAppBuildNumber()).thenReturn("new build number");
        when(mReporterConfiguration.getApiKey()).thenReturn("new api key");
        when(mReporterConfiguration.getManualLocation()).thenReturn(mock(Location.class));
        when(mReporterConfiguration.isLocationTrackingEnabled()).thenReturn(false);
        when(mReporterConfiguration.isFirstActivationAsUpdate()).thenReturn(false);
        when(mReporterConfiguration.getSessionTimeout()).thenReturn(30);
        when(mReporterConfiguration.getMaxReportsCount()).thenReturn(30);
        when(mReporterConfiguration.getDispatchPeriod()).thenReturn(30);
        when(mReporterConfiguration.isLogEnabled()).thenReturn(false);
        when(mReporterConfiguration.getStatisticsSending()).thenReturn(false);
        Map<String, String> clidsFromClient = new HashMap<String, String>();
        clidsFromClient.put("clid1", "1");

        mOtherArguments = new CommonArguments.ReporterArguments(mReporterConfiguration, clidsFromClient);
        CommonArguments.ReporterArguments newArguments = mArguments.mergeFrom(mOtherArguments);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(newArguments.deviceType).isEqualTo(mDeviceType);
        softly.assertThat(newArguments.appVersion).isEqualTo(mAppVersion);
        softly.assertThat(newArguments.appBuildNumber).isEqualTo(mAppBuildNumber);
        softly.assertThat(newArguments.apiKey).isEqualTo(mApiKey);
        softly.assertThat(newArguments.manualLocation).isEqualTo(mManualLocation);
        softly.assertThat(newArguments.firstActivationAsUpdate).isEqualTo(mFirstActivationAsUpdate);
        softly.assertThat(newArguments.sessionTimeout).isEqualTo(mSessionTimeout);
        softly.assertThat(newArguments.maxReportsCount).isEqualTo(mMaxReportsCount);
        softly.assertThat(newArguments.dispatchPeriod).isEqualTo(mDispatchPeriod);
        softly.assertThat(newArguments.logEnabled).isEqualTo(mLogEnabled);
        softly.assertThat(newArguments.statisticsSending).isEqualTo(mStatisticsSending);
        softly.assertThat(newArguments.clidsFromClient).isEqualTo(mClidsFromClient);
        softly.assertAll();
    }

    @Test
    public void testMergeFromWasNullAndNewNull() {
        mArguments = new CommonArguments.ReporterArguments();
        mOtherArguments = new CommonArguments.ReporterArguments();
        CommonArguments.ReporterArguments newArguments = mArguments.mergeFrom(mOtherArguments);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(newArguments.deviceType).isNull();
        softly.assertThat(newArguments.appVersion).isNull();
        softly.assertThat(newArguments.appBuildNumber).isNull();
        softly.assertThat(newArguments.apiKey).isNull();
        softly.assertThat(newArguments.manualLocation).isNull();
        softly.assertThat(newArguments.firstActivationAsUpdate).isNull();
        softly.assertThat(newArguments.sessionTimeout).isNull();
        softly.assertThat(newArguments.maxReportsCount).isNull();
        softly.assertThat(newArguments.dispatchPeriod).isNull();
        softly.assertThat(newArguments.logEnabled).isNull();
        softly.assertThat(newArguments.statisticsSending).isNull();
        softly.assertThat(newArguments.clidsFromClient).isNull();
        softly.assertAll();
    }

    @Test
    public void testCompareWithOtherArgumentsBothEmpty() {
        // see HashCodeEqualsTest
        mArguments = new CommonArguments.ReporterArguments();
        mOtherArguments = new CommonArguments.ReporterArguments();
        assertThat(mArguments.compareWithOtherArguments(mOtherArguments)).isTrue();
    }

    @Test
    public void testCompareWithOtherArgumentsIdentical() {
        // see HashCodeEqualsTest
        mArguments = new CommonArguments.ReporterArguments(mReporterConfiguration, mClidsFromClient);
        mOtherArguments = new CommonArguments.ReporterArguments(mReporterConfiguration, mClidsFromClient);
        assertThat(mArguments.compareWithOtherArguments(mOtherArguments)).isTrue();
    }

    @Test
    public void testCompareWithOtherArgumentsOneFieldDifferent() {
        // see HashCodeEqualsTest
        Map<String, String> clidsFromClient = new HashMap<String, String>();
        clidsFromClient.put("clid1", "1");
        mArguments = new CommonArguments.ReporterArguments(mReporterConfiguration, mClidsFromClient);
        mOtherArguments = new CommonArguments.ReporterArguments(mReporterConfiguration, clidsFromClient);
        assertThat(mArguments.compareWithOtherArguments(mOtherArguments)).isFalse();
    }
}
