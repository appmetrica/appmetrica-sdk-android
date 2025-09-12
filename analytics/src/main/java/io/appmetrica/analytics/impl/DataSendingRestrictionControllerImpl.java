package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AppMetricaDefaultValues;
import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.utils.BooleanUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.HashSet;

public class DataSendingRestrictionControllerImpl implements DataSendingRestrictionController {

    public interface Storage {

        void storeRestrictionFromMainReporter(boolean value);

        @Nullable
        Boolean readRestrictionFromMainReporter();

    }

    public static class StorageImpl implements Storage {

        private final PreferencesServiceDbStorage mStorage;

        public StorageImpl(@NonNull PreferencesServiceDbStorage storage) {
            mStorage = storage;
        }

        @Override
        public void storeRestrictionFromMainReporter(boolean value) {
            mStorage.putDataSendingRestrictedFromMainReporter(value).commit();
        }

        @Override
        @Nullable
        public Boolean readRestrictionFromMainReporter() {
            return mStorage.getDataSendingRestrictedFromMainReporter();
        }
    }

    private static final String TAG = "[DataSendingRestrictionController]";

    private final Storage mPreferencesServiceDbStorage;

    @Nullable
    private Boolean mRestrictedInMainReporter;

    private final HashSet<String> mRestrictedReporters = new HashSet<String>();
    private final HashSet<String> mEnabledReporters = new HashSet<String>();

    public DataSendingRestrictionControllerImpl(@NonNull Storage storage) {
        mPreferencesServiceDbStorage = storage;
        mRestrictedInMainReporter = mPreferencesServiceDbStorage.readRestrictionFromMainReporter();
    }

    public synchronized void setEnabledFromMainReporterIfNotYet(@Nullable Boolean dataSendingEnabled) {
        if (mRestrictedInMainReporter == null) {
            DebugLogger.INSTANCE.info(TAG, "setEnabledFromMainReporterIfNotYet: %s", dataSendingEnabled);
            setEnabledFromMainReporter(dataSendingEnabled);
        }
    }

    public synchronized void setEnabledFromMainReporter(@Nullable Boolean dataSendingEnabled) {
        if (Utils.isFieldSet(dataSendingEnabled) || mRestrictedInMainReporter == null) {
            DebugLogger.INSTANCE.info(
                TAG,
                "setEnabledFromMainReporter: %s",
                dataSendingEnabled == null ? "null" : String.valueOf(dataSendingEnabled)
            );
            mRestrictedInMainReporter = BooleanUtils.isFalse(dataSendingEnabled);
            mPreferencesServiceDbStorage.storeRestrictionFromMainReporter(mRestrictedInMainReporter);
        }
    }

    public synchronized void setEnabledFromSharedReporter(@NonNull String apiKey,
                                                          @Nullable Boolean dataSendingEnabled) {
        if (Utils.isFieldSet(dataSendingEnabled) ||
                (mEnabledReporters.contains(apiKey) == false && mRestrictedReporters.contains(apiKey) == false)) {
            DebugLogger.INSTANCE.info(
                TAG,
                "setEnabledForReporter with API_KEY=%s: %s",
                apiKey,
                dataSendingEnabled == null ? "null" : String.valueOf(dataSendingEnabled)
            );
            if (WrapUtils.getOrDefault(dataSendingEnabled,
                    AppMetricaDefaultValues.DEFAULT_REPORTER_DATA_SENDING_ENABLED)) {
                mEnabledReporters.add(apiKey);
                mRestrictedReporters.remove(apiKey);
            } else {
                mRestrictedReporters.add(apiKey);
                mEnabledReporters.remove(apiKey);
            }
        }
    }

    @Override
    public synchronized boolean isRestrictedForMainReporter() {
        return BooleanUtils.isTrue(mRestrictedInMainReporter);
    }

    @Override
    public synchronized boolean isRestrictedForSdk() {

        boolean result = mRestrictedInMainReporter == null ? mEnabledReporters.isEmpty() : mRestrictedInMainReporter;

        DebugLogger.INSTANCE.info(
            TAG,
            "isRestrictedForAppMetrica = %b (restrictedInMainReporter = %s); enabledReportersCount = %d; " +
                "restrictedReportersCount = %d",
            result,
            mRestrictedInMainReporter == null ? "null" : String.valueOf(mRestrictedInMainReporter),
            mEnabledReporters.size(),
            mRestrictedReporters.size()
        );

        return result;
    }

    @Override
    public synchronized boolean isRestrictedForReporter(@NonNull String apiKey) {
        return mRestrictedReporters.contains(apiKey) || BooleanUtils.isTrue(mRestrictedInMainReporter);
    }
}
