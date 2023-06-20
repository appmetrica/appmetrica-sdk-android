package io.appmetrica.analytics.impl.component.session;

import android.content.ContentValues;
import android.os.SystemClock;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.constants.Constants;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class BaseSessionTest extends CommonTest {

    SessionStorageImpl mSessionStorage;
    @Mock
    ComponentUnit mComponent;
    @Mock
    private ReportRequestConfig mConfig;
    @Mock
    private DatabaseHelper mDatabaseHelper;
    @Mock
    SessionIDProvider sessionIDProvider;
    @Mock
    IReporterExtended mSelftReporter;
    @Mock
    SystemTimeProvider timeProvider;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        PreferencesComponentDbStorage storage = mock(PreferencesComponentDbStorage.class);
        doReturn("").when(storage).getSessionParameters(anyString());
        mSessionStorage = spy(new SessionStorageImpl(storage, "a"));

        doReturn(mConfig).when(mComponent).getFreshReportRequestConfig();

        doReturn(mDatabaseHelper).when(mComponent).getDbHelper();
        doReturn(new ContentValues()).when(mDatabaseHelper).getSessionRequestParameters(anyLong(), any(SessionType.class));
    }

    protected abstract AbstractSessionFactory getSessionFactory();

    @Test
    public void testSessionValid() throws Exception {
        prepareRequestParamsTest();

        mConfig.setSessionTimeout(getSessionTimeout());
        mSessionStorage.putSleepStart((long) 1 - getSessionTimeout());
        mSessionStorage.putSessionId(1L);
        Session session = getSessionFactory().load();

        assertThat(session.isValid(SystemClock.elapsedRealtime())).isTrue();
    }

    protected abstract int getSessionTimeout();

    @Test
    public void testSessionInValidIfExpired() throws Exception {
        mConfig.setSessionTimeout(getSessionTimeout());

        mSessionStorage.putSleepStart((long) -getSessionTimeout());

        Session session = getSessionFactory().load();

        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testSessionValidIfRequestParamsNotChanged() throws Exception {
        prepareRequestParamsTest();

        Session session = getSessionFactory().create(mock(SessionArguments.class));
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isTrue();
    }

    @Test
    public void testSessionInvalidIfParamOsVersionChanged() throws Exception {
        ReportRequestConfig reportRequestConfig = prepareRequestParamsTest();
        doReturn("321").when(reportRequestConfig).getOsVersion();

        Session session = getSessionFactory().create(mock(SessionArguments.class));
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testSessionInvalidIfParamOsApiLevelChanged() throws Exception {
        ReportRequestConfig reportRequestConfig = prepareRequestParamsTest();
        doReturn(99).when(reportRequestConfig).getOsApiLevel();

        Session session = getSessionFactory().create(mock(SessionArguments.class));
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testSessionInvalidIfKitBuildNumberParamChanged() throws Exception {
        ReportRequestConfig reportRequestConfig = prepareRequestParamsTest();
        doReturn("9876").when(reportRequestConfig).getKitBuildNumber();

        Session session = getSessionFactory().create(mock(SessionArguments.class));
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testSessionInvalidIfSdkVersionParamChanged() throws Exception {
        ReportRequestConfig reportRequestConfig = prepareRequestParamsTest();
        doReturn("9876").when(reportRequestConfig).getAnalyticsSdkVersionName();

        Session session = getSessionFactory().create(mock(SessionArguments.class));
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testSessionInvalidIfAppVersionParamChanged() throws Exception {
        ReportRequestConfig reportRequestConfig = prepareRequestParamsTest();
        doReturn("9876").when(reportRequestConfig).getAppVersion();

        Session session = getSessionFactory().create(mock(SessionArguments.class));
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testSessionInvalidIfAppBuildNumberParamChanged() throws Exception {
        ReportRequestConfig reportRequestConfig = prepareRequestParamsTest();
        doReturn("9876").when(reportRequestConfig).getAppBuildNumber();

        Session session = getSessionFactory().create(mock(SessionArguments.class));
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testSessionInvalidIfAttributionIdParamChanged() throws Exception {
        ReportRequestConfig reportRequestConfig = prepareRequestParamsTest();
        when(reportRequestConfig.getAttributionId()).thenReturn(231);

        Session session = getSessionFactory().create(mock(SessionArguments.class));
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testSessionInvalidIfLengthMoreThanOneDay() throws Exception {
        mSessionStorage.putCreationTime(-(SessionDefaults.SESSION_MAX_LENGTH_SEC + 1) * 1000);
        Session session = getSessionFactory().load();
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testSessionValidIfLengthLessThanOneDay() throws Exception {
        prepareRequestParamsTest();

        mSessionStorage.putCreationTime(-(SessionDefaults.SESSION_MAX_LENGTH_SEC - 1) * 1000);
        when(mSessionStorage.getSessionId()).thenReturn(1L);
        Session session = getSessionFactory().load();
        assertThat(session.isValid(SystemClock.elapsedRealtime())).isTrue();
    }

    @Test
    public void testSessionInvalidIfRequestParametersIsNotExists() throws Exception {
        mSessionStorage.putCreationTime(-(SessionDefaults.SESSION_MAX_LENGTH_SEC - 1) * 1000);
        Session session = getSessionFactory().load();

        assertThat(session.isValid(SystemClock.elapsedRealtime())).isFalse();
    }

    @Test
    public void testDefaultAliveOffset() {
        int sleepStart = 1999;
        int initTime = 1000000;
        mSessionStorage.putSleepStart(sleepStart);
        mSessionStorage.putCreationTime(initTime);
        Session session = getSessionFactory().load();
        assertThat(session.getAliveReportOffset()).isEqualTo(999);
    }

    @Test
    public void testLoadAliveOffset() {
        int lastEventOffset = new Random().nextInt(1000);
        mSessionStorage.putLastEventOffset(lastEventOffset);
        Session session = getSessionFactory().load();
        assertThat(session.getAliveReportOffset()).isEqualTo(lastEventOffset);
    }

    @Test
    public void testUseLastEventTimeAsAliveOffset() {
        long initTime = 300000;
        when(timeProvider.elapsedRealtime()).thenReturn(initTime);
        mSessionStorage.putSleepStart(TimeUnit.MILLISECONDS.toSeconds(initTime) + 1);
        mSessionStorage.putCreationTime(initTime);
        Session session = getSessionFactory().load();
        assertThat(session.getAliveReportOffset()).isEqualTo(1);
        long lastEventTime = session.getAndUpdateLastEventTime(initTime + 3000);
        assertThat(lastEventTime).isEqualTo(3);
        assertThat(session.getAliveReportOffset()).isEqualTo(lastEventTime);
    }

    @Test
    public void testUseSleepStartTimeAsAliveOffset() {
        long initTime = 300000;
        mSessionStorage.putSleepStart(TimeUnit.MILLISECONDS.toSeconds(initTime) + 1);
        mSessionStorage.putCreationTime(initTime);
        Session session = getSessionFactory().load();
        assertThat(session.getAliveReportOffset()).isEqualTo(1);
        session.updateLastActiveTime(initTime + 5000);
        assertThat(session.getAliveReportOffset()).isEqualTo(5);
    }

    private ReportRequestConfig prepareRequestParamsTest() throws JSONException {
        ReportRequestConfig reportRequestConfig = getTestRequestConfig();
        doReturn(10).when(reportRequestConfig).getSessionTimeout();
        ContentValues params = getParamsDbValues(reportRequestConfig);

        doReturn(params).when(mDatabaseHelper).getSessionRequestParameters(anyLong(), any(SessionType.class));
        doReturn(reportRequestConfig).when(mComponent).getFreshReportRequestConfig();
        return reportRequestConfig;
    }

    public static ContentValues getParamsDbValues(final ReportRequestConfig reportRequestConfig) throws JSONException {
        ContentValues contentValues = new ContentValues();

        JSONObject requestParameters = new JSONObject();
        requestParameters.putOpt(Constants.RequestParametersJsonKeys.APP_VERSION, reportRequestConfig.getAppVersion());
        requestParameters.putOpt(Constants.RequestParametersJsonKeys.APP_BUILD, reportRequestConfig.getAppBuildNumber());
        requestParameters.putOpt(Constants.RequestParametersJsonKeys.ANALYTICS_SDK_VERSION_NAME, reportRequestConfig.getAnalyticsSdkVersionName());
        requestParameters.putOpt(Constants.RequestParametersJsonKeys.KIT_BUILD_NUMBER, reportRequestConfig.getKitBuildNumber());
        requestParameters.putOpt(Constants.RequestParametersJsonKeys.OS_VERSION, reportRequestConfig.getOsVersion());
        requestParameters.putOpt(Constants.RequestParametersJsonKeys.OS_API_LEVEL, reportRequestConfig.getOsApiLevel());
        requestParameters.putOpt(Constants.RequestParametersJsonKeys.ATTRIBUTION_ID, reportRequestConfig.getAttributionId());

        contentValues.put(Constants.SessionTable.SessionTableEntry.FIELD_SESSION_REPORT_REQUEST_PARAMETERS, requestParameters.toString());
        return contentValues;
    }

    private ReportRequestConfig getTestRequestConfig() {
        return mock(ReportRequestConfig.class);
    }

}
