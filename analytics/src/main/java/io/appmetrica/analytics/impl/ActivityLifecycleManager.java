package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleRegistry;
import io.appmetrica.analytics.impl.utils.collection.HashMultimap;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.Collection;

public class ActivityLifecycleManager extends DefaultActivityLifecycleCallbacks implements ActivityLifecycleRegistry {

    public ActivityLifecycleManager() {}

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

    @Nullable
    private Application application;
    @NonNull
    private volatile WatchingStatus watchingStatus = WatchingStatus.NOT_WATCHING_YET;

    @NonNull
    private final HashMultimap<ActivityEvent, ActivityLifecycleListener> listeners =
        new HashMultimap<>(true);

    @AnyThread
    @Override
    public synchronized void registerListener(
        @NonNull ActivityLifecycleListener listener,
        @NonNull ActivityEvent... events
    ) {
        DebugLogger.INSTANCE.info(TAG, "register listener %s", listener);
        for (ActivityEvent event : eventsOrAll(events)) {
            listeners.put(event, listener);
        }
        maybeInitInternal();
    }

    @AnyThread
    @Override
    public synchronized void unregisterListener(
        @NonNull ActivityLifecycleListener listener,
        @NonNull ActivityEvent... events
    ) {
        DebugLogger.INSTANCE.info(TAG, "unregister listener %s", listener);
        for (ActivityEvent event : eventsOrAll(events)) {
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
                DebugLogger.INSTANCE.error(TAG, ex, "Context is not application");
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
        DebugLogger.INSTANCE.info(TAG, "register activity lifecycle callbacks");
        application.registerActivityLifecycleCallbacks(this);
    }

    @AnyThread
    private synchronized void maybeUnregister() {
        if (watchingStatus == WatchingStatus.WATCHING && listeners.isEmpty()) {
            watchingStatus = WatchingStatus.NOT_WATCHING_YET;
            DebugLogger.INSTANCE.info(TAG, "unregister activity lifecycle callbacks");
            if (application != null) {
                application.unregisterActivityLifecycleCallbacks(this);
            }
        }
    }

    @Override
    @MainThread
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        notifyListeners(ActivityEvent.CREATED, activity);
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
        notifyListeners(ActivityEvent.RESUMED, activity);
    }

    @Override
    @MainThread
    public void onActivityPaused(@NonNull final Activity activity) {
        notifyListeners(ActivityEvent.PAUSED, activity);
    }

    @NonNull
    @AnyThread
    public WatchingStatus getWatchingStatus() {
        return watchingStatus;
    }

    private void notifyListeners(@NonNull ActivityEvent state, @NonNull Activity activity) {
        Collection<ActivityLifecycleListener> eventListeners;
        synchronized (this) {
            eventListeners = listeners.get(state);
        }
        if (eventListeners != null) {
            for (ActivityLifecycleListener listener : eventListeners) {
                listener.onEvent(activity, state);
            }
        }
    }

    @NonNull
    private ActivityEvent[] eventsOrAll(@Nullable ActivityEvent[] events) {
        return Utils.isNullOrEmpty(events) ? ActivityEvent.values() : events;
    }
}
