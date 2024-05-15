package io.appmetrica.analytics.impl.utils;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.IServerTimeOffsetProvider;
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.concurrent.TimeUnit;

public class ServerTime implements IServerTimeOffsetProvider {

    private static final String TAG = "[ServerTime]";

    private static class LazyServerTimeHolder {
        static ServerTime singletonInstance = new ServerTime();
    }

    public static ServerTime getInstance() {
        return LazyServerTimeHolder.singletonInstance;
    }

    private volatile long mServerTimeOffsetSeconds;
    private PreferencesServiceDbStorage mPreferences;
    private TimeProvider mTimeProvider;

    private ServerTime() { }

    @Override
    public synchronized long getServerTimeOffsetSeconds() {
        return mServerTimeOffsetSeconds;
    }

    public synchronized void init() {
        init(GlobalServiceLocator.getInstance().getServicePreferences(), new SystemTimeProvider());
    }

    public synchronized void updateServerTime(long serverTime, @Nullable final Long maxValidTimeDifference) {
        mServerTimeOffsetSeconds = (serverTime - mTimeProvider.currentTimeMillis()) / 1000;

        boolean uncheckedTimeDifference = mPreferences.isUncheckedTime(true);
        if (uncheckedTimeDifference) {
            if (maxValidTimeDifference != null) {
                long delta = Math.abs(serverTime - mTimeProvider.currentTimeMillis());
                mPreferences.putUncheckedTime(delta > TimeUnit.SECONDS.toMillis(maxValidTimeDifference));
            } else {
                mPreferences.putUncheckedTime(false);
            }
        }
        mPreferences.putServerTimeOffset(mServerTimeOffsetSeconds);
        mPreferences.commit();

        DebugLogger.info(TAG, "Server time updated, offset = " + mServerTimeOffsetSeconds);
    }

    public synchronized void disableTimeDifferenceChecking() {
        mPreferences.putUncheckedTime(false);
        mPreferences.commit();
    }

    public synchronized long currentTimeSec() {
        return TimeUtils.currentDeviceTimeSec() + mServerTimeOffsetSeconds;
    }

    public synchronized boolean isUncheckedTime() {
        return mPreferences.isUncheckedTime(true);
    }

    @VisibleForTesting
    public void init(PreferencesServiceDbStorage preferences, TimeProvider timeProvider) {
        mPreferences = preferences;
        mServerTimeOffsetSeconds = mPreferences.getServerTimeOffset(0);
        mTimeProvider = timeProvider;

        DebugLogger.info(TAG, "Server time initialized, offset = " + mServerTimeOffsetSeconds);
    }
}
