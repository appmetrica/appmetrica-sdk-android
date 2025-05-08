package io.appmetrica.analytics.impl.component.session;

import android.content.ContentValues;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SessionTests extends CommonTest {

    private static final long SLEEP_START_TIME = 435454645L;
    private static final long SESSION_CREATION_TIME = SLEEP_START_TIME - TimeUnit.SECONDS.toMillis(10000);
    private static final int SESSION_TIMEOUT = 100;

    private ComponentUnit mComponentUnit;
    private SessionStorageImpl mSessionStorage;
    private SessionArgumentsInternal mSessionArguments;
    private SystemTimeProvider mSystemTimeProvider;
    private DatabaseHelper mDatabaseHelper;

    private Session mSession;

    @Before
    public void setUp() throws Exception {
        mComponentUnit = mock(ComponentUnit.class);
        mSessionStorage = mock(SessionStorageImpl.class);
        mSessionArguments = mock(SessionArgumentsInternal.class);
        when(mSessionArguments.getCreationTime(anyLong())).thenReturn(SESSION_CREATION_TIME);
        when(mSessionArguments.getType()).thenReturn(SessionType.FOREGROUND);
        mSystemTimeProvider = mock(SystemTimeProvider.class);
        when(mSessionArguments.getId(anyLong())).thenReturn(0L);
        mDatabaseHelper = mock(DatabaseHelper.class);
        when(mDatabaseHelper.getSessionRequestParameters(anyLong(), any(SessionType.class)))
            .thenReturn(getSessionRequestParameters());
        when(mComponentUnit.getDbHelper()).thenReturn(mDatabaseHelper);
        doReturn(getRequestConfig()).when(mComponentUnit).getFreshReportRequestConfig();

        mSession = new Session(mComponentUnit, mSessionStorage, mSessionArguments, mSystemTimeProvider);
    }

    @Test
    public void testIsValidIfNotExpired() {
        long elapsedRealtime = SLEEP_START_TIME + TimeUnit.SECONDS.toMillis(SESSION_TIMEOUT) - 10000;
        when(mSessionArguments.getSleepStart(anyLong())).thenReturn(SLEEP_START_TIME);
        when(mSessionArguments.getTimeout(anyInt())).thenReturn(SESSION_TIMEOUT);
        doReturn(elapsedRealtime).when(mSystemTimeProvider).elapsedRealtime();
        mSession = new Session(mComponentUnit, mSessionStorage, mSessionArguments, mSystemTimeProvider);
        assertThat(mSession.isValid(elapsedRealtime)).isTrue();
    }

    @Test
    public void testIsNotValidIfExpired() {
        long elapsedRealtime = SLEEP_START_TIME + TimeUnit.SECONDS.toMillis(SESSION_TIMEOUT) + 10000;
        when(mSessionArguments.getSleepStart(anyLong())).thenReturn(TimeUnit.MILLISECONDS.toSeconds(SLEEP_START_TIME));
        when(mSessionArguments.getTimeout(anyInt())).thenReturn(SESSION_TIMEOUT);
        mSession = new Session(mComponentUnit, mSessionStorage, mSessionArguments, mSystemTimeProvider);
        assertThat(mSession.isValid(elapsedRealtime)).isFalse();
    }

    @Test
    public void testExpiredAfterReboot() {
        Long lastActiveTime = 345454656L;
        Long lastActiveTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(lastActiveTime);
        when(mSessionStorage.putSleepStart(anyLong())).thenReturn(mSessionStorage);

        doReturn(lastActiveTimeSeconds).when(mSessionArguments).getSleepStart(anyLong());
        doReturn(1000).when(mSessionArguments).getTimeout(anyInt());
        doReturn(lastActiveTime - 10000).when(mSessionArguments).getCreationTime(anyLong());

        Session session = new Session(mComponentUnit, mSessionStorage, mSessionArguments);

        session.updateLastActiveTime(lastActiveTime);

        assertThat(session.isExpired(lastActiveTime + 1000, 10)).isTrue();
    }

    @Test
    public void testNoExpiredIfNoReboot() {
        Long lastActiveTime = 345454656L;
        Long lastActiveTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(lastActiveTime);
        when(mSessionStorage.putSleepStart(anyLong())).thenReturn(mSessionStorage);

        doReturn(lastActiveTimeSeconds).when(mSessionArguments).getSleepStart(anyLong());
        doReturn(1000).when(mSessionArguments).getTimeout(anyInt());
        doReturn(lastActiveTime - 10000).when(mSessionArguments).getCreationTime(anyLong());

        Session session = new Session(mComponentUnit, mSessionStorage, mSessionArguments);

        session.updateLastActiveTime(lastActiveTime);

        assertThat(session.isExpired(lastActiveTime + 1000, lastActiveTime)).isFalse();
    }

    @Test
    public void testUpdateLastActiveTime() {
        Long value = 345454656L;
        when(mSessionStorage.putSleepStart(anyLong())).thenReturn(mSessionStorage);
        mSession.updateLastActiveTime(value);
        assertThat(mSession.getSleepStart()).isEqualTo(value);
        verify(mSessionStorage, times(1)).putSleepStart(value);
    }

    @Test
    public void testGetLastEventTimeOffset() {
        final long lastEventTimeOffsetSeconds = 123456;
        mSession.getAndUpdateLastEventTimeSeconds(SESSION_CREATION_TIME + TimeUnit.SECONDS.toMillis(lastEventTimeOffsetSeconds));
        assertThat(mSession.getLastEventTimeOffsetSeconds()).isEqualTo(lastEventTimeOffsetSeconds);
        // to make sure value was not updated
        assertThat(mSession.getLastEventTimeOffsetSeconds()).isEqualTo(lastEventTimeOffsetSeconds);
    }

    @Test
    public void toStringContainsExpectedFields() {
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(mSession.toString())
            .contains("id=0")
            .contains("creationTime=" + SESSION_CREATION_TIME)
            .contains("currentReportId=0")
            .contains("sessionRequestParams=null")
            .contains("sleepStart=0");
        assertions.assertAll();
    }

    private ContentValues getSessionRequestParameters() throws Exception {
        ContentValues contentValues = new ContentValues();
        JSONObject requestParameters = new JSONObject();
        requestParameters.put("key1", "value1");
        requestParameters.put("key2", "value2");
        contentValues.put("report_request_parameters", requestParameters.toString());
        return contentValues;
    }

    private ReportRequestConfig getRequestConfig() {
        ReportRequestConfig requestConfig = mock(ReportRequestConfig.class);

        doReturn("").when(requestConfig).getAnalyticsSdkVersionName();
        doReturn("").when(requestConfig).getAnalyticsSdkBuildNumber();
        doReturn("").when(requestConfig).getAppVersion();
        doReturn("").when(requestConfig).getAppBuildNumber();
        doReturn("").when(requestConfig).getOsVersion();
        doReturn(-1).when(requestConfig).getOsApiLevel();

        return requestConfig;
    }
}
