package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AppMetricaDefaultValues;
import io.appmetrica.analytics.coreapi.internal.control.DataSendingRestrictionController;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.impl.utils.BooleanUtils;
import io.appmetrica.analytics.logger.internal.YLogger;
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

    public synchronized void setEnabledFromMainReporter(@Nullable Boolean dataSendingEnabled) {
        if (Utils.isFieldSet(dataSendingEnabled) || mRestrictedInMainReporter == null) {
            YLogger.d(
                    "%ssetEnabledFromMainReporter: %s",
                    TAG,
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
            YLogger.d(
                    "%ssetEnabledForReporter with API_KEY=%s: %s",
                    TAG,
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
    public synchronized boolean isRestrictedForReporter() {
        boolean result = mRestrictedInMainReporter == null
                ? mEnabledReporters.isEmpty() && mRestrictedReporters.isEmpty()
                : mRestrictedInMainReporter;

        YLogger.d(
                "%sisRestrictedForReporter = %b (mRestrictedInMainReporter = %s; mEnabledReportersCount = %d); " +
                        "mRestrictedReportersCount = %d",
                TAG,
                result,
                mRestrictedInMainReporter == null ? "null" : String.valueOf(mRestrictedInMainReporter),
                mEnabledReporters.size(),
                mRestrictedReporters.size()
        );

        return result;
    }

    @Override
    public synchronized boolean isRestrictedForSdk() {

        boolean result = mRestrictedInMainReporter == null ? mEnabledReporters.isEmpty() : mRestrictedInMainReporter;

        YLogger.d(
                "%sisRestrictedForAppMetrica = %b (mRestrictedInMainReporter = %s); mEnabledReportersCount = %d",
                TAG,
                result,
                mRestrictedInMainReporter == null ? "null" : String.valueOf(mRestrictedInMainReporter),
                mEnabledReporters.size()
        );

        return result;
    }

    @Override
    public boolean isRestrictedForBackgroundDataCollection() {
        boolean result = mRestrictedInMainReporter == null
                ? !mRestrictedReporters.isEmpty() || mEnabledReporters.isEmpty()
                : mRestrictedInMainReporter;

        YLogger.d(
                "%sisRestrictedForBackgroundDataCollection = %b (mRestrictedInMainReporter = %s; " +
                        "mRestrictedReporters = %s; mEnabledReporters = %s)",
                TAG,
                result,
                String.valueOf(mRestrictedInMainReporter),
                mRestrictedReporters.size(),
                mEnabledReporters.size()
        );
        return result;
    }
}
