package io.appmetrica.analytics.impl.component;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.AppEnvironment;
import io.appmetrica.analytics.impl.CoreServiceEvent;
import io.appmetrica.analytics.impl.component.session.SessionManagerStateMachine;
import io.appmetrica.analytics.impl.component.session.SessionState;
import io.appmetrica.analytics.impl.component.sessionextras.SessionExtrasHolder;
import io.appmetrica.analytics.impl.db.DatabaseHelper;
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesComponentDbStorage;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

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
    private final SessionExtrasHolder sessionExtrasHolder;
    @NonNull
    private final AppEnvironment mAppEnvironment;
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
                      @NonNull SessionExtrasHolder sessionExtrasHolder,
                      final int currentAppVersion,
                      @NonNull ReportSavedListener reportSavedListener) {
        this(
                preferences,
                vitalComponentDataProvider,
                sessionManager,
                dbHelper,
                appEnvironment,
                sessionExtrasHolder,
                currentAppVersion,
                reportSavedListener,
                new SystemTimeProvider()
        );
    }

    @VisibleForTesting
    public EventSaver(@NonNull PreferencesComponentDbStorage preferences,
                      @NonNull VitalComponentDataProvider vitalComponentDataProvider,
                      @NonNull SessionManagerStateMachine sessionManager,
                      @NonNull DatabaseHelper dbHelper,
                      @NonNull AppEnvironment appEnvironment,
                      @NonNull SessionExtrasHolder sessionExtrasHolder,
                      final int currentAppVersion,
                      @NonNull ReportSavedListener reportSavedListener,
                      @NonNull TimeProvider timeProvider) {
        mPreferences = preferences;
        this.vitalComponentDataProvider = vitalComponentDataProvider;
        mSessionManager = sessionManager;
        mDbHelper = dbHelper;
        mAppEnvironment = appEnvironment;
        this.sessionExtrasHolder = sessionExtrasHolder;
        mCurrentAppVersion = currentAppVersion;
        mTimeProvider = timeProvider;
        mReportSavedListener = reportSavedListener;
        mPermissionsCheckTime = mPreferences.getPermissionsEventSendTime();
        mLastAppVersionWithCollectedFeatures = mPreferences.getLastAppVersionWithFeatures();

    }

    public void identifyAndSaveFirstEventReport(CoreServiceEvent serviceEvent) {
        mSessionManager.getSomeSession(serviceEvent); //workaround for first event
    }

    public void savePermissionsReport(CoreServiceEvent serviceEvent) {
        identifyAndSaveReport(serviceEvent);
        savePermissionsCheckTime();
    }

    public void saveFeaturesReport(CoreServiceEvent serviceEvent) {
        identifyAndSaveReport(serviceEvent);
        saveFeaturesCheckVersion();
    }

    public void identifyAndSaveReport(final CoreServiceEvent serviceEvent) {
        saveReport(serviceEvent, mSessionManager.getCurrentSessionState(serviceEvent));
    }

    public boolean saveReportFromPrevSession(@NonNull CoreServiceEvent serviceEvent) {
        SessionState sessionState = mSessionManager.peekCurrentSessionState(serviceEvent);
        DebugLogger.INSTANCE.info(
            TAG,
            "saveReportFromPrevSession: %s of type: %d; sessionState",
            serviceEvent.getName(),
            serviceEvent.getType(),
            sessionState
        );
        if (sessionState != null) {
            saveReport(serviceEvent, sessionState);
            return true;
        } else {
            DebugLogger.INSTANCE.error(TAG, "saveReportFromPrevSession: sessionState is null");
            return false;
        }
    }

    @VisibleForTesting
    public void saveReport(@NonNull final CoreServiceEvent serviceEvent, @NonNull final SessionState sessionState) {
        DebugLogger.INSTANCE.info(
            TAG,
            "saveReport: %s of type: %d",
            serviceEvent.getName(),
            serviceEvent.getType()
        );
        serviceEvent.getExtras().putAll(sessionExtrasHolder.getSnapshot());
        serviceEvent.setProfileID(mPreferences.getProfileID());
        serviceEvent.setOpenId(vitalComponentDataProvider.getOpenId());
        AppEnvironment.EnvironmentRevision revision = mAppEnvironment.getLastRevision();
        mDbHelper.saveReport(
                serviceEvent,
                serviceEvent.getType(),
                sessionState,
                revision,
                vitalComponentDataProvider
        );
        mReportSavedListener.onReportSaved();
    }

    public void savePermissionsCheckTime() {
        mPermissionsCheckTime = mTimeProvider.currentTimeSeconds();
        mPreferences.putPermissionsCheckTime(mPermissionsCheckTime);
    }

    public void saveFeaturesCheckVersion() {
        mLastAppVersionWithCollectedFeatures = mCurrentAppVersion;
        mPreferences.putLastAppVersionWithFeatures(mLastAppVersionWithCollectedFeatures);
    }

    public long getPermissionsCheckTime() {
        return mPermissionsCheckTime;
    }

    public boolean wasLastFeaturesEventLongAgo() {
        return mLastAppVersionWithCollectedFeatures < mCurrentAppVersion;
    }

}
