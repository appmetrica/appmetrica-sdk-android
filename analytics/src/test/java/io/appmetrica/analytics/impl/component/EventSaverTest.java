package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.component.session.SessionState;
import io.appmetrica.analytics.impl.component.sessionextras.SessionExtrasHolder;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.utils.encryption.EncryptedCounterReport;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypter;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypterProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class EventSaverTest extends CommonTest {

    @Mock
    private PreferencesComponentDbStorage mPreferences;
    @Mock
    private VitalComponentDataProvider vitalComponentDataProvider;
    @Mock
    private SessionManagerStateMachine mSessionManager;
    @Mock
    private DatabaseHelper mDbHelper;
    @Mock
    private AppEnvironment mAppEnvironment;
    @Mock
    private EventEncrypterProvider mEventEncrypterProvider;
    @Mock
    private EventSaver.ReportSavedListener mReportSavedListener;
    @Mock
    private EventNumberGenerator mEventNumberGenerator;
    @Mock
    private TimeProvider mTimeProvider;
    @Mock
    private CounterReport mCounterReport;
    @Mock
    private SessionState mSessionState;
    @Mock
    private EncryptedCounterReport mEncryptedCounterReport;
    @Mock
    private EventEncrypter mEventEncrypter;
    @Mock
    private SessionExtrasHolder sessionExtrasHolder;
    private AppEnvironment.EnvironmentRevision mRevision;
    private final String mProfileId = "profile id";
    private final int curAppVersion = 10;
    private EventSaver mEventSaver;
    private long mIdentityEventSendTime = 5000;
    private long mPermissionsCheckTime = 7000;
    private int mLastAppVersionWithCollectedFeatures = 5;
    private final int mReportType = 15;
    private final int openId = 6655;
    private final HashMap<String, byte[]> extras = new HashMap<>();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mCounterReport.getType()).thenReturn(mReportType);
        when(mPreferences.getPermissionsEventSendTime()).thenReturn(mPermissionsCheckTime);
        when(mPreferences.putPermissionsCheckTime(anyLong())).thenReturn(mPreferences);
        when(mPreferences.putLastAppVersionWithFeatures(anyInt())).thenReturn(mPreferences);

        when(mPreferences.getProfileID()).thenReturn(mProfileId);
        when(vitalComponentDataProvider.getOpenId()).thenReturn(openId);
        mRevision = new AppEnvironment.EnvironmentRevision("value", 5);
        when(mAppEnvironment.getLastRevision()).thenReturn(mRevision);

        when(mCounterReport.getExtras()).thenReturn(extras);

        mEventSaver = createReportSaverWithLastAppVersionsWithFeatures(curAppVersion);
    }

    @Test
    public void testSavePermissionsEventSendTime() {
        final long currentTime = 1100;
        when(mTimeProvider.currentTimeSeconds()).thenReturn(currentTime);
        mEventSaver.savePermissionsCheckTime();
        verify(mPreferences).putPermissionsCheckTime(currentTime);
        verify(mPreferences).commit();
    }

    @Test
    public void testSaveFeaturesCheckVersion() {
        mEventSaver.saveFeaturesCheckVersion();
        verify(mPreferences).putLastAppVersionWithFeatures(curAppVersion);
        verify(mPreferences).commit();
    }

    @Test
    public void testGetPermissionsCheckTime() {
        final long currentTime = 555555;
        when(mTimeProvider.currentTimeSeconds()).thenReturn(currentTime);
        mEventSaver.savePermissionsCheckTime();
        assertThat(mEventSaver.getPermissionsCheckTime()).isEqualTo(currentTime);
    }

    @Test
    public void testWasLastFeaturesEventLongAgoSameVersion() {
        assertThat(mEventSaver.wasLastFeaturesEventLongAgo()).isFalse();
    }

    @Test
    public void testLastAppVersionWithCollectedFeaturesUpdated() {
        EventSaver eventSaver = createReportSaverWithLastAppVersionsWithFeatures(curAppVersion - 1);
        assertThat(eventSaver.wasLastFeaturesEventLongAgo()).isTrue();
        eventSaver.saveFeaturesCheckVersion();
        assertThat(eventSaver.wasLastFeaturesEventLongAgo()).isFalse();
    }

    @Test
    public void testSaveReport() {
        when(mEventEncrypterProvider.getEventEncrypter(mCounterReport)).thenReturn(mEventEncrypter);
        when(mEventEncrypter.encrypt(mCounterReport)).thenReturn(mEncryptedCounterReport);
        mEventSaver.saveReport(mCounterReport, mSessionState);
        verify(mCounterReport).setProfileID(mProfileId);
        verify(mCounterReport).setOpenId(openId);
        verify(mDbHelper).saveReport(mEncryptedCounterReport, mReportType, mSessionState, mRevision, mEventNumberGenerator);
        verify(mReportSavedListener).onReportSaved();
    }

    @Test
    public void saveReportWithoutExtrasAndSessionExtras() {
        when(sessionExtrasHolder.getSnapshot()).thenReturn(Collections.<String, byte[]>emptyMap());
        when(mEventEncrypterProvider.getEventEncrypter(mCounterReport)).thenReturn(mEventEncrypter);
        when(mEventEncrypter.encrypt(mCounterReport)).thenReturn(mEncryptedCounterReport);
        mEventSaver.saveReport(mCounterReport, mSessionState);
        verify(mDbHelper)
            .saveReport(mEncryptedCounterReport, mReportType, mSessionState, mRevision, mEventNumberGenerator);
        verify(mReportSavedListener).onReportSaved();

        assertThat(extras).isEmpty();
    }

    @Test
    public void saveReportWithExtrasWithoutSessionExtras() {
        String extraKey = "Event extra key";
        byte[] extraValue = new byte[] {1, 5, 7, 1, 6};
        extras.put(extraKey, extraValue);
        when(sessionExtrasHolder.getSnapshot()).thenReturn(Collections.<String, byte[]>emptyMap());
        when(mEventEncrypterProvider.getEventEncrypter(mCounterReport)).thenReturn(mEventEncrypter);
        when(mEventEncrypter.encrypt(mCounterReport)).thenReturn(mEncryptedCounterReport);
        mEventSaver.saveReport(mCounterReport, mSessionState);
        verify(mDbHelper)
            .saveReport(mEncryptedCounterReport, mReportType, mSessionState, mRevision, mEventNumberGenerator);
        verify(mReportSavedListener).onReportSaved();

        assertThat(extras).containsExactlyEntriesOf(Collections.singletonMap(extraKey, extraValue));
    }

    @Test
    public void saveReportWithoutExtrasWithSessionExtras() {
        String sessionExtraKey = "Session extra key";
        byte[] sessionExtraValue = "Session extra value".getBytes(StandardCharsets.UTF_8);
        when(sessionExtrasHolder.getSnapshot())
            .thenReturn(Collections.singletonMap(sessionExtraKey, sessionExtraValue));
        when(mEventEncrypterProvider.getEventEncrypter(mCounterReport)).thenReturn(mEventEncrypter);
        when(mEventEncrypter.encrypt(mCounterReport)).thenReturn(mEncryptedCounterReport);
        mEventSaver.saveReport(mCounterReport, mSessionState);
        verify(mDbHelper)
            .saveReport(mEncryptedCounterReport, mReportType, mSessionState, mRevision, mEventNumberGenerator);
        verify(mReportSavedListener).onReportSaved();

        assertThat(extras).containsExactlyEntriesOf(Collections.singletonMap(sessionExtraKey, sessionExtraValue));
    }

    @Test
    public void saveReportWithExtrasAndSessionExtras() {
        String eventExtraKey = "Event extra key";
        byte[] eventExtraValue = "Event extra value".getBytes(StandardCharsets.UTF_8);
        String sessionExtraKey = "Session extra key";
        byte[] sessionExtraValue = "Session extra value".getBytes(StandardCharsets.UTF_8);

        Map<String, byte[]> expectedExtras = new HashMap<>();
        expectedExtras.put(eventExtraKey, eventExtraValue);
        expectedExtras.put(sessionExtraKey, sessionExtraValue);

        extras.put(eventExtraKey, eventExtraValue);
        when(sessionExtrasHolder.getSnapshot())
            .thenReturn(Collections.singletonMap(sessionExtraKey, sessionExtraValue));

        when(mEventEncrypterProvider.getEventEncrypter(mCounterReport)).thenReturn(mEventEncrypter);
        when(mEventEncrypter.encrypt(mCounterReport)).thenReturn(mEncryptedCounterReport);
        mEventSaver.saveReport(mCounterReport, mSessionState);
        verify(mDbHelper)
            .saveReport(mEncryptedCounterReport, mReportType, mSessionState, mRevision, mEventNumberGenerator);
        verify(mReportSavedListener).onReportSaved();

        assertThat(extras).containsExactlyEntriesOf(expectedExtras);
    }

    @Test
    public void testIdentifyAndSaveReport() {
        prepareReportSaver(mCounterReport);
        mEventSaver.identifyAndSaveReport(mCounterReport);
        verifyReportSaved(mCounterReport);
    }

    @Test
    public void testSavePermissionsReport() {
        prepareReportSaver(mCounterReport);
        final long currentTime = 1100;
        when(mTimeProvider.currentTimeSeconds()).thenReturn(currentTime);
        mEventSaver.savePermissionsReport(mCounterReport);
        verifyReportSaved(mCounterReport);
        verify(mPreferences).putPermissionsCheckTime(currentTime);
        verify(mPreferences).commit();
    }

    @Test
    public void testSaveFeaturesReport() {
        prepareReportSaver(mCounterReport);
        mEventSaver.saveFeaturesReport(mCounterReport);
        verifyReportSaved(mCounterReport);
        verify(mPreferences).putLastAppVersionWithFeatures(curAppVersion);
        verify(mPreferences).commit();
    }

    @Test
    public void testIdentifyAndSaveFirstEventReport() {
        prepareReportSaver(mCounterReport);
        mEventSaver.identifyAndSaveFirstEventReport(mCounterReport);
        verify(mSessionManager).getSomeSession(mCounterReport);
    }

    @Test
    public void testSaveReportWithCurrentSession() {
        final long timestamp = 123456000;
        when(mCounterReport.getCreationTimestamp()).thenReturn(timestamp);
        prepareReportSaver(mCounterReport);
        SessionState sessionState = mock(SessionState.class);
        when(mSessionManager.peekCurrentSessionState(mCounterReport)).thenReturn(sessionState);
        mEventSaver.saveReportFromPrevSession(mCounterReport);
        verifyReportSaved(mCounterReport, sessionState);
    }

    private void prepareReportSaver(CounterReport counterReport) {
        when(mEventEncrypterProvider.getEventEncrypter(mCounterReport)).thenReturn(mEventEncrypter);
        when(mEventEncrypter.encrypt(counterReport)).thenReturn(mEncryptedCounterReport);
        when(mSessionManager.getCurrentSessionState(counterReport)).thenReturn(mSessionState);
    }

    private void verifyReportSaved(CounterReport counterReport) {
        verifyReportSaved(counterReport, mSessionState);
    }

    private void verifyReportSaved(CounterReport counterReport, SessionState sessionState) {
        verify(counterReport).setProfileID(mProfileId);
        verify(counterReport).setOpenId(openId);
        verify(mDbHelper).saveReport(mEncryptedCounterReport, mReportType, sessionState, mRevision, mEventNumberGenerator);
        verify(mReportSavedListener).onReportSaved();
    }

    private EventSaver createReportSaverWithLastAppVersionsWithFeatures(final int lastAppVersionWithCollectedFeatures) {
        mLastAppVersionWithCollectedFeatures = lastAppVersionWithCollectedFeatures;
        when(mPreferences.getLastAppVersionWithFeatures()).thenReturn(mLastAppVersionWithCollectedFeatures);
        return new EventSaver(
                mPreferences,
                vitalComponentDataProvider,
                mSessionManager,
                mDbHelper,
                mAppEnvironment,
                mEventEncrypterProvider,
                sessionExtrasHolder,
                curAppVersion,
                mReportSavedListener,
                mEventNumberGenerator,
                mTimeProvider
        );
    }
}
