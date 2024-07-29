package io.appmetrica.analytics.impl;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import io.appmetrica.analytics.impl.utils.ConditionalExecutor;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class SessionsTrackingManager {

    private static final String TAG = "[SessionsTrackingManager]";

    @NonNull
    private final ActivityLifecycleManager activityLifecycleManager;
    @NonNull
    private final ConditionalExecutor<MainReporter> conditionalExecutor;
    @NonNull
    private final ActivityLifecycleManager.Listener onResumedCallback;
    @NonNull
    private final ActivityLifecycleManager.Listener onPausedCallback;
    @NonNull
    private final ActivityStateManager activityStateManager;
    @NonNull
    private final ActivityAppearedListener activityAppearedListener;

    private boolean sessionAutotrackingStarted = false;

    public SessionsTrackingManager(@NonNull ActivityLifecycleManager activityLifecycleManager,
                                   @NonNull ActivityAppearedListener activityAppearedListener) {
        this(
            activityLifecycleManager,
            activityAppearedListener,
            new ConditionalExecutor<>(),
            new ActivityStateManager()
        );
    }

    @VisibleForTesting
    SessionsTrackingManager(@NonNull ActivityLifecycleManager activityLifecycleManager,
                            @NonNull ActivityAppearedListener activityAppearedListener,
                            @NonNull ConditionalExecutor<MainReporter> conditionalExecutor,
                            @NonNull ActivityStateManager activityStateManager) {
        this.activityLifecycleManager = activityLifecycleManager;
        this.activityAppearedListener = activityAppearedListener;
        this.conditionalExecutor = conditionalExecutor;
        this.activityStateManager = activityStateManager;
        onResumedCallback = (activity, event) -> {
            synchronized (SessionsTrackingManager.this) {
                DebugLogger.INSTANCE.info(TAG, "on resume callback");
                if (sessionAutotrackingStarted) {
                    DebugLogger.INSTANCE.info(TAG, "resume session");
                    SessionsTrackingManager.this.conditionalExecutor.addCommand(data ->
                        resumeActivityInternal(activity, data)
                    );
                }
            }
        };
        onPausedCallback = (activity, event) -> {
            DebugLogger.INSTANCE.info(TAG, "on pause callback");
            synchronized (SessionsTrackingManager.this) {
                if (sessionAutotrackingStarted) {
                    DebugLogger.INSTANCE.info(TAG, "pause session");
                    SessionsTrackingManager.this.conditionalExecutor.addCommand(data ->
                        pauseActivityInternal(activity, data)
                    );
                }
            }
        };
    }

    @NonNull
    public synchronized ActivityLifecycleManager.WatchingStatus startWatchingIfNotYet() {
        if (!sessionAutotrackingStarted) {
            DebugLogger.INSTANCE.info(TAG, "Start watching");
            activityLifecycleManager.registerListener(
                onResumedCallback,
                ActivityLifecycleManager.ActivityEvent.RESUMED
            );
            activityLifecycleManager.registerListener(onPausedCallback, ActivityLifecycleManager.ActivityEvent.PAUSED);
            sessionAutotrackingStarted = true;
        } else {
            DebugLogger.INSTANCE.info(TAG, "Ignore start watching if not yet as already has been started");
        }
        return activityLifecycleManager.getWatchingStatus();
    }

    public synchronized void stopWatchingIfHasAlreadyBeenStarted() {
        if (sessionAutotrackingStarted) {
            DebugLogger.INSTANCE.info(TAG, "Stop watching");
            activityLifecycleManager.unregisterListener(
                onResumedCallback,
                ActivityLifecycleManager.ActivityEvent.RESUMED
            );
            activityLifecycleManager.unregisterListener(
                onPausedCallback,
                ActivityLifecycleManager.ActivityEvent.PAUSED
            );
            sessionAutotrackingStarted = false;
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                "Ignore stop watching if not yet as already has been stopped or not started"
            );
        }
    }

    @ApiProxyThread
    public void resumeActivityManually(@Nullable Activity activity, @NonNull IMainReporter reporter) {
        DebugLogger.INSTANCE.info(TAG, "resume %s manually", activity);
        if (activity != null) {
            activityAppearedListener.onActivityAppeared(activity);
        }
        resumeActivityInternal(activity, reporter);
    }

    @ApiProxyThread
    public void pauseActivityManually(@Nullable Activity activity, @NonNull IMainReporter reporter) {
        DebugLogger.INSTANCE.info(TAG, "pause %s manually", activity);
        if (activity != null) {
            activityAppearedListener.onActivityAppeared(activity);
        }
        pauseActivityInternal(activity, reporter);
    }

    public void setReporter(@NonNull MainReporter reporter) {
        DebugLogger.INSTANCE.info(TAG, "set reporter");
        conditionalExecutor.setResource(reporter);
    }

    @ApiProxyThread
    private void resumeActivityInternal(@Nullable Activity activity, @NonNull IMainReporter reporter) {
        if (activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED)) {
            DebugLogger.INSTANCE.info(TAG, "State changed. Proxying resume to reporter");
            reporter.resumeSession(activity);
        }
    }

    @ApiProxyThread
    private void pauseActivityInternal(@Nullable Activity activity, @NonNull IMainReporter reporter) {
        if (activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED)) {
            DebugLogger.INSTANCE.info(TAG, "State changed. Proxying pause to reporter");
            reporter.pauseSession(activity);
        }
    }
}
