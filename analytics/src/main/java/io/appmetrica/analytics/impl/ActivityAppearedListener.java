package io.appmetrica.analytics.impl;

import android.app.Activity;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import java.util.HashSet;
import java.util.Set;

public class ActivityAppearedListener implements ActivityLifecycleManager.Listener {

    public interface Listener {

        @WorkerThread
        void onActivityAppeared(@NonNull Activity activity);
    }

    @NonNull
    private final Set<Listener> listeners = new HashSet<>();

    public ActivityAppearedListener(@NonNull ActivityLifecycleManager activityLifecycleManager) {
        activityLifecycleManager.registerListener(this);
    }

    public synchronized void registerListener(@NonNull Listener listener) {
        listeners.add(listener);
    }

    @ApiProxyThread
    public void onActivityAppeared(@NonNull Activity activity) {
        Set<Listener> listenersSnapshot;
        synchronized (this) {
            listenersSnapshot = new HashSet<>(listeners);
        }
        for (Listener listener : listenersSnapshot) {
            listener.onActivityAppeared(activity);
        }
    }

    @Override
    @MainThread
    public void onEvent(@NonNull final Activity activity, @NonNull ActivityLifecycleManager.ActivityEvent event) {
        ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor().execute(new Runnable() {
            @Override
            @ApiProxyThread
            public void run() {
                onActivityAppeared(activity);
            }
        });
    }
}
