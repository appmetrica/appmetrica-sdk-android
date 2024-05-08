package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.component.session.SessionState;
import io.appmetrica.analytics.impl.component.sessionextras.SessionExtrasHolder;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypter;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypterProvider;
import io.appmetrica.analytics.logger.internal.YLogger;

public class EventSaver {

    private static final String TAG = "[ReportSaver]";

    public interface ReportSavedListener {

        void onReportSaved();
    }

    @NonNull
    private final PreferencesComponentDbStorage mPreferences;
    @NonNull
    private final VitalComponentDataProvider vitalComponentDataProvider;
    @NonNull
    private SessionManagerStateMachine mSessionManager;
    @NonNull
    private DatabaseHelper mDbHelper;
    @NonNull
    private final EventEncrypterProvider mEventEncrypterProvider;
    @NonNull
    private final SessionExtrasHolder sessionExtrasHolder;
    @NonNull
    private final AppEnvironment mAppEnvironment;
    @NonNull
    private final EventNumberGenerator mEventNumberGenerator;
    @NonNull
    private ReportSavedListener mReportSavedListener;
    @NonNull
    private final TimeProvider mTimeProvider;
    private final int mCurrentAppVersion;
    private long mPermissionsCheckTime;
    private int mLastAppVersionWithCollectedFeatures;

    public EventSaver(@NonNull PreferencesComponentDbStorage preferences,
                      @NonNull VitalComponentDataProvider vitalComponentDataProvider,
                      @NonNull SessionManagerStateMachine sessionManager,
                      @NonNull DatabaseHelper dbHelper,
                      @NonNull AppEnvironment appEnvironment,
                      @NonNull EventEncrypterProvider eventEncrypterProvider,
                      @NonNull SessionExtrasHolder sessionExtrasHolder,
                      final int currentAppVersion,
                      @NonNull ReportSavedListener reportSavedListener) {
        this(
                preferences,
                vitalComponentDataProvider,
                sessionManager,
                dbHelper,
                appEnvironment,
                eventEncrypterProvider,
                sessionExtrasHolder,
                currentAppVersion,
                reportSavedListener,
                new EventNumberGenerator(vitalComponentDataProvider),
                new SystemTimeProvider()
        );
    }

    @VisibleForTesting
    public EventSaver(@NonNull PreferencesComponentDbStorage preferences,
                      @NonNull VitalComponentDataProvider vitalComponentDataProvider,
                      @NonNull SessionManagerStateMachine sessionManager,
                      @NonNull DatabaseHelper dbHelper,
                      @NonNull AppEnvironment appEnvironment,
                      @NonNull EventEncrypterProvider eventEncrypterProvider,
                      @NonNull SessionExtrasHolder sessionExtrasHolder,
                      final int currentAppVersion,
                      @NonNull ReportSavedListener reportSavedListener,
                      @NonNull EventNumberGenerator eventNumberGenerator,
                      @NonNull TimeProvider timeProvider) {
        mPreferences = preferences;
        this.vitalComponentDataProvider = vitalComponentDataProvider;
        mSessionManager = sessionManager;
        mDbHelper = dbHelper;
        mAppEnvironment = appEnvironment;
        mEventEncrypterProvider = eventEncrypterProvider;
        this.sessionExtrasHolder = sessionExtrasHolder;
        mCurrentAppVersion = currentAppVersion;
        mEventNumberGenerator = eventNumberGenerator;
        mTimeProvider = timeProvider;
        mReportSavedListener = reportSavedListener;
        mPermissionsCheckTime = mPreferences.getPermissionsEventSendTime();
        mLastAppVersionWithCollectedFeatures = mPreferences.getLastAppVersionWithFeatures();

    }

    public void identifyAndSaveFirstEventReport(CounterReport reportData) {
        mSessionManager.getSomeSession(reportData); //workaround for first event
    }

    public void savePermissionsReport(CounterReport report) {
        identifyAndSaveReport(report);
        savePermissionsCheckTime();
    }

    public void saveFeaturesReport(CounterReport report) {
        identifyAndSaveReport(report);
        saveFeaturesCheckVersion();
    }

    public void identifyAndSaveReport(final CounterReport reportData) {
        saveReport(reportData, mSessionManager.getCurrentSessionState(reportData));
    }

    public void saveReportFromPrevSession(@NonNull CounterReport report) {
        saveReport(report, mSessionManager.peekCurrentSessionState(report));
    }

    @VisibleForTesting
    public void saveReport(@NonNull final CounterReport reportData, @NonNull final SessionState sessionState) {
        YLogger.debug(TAG, "saveReport: %s of type: %d", reportData.getName(), reportData.getType());
        reportData.getExtras().putAll(sessionExtrasHolder.getSnapshot());
        reportData.setProfileID(mPreferences.getProfileID());
        reportData.setOpenId(vitalComponentDataProvider.getOpenId());
        AppEnvironment.EnvironmentRevision revision = mAppEnvironment.getLastRevision();
        EventEncrypter eventEncrypter = mEventEncrypterProvider.getEventEncrypter(reportData);
        mDbHelper.saveReport(
                eventEncrypter.encrypt(reportData),
                reportData.getType(),
                sessionState,
                revision,
                mEventNumberGenerator
        );
        mReportSavedListener.onReportSaved();
    }

    public void savePermissionsCheckTime() {
        mPermissionsCheckTime = mTimeProvider.currentTimeSeconds();
        mPreferences.putPermissionsCheckTime(mPermissionsCheckTime).commit();
    }

    public void saveFeaturesCheckVersion() {
        mLastAppVersionWithCollectedFeatures = mCurrentAppVersion;
        mPreferences.putLastAppVersionWithFeatures(mLastAppVersionWithCollectedFeatures).commit();
    }

    public long getPermissionsCheckTime() {
        return mPermissionsCheckTime;
    }

    public boolean wasLastFeaturesEventLongAgo() {
        return mLastAppVersionWithCollectedFeatures < mCurrentAppVersion;
    }

}
