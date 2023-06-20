package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import java.util.Collection;

public class ActivityLifecycleManager extends DefaultActivityLifecycleCallbacks {

    private static final String TAG = "[ActivityLifecycleManager]";

    public enum WatchingStatus {
        WATCHING(null),
        NO_APPLICATION("Bad application object"),
        NOT_WATCHING_YET("Internal inconsistency");

        public final String error;

        WatchingStatus(String error) {
            this.error = error;
        }
    }

    public enum ActivityEvent {
        CREATED, RESUMED, PAUSED, STARTED, STOPPED, DESTROYED
    }

    interface Listener {
        @MainThread
        void onEvent(@NonNull Activity activity, @NonNull ActivityEvent event);
    }

    @Nullable
    private Application application;
    @NonNull
    private volatile WatchingStatus watchingStatus = WatchingStatus.NOT_WATCHING_YET;

    @NonNull
    private final HashMultimap<ActivityLifecycleManager.ActivityEvent, ActivityLifecycleManager.Listener> listeners =
            new HashMultimap<ActivityLifecycleManager.ActivityEvent, ActivityLifecycleManager.Listener>(true);

    @AnyThread
    public synchronized void registerListener(@NonNull ActivityLifecycleManager.Listener listener,
                                              @Nullable ActivityLifecycleManager.ActivityEvent... events) {
        YLogger.info(TAG, "register listener %s", listener);
        for (ActivityLifecycleManager.ActivityEvent event : eventsOrAll(events)) {
            listeners.put(event, listener);
        }
        maybeInitInternal();
    }

    @AnyThread
    public synchronized void unregisterListener(@NonNull ActivityLifecycleManager.Listener listener,
                                                ActivityLifecycleManager.ActivityEvent... events) {
        YLogger.info(TAG, "unregister listener %s", listener);
        for (ActivityLifecycleManager.ActivityEvent event : eventsOrAll(events)) {
            listeners.remove(event, listener);
        }
        maybeUnregister();
    }

    @AnyThread
    public synchronized void maybeInit(@NonNull Context context) {
        if (application == null) {
            try {
                application = (Application) context.getApplicationContext();
            } catch (Throwable ex) {
                YLogger.error(TAG, ex, "Context is not application");
            }
        }
        maybeInitInternal();
    }

    @AnyThread
    public synchronized void maybeInit(@NonNull Application application) {
        if (this.application == null) {
            this.application = application;
        }
        maybeInitInternal();
    }

    @AnyThread
    private synchronized void maybeInitInternal() {
        if (watchingStatus == WatchingStatus.WATCHING || listeners.isEmpty()) {
            return;
        }
        if (application == null) {
            watchingStatus = WatchingStatus.NO_APPLICATION;
            return;
        }
        watchingStatus = WatchingStatus.WATCHING;
        YLogger.info(TAG, "register activity lifecycle callbacks");
        application.registerActivityLifecycleCallbacks(this);
    }

    @AnyThread
    private synchronized void maybeUnregister() {
        if (watchingStatus == WatchingStatus.WATCHING && listeners.isEmpty()) {
            watchingStatus = WatchingStatus.NOT_WATCHING_YET;
            YLogger.info(TAG, "unregister activity lifecycle callbacks");
            application.unregisterActivityLifecycleCallbacks(this);
        }
    }

    @Override
    @MainThread
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        notifyListeners(ActivityLifecycleManager.ActivityEvent.CREATED, activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        notifyListeners(ActivityEvent.STARTED, activity);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        notifyListeners(ActivityEvent.STOPPED, activity);
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        notifyListeners(ActivityEvent.DESTROYED, activity);
    }

    @Override
    @MainThread
    public void onActivityResumed(@NonNull final Activity activity) {
        notifyListeners(ActivityLifecycleManager.ActivityEvent.RESUMED, activity);
    }

    @Override
    @MainThread
    public void onActivityPaused(@NonNull final Activity activity) {
        notifyListeners(ActivityLifecycleManager.ActivityEvent.PAUSED, activity);
    }

    @NonNull
    @AnyThread
    public WatchingStatus getWatchingStatus() {
        return watchingStatus;
    }

    private void notifyListeners(@NonNull ActivityLifecycleManager.ActivityEvent state, @NonNull Activity activity) {
        Collection<ActivityLifecycleManager.Listener> eventListeners;
        synchronized (this) {
            eventListeners = listeners.get(state);
        }
        if (eventListeners != null) {
            for (ActivityLifecycleManager.Listener listener : eventListeners) {
                listener.onEvent(activity, state);
            }
        }
    }

    @NonNull
    private ActivityEvent[] eventsOrAll(@Nullable ActivityEvent[] events) {
        return Utils.isNullOrEmpty(events) ? ActivityEvent.values() : events;
    }
}
