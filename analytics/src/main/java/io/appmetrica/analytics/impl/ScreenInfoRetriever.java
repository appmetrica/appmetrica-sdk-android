package io.appmetrica.analytics.impl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.coreapi.internal.model.ScreenInfo;
import io.appmetrica.analytics.coreutils.internal.AndroidUtils;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.lang.ref.WeakReference;

public class ScreenInfoRetriever implements ActivityAppearedListener.Listener {

    private static final String TAG = "[ScreenInfoRetriever]";

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static volatile ScreenInfoRetriever sInstance;

    @NonNull
    public static ScreenInfoRetriever getInstance(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (ScreenInfoRetriever.class) {
                if (sInstance == null) {
                    sInstance = new ScreenInfoRetriever(context);
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private final Context context;
    @Nullable
    private ScreenInfo screenInfo;
    @NonNull
    private WeakReference<Activity> activityHolder = new WeakReference<>(null);
    @NonNull
    private final PreferencesClientDbStorage clientPreferences;
    @NonNull
    private final ScreenInfoExtractor screenInfoExtractor;
    private boolean checkedByDeprecated;

    private ScreenInfoRetriever(@NonNull Context context) {
        this(
                context,
                new PreferencesClientDbStorage(
                        DatabaseStorageFactory.getInstance(context).getClientDbHelper()
                ),
                new ScreenInfoExtractor()
        );
    }

    @VisibleForTesting
    ScreenInfoRetriever(@NonNull Context context,
                        @NonNull PreferencesClientDbStorage clientPreferences,
                        @NonNull ScreenInfoExtractor screenInfoExtractor) {
        this.context = context;
        this.clientPreferences = clientPreferences;
        this.screenInfoExtractor = screenInfoExtractor;
        this.screenInfo = this.clientPreferences.getScreenInfo();
        this.checkedByDeprecated = this.clientPreferences.isScreenSizeCheckedByDeprecated();
        ClientServiceLocator.getInstance().getActivityAppearedListener().registerListener(this);
    }

    @Nullable
    @WorkerThread
    public synchronized ScreenInfo retrieveScreenInfo() {
        tryToUpdateScreenInfo(activityHolder.get());
        if (screenInfo == null) {
            if (AndroidUtils.isApiAchieved(Build.VERSION_CODES.R)) {
                DebugLogger.info(TAG, "Screen info not found. Maybe update by deprecated method. " +
                        "checkedByDeprecated = %b", checkedByDeprecated);
                if (!checkedByDeprecated) {
                    tryToUpdateScreenInfo(context);
                    checkedByDeprecated = true;
                    clientPreferences.markScreenSizeCheckedByDeprecated();
                }
            } else {
                tryToUpdateScreenInfo(context);
            }
        }
        return screenInfo;
    }

    @Override
    @WorkerThread
    public synchronized void onActivityAppeared(@NonNull Activity activity) {
        DebugLogger.info(TAG, "Activity appeared: %s", activity);
        activityHolder = new WeakReference<>(activity);
        if (screenInfo == null) {
            tryToUpdateScreenInfo(activity);
        }
    }

    private void tryToUpdateScreenInfo(@Nullable Context context) {
        DebugLogger.info(TAG, "try to update screen info for context: %s", context);
        if (context != null) {
            ScreenInfo newScreenInfo = screenInfoExtractor.extractScreenInfo(context);
            DebugLogger.info(TAG, "Extracted screen info: %s, old screen info: %s", newScreenInfo, screenInfo);
            if (newScreenInfo != null && !newScreenInfo.equals(screenInfo)) {
                screenInfo = newScreenInfo;
                clientPreferences.saveScreenInfo(screenInfo);
            }
        }
    }
}
