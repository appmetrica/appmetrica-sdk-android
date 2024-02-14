package io.appmetrica.analytics.impl;

import android.app.Activity;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.utils.ApiProxyThread;
import io.appmetrica.analytics.impl.utils.ConditionalExecutor;
import io.appmetrica.analytics.logger.internal.YLogger;

public class SessionsTrackingManager {

    private static final String TAG = "[SessionsAutoTrackingManager]";

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

    public SessionsTrackingManager(@NonNull ActivityLifecycleManager activityLifecycleManager,
                                   @NonNull ICommonExecutor apiProxyExecutor,
                                   @NonNull ActivityAppearedListener activityAppearedListener) {
        this(
                activityLifecycleManager,
                activityAppearedListener,
                new ConditionalExecutor<MainReporter>(apiProxyExecutor),
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
        onResumedCallback = new ActivityLifecycleManager.Listener() {
            @Override
            @MainThread
            public void onEvent(@NonNull final Activity activity,
                                @NonNull ActivityLifecycleManager.ActivityEvent event) {
                YLogger.info(TAG, "on resume callback");
                SessionsTrackingManager.this.conditionalExecutor.addCommand(new NonNullConsumer<MainReporter>() {
                    @ApiProxyThread
                    @Override
                    public void consume(@NonNull MainReporter data) {
                        resumeActivityInternal(activity, data);
                    }
                });
            }
        };
        onPausedCallback = new ActivityLifecycleManager.Listener() {
            @Override
            @MainThread
            public void onEvent(@NonNull final Activity activity,
                                @NonNull ActivityLifecycleManager.ActivityEvent event) {
                YLogger.info(TAG, "on pause callback");
                SessionsTrackingManager.this.conditionalExecutor.addCommand(new NonNullConsumer<MainReporter>() {
                    @ApiProxyThread
                    @Override
                    public void consume(@NonNull MainReporter data) {
                        pauseActivityInternal(activity, data);
                    }
                });
            }
        };
    }

    @NonNull
    public ActivityLifecycleManager.WatchingStatus startWatching(boolean auto) {
        activityLifecycleManager.registerListener(onResumedCallback, ActivityLifecycleManager.ActivityEvent.RESUMED);
        activityLifecycleManager.registerListener(onPausedCallback, ActivityLifecycleManager.ActivityEvent.PAUSED);
        return activityLifecycleManager.getWatchingStatus();
    }

    @ApiProxyThread
    public void resumeActivityManually(@Nullable Activity activity, @NonNull IMainReporter reporter) {
        YLogger.info(TAG, "resume %s manually", activity);
        if (activity != null) {
            activityAppearedListener.onActivityAppeared(activity);
        }
        resumeActivityInternal(activity, reporter);
    }

    @ApiProxyThread
    public void pauseActivityManually(@Nullable Activity activity, @NonNull IMainReporter reporter) {
        YLogger.info(TAG, "pause %s manually", activity);
        if (activity != null) {
            activityAppearedListener.onActivityAppeared(activity);
        }
        pauseActivityInternal(activity, reporter);
    }

    public void setReporter(@NonNull MainReporter reporter) {
        YLogger.info(TAG, "set reporter");
        conditionalExecutor.setResource(reporter);
    }

    @ApiProxyThread
    private void resumeActivityInternal(@Nullable Activity activity, @NonNull IMainReporter reporter) {
        if (activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED)) {
            YLogger.info(TAG, "State changed. Proxying resume to reporter");
            reporter.resumeSession(activity);
        }
    }

    @ApiProxyThread
    private void pauseActivityInternal(@Nullable Activity activity, @NonNull IMainReporter reporter) {
        if (activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED)) {
            YLogger.info(TAG, "State changed. Proxying pause to reporter");
            reporter.pauseSession(activity);
        }
    }
}
