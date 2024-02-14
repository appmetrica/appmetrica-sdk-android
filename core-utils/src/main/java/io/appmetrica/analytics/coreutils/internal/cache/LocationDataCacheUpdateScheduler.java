package io.appmetrica.analytics.coreutils.internal.cache;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.annotations.GeoThread;
import io.appmetrica.analytics.coreapi.internal.cache.CacheUpdateScheduler;
import io.appmetrica.analytics.coreapi.internal.cache.UpdateConditionsChecker;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.locationapi.internal.ILastKnownUpdater;
import java.util.concurrent.TimeUnit;

public class LocationDataCacheUpdateScheduler implements CacheUpdateScheduler {

    private static final String TAG_PATTERN = "[LocationDataCacheUpdateScheduler-%s]";

    private static final int UPDATE_INTERVAL_SECONDS = 90;

    @NonNull
    private final ICommonExecutor mExecutor;
    @NonNull
    private final ILastKnownUpdater mLastKnownUpdater;
    @NonNull
    private final UpdateConditionsChecker mUpdateConditionsChecker;
    @NonNull
    private final Runnable mUpdateRunnable = new Runnable() {
        @GeoThread
        @Override
        public void run() {
            YLogger.info(tag, "Executing last known update.");
            mLastKnownUpdater.updateLastKnown();
        }
    };

    @NonNull
    private final Runnable mUpdateIfNeededRunnable = new Runnable() {
        @GeoThread
        @Override
        public void run() {
            final boolean shouldUpdate = mUpdateConditionsChecker.shouldUpdate();
            YLogger.info(tag, "Executing last known update. Should update? : %b", shouldUpdate);
            if (shouldUpdate) {
                mUpdateRunnable.run();
            }
        }
    };

    private final String tag;

    public LocationDataCacheUpdateScheduler(@NonNull ICommonExecutor executor,
                                            @NonNull ILastKnownUpdater lastKnownUpdater,
                                            @NonNull UpdateConditionsChecker updateConditionsChecker,
                                            @NonNull String tag) {
        mExecutor = executor;
        mLastKnownUpdater = lastKnownUpdater;
        mUpdateConditionsChecker = updateConditionsChecker;
        this.tag = String.format(TAG_PATTERN, tag);
    }

    public void startUpdates() {
        onStateUpdated();
    }

    @GeoThread
    public void stopUpdates() {
        mExecutor.remove(mUpdateRunnable);
        mExecutor.remove(mUpdateIfNeededRunnable);
    }

    @Override
    public void scheduleUpdateIfNeededNow() {
        YLogger.info(tag, "Schedule last known update now.");
        mExecutor.execute(mUpdateIfNeededRunnable);
    }

    @Override
    public void onStateUpdated() {
        mExecutor.remove(mUpdateRunnable);
        YLogger.info(tag, "Schedule update last known location by timer");
        mExecutor.executeDelayed(mUpdateRunnable, UPDATE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
}
