package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateObserver;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateProvider;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ApplicationStateProviderImpl implements ServiceLifecycleObserver, ApplicationStateProvider {

    private static final String TAG = "[ApplicationStateProvider]";

    private final Set<Integer> mVisibleProcessesPids = new HashSet<>();
    private final Set<Integer> mPausedProcessesPids = new HashSet<>();
    @NonNull
    private volatile ApplicationState mCurrentState = ApplicationState.UNKNOWN;

    private final Set<ApplicationStateObserver> mObservers = new CopyOnWriteArraySet<>();

    @Override
    public void onCreate() {
        updateApplicationState();
        DebugLogger.INSTANCE.info(TAG, "Init finished. Inital application state = %s", mCurrentState.name());
    }

    @Override
    public void onDestroy() {
        if (mCurrentState == ApplicationState.VISIBLE) {
            mCurrentState = ApplicationState.BACKGROUND;
        }
        DebugLogger.INSTANCE.info(TAG, "Destroy. Actual application state = %s", mCurrentState.name());
    }

    @Override
    @NonNull
    public ApplicationState registerStickyObserver(@Nullable ApplicationStateObserver observer) {
        if (observer != null) {
            mObservers.add(observer);
            DebugLogger.INSTANCE.info(
                TAG,
                "Register observer(%s). Actual observers count = %d",
                observer,
                mObservers.size()
            );
        }
        return mCurrentState;
    }

    @Override
    @NonNull
    public ApplicationState getCurrentState() {
        return mCurrentState;
    }

    public void resumeUserSessionForPid(int pid) {
        mVisibleProcessesPids.add(pid);
        mPausedProcessesPids.remove(pid);
        DebugLogger.INSTANCE.info(
            TAG,
            "ResumeUserSessionForPid = %d. Visible processes count = %d; paused processes count = %d",
            pid,
            mVisibleProcessesPids.size(),
            mPausedProcessesPids.size()
        );
        updateApplicationState();
    }

    public void pauseUserSessionForPid(int pid) {
        mPausedProcessesPids.add(pid);
        mVisibleProcessesPids.remove(pid);
        DebugLogger.INSTANCE.info(
            TAG,
            "PauseUserSessionForPid = %d. Visible processes count = %d; paused processes count = %d",
            pid,
            mVisibleProcessesPids.size(),
            mPausedProcessesPids.size()
        );
        updateApplicationState();
    }

    public void notifyProcessDisconnected(int pid) {
        mVisibleProcessesPids.remove(pid);
        DebugLogger.INSTANCE.info(
            TAG,
            "NotifyProcessDisconnected for pid = %d. Visible processes count = %d; paused processes count = %d ",
            pid,
            mVisibleProcessesPids.size(),
            mPausedProcessesPids.size()
        );
        updateApplicationState();
    }

    private void updateApplicationState() {
        ApplicationState incomingState = calculateApplicationState();
        if (mCurrentState != incomingState) {
            DebugLogger.INSTANCE.info(
                TAG,
                "Change application state: %s -> %s; Actual details: visible processes pids = %d; " +
                    "paused processes pids = %d",
                mCurrentState.name(),
                incomingState.name(),
                mVisibleProcessesPids.size(),
                mPausedProcessesPids.size()
            );
            mCurrentState = incomingState;
            notifyApplicationStateChanged();
        }
    }

    @NonNull
    private ApplicationState calculateApplicationState() {
        ApplicationState applicationState = ApplicationState.UNKNOWN;
        if (mVisibleProcessesPids.isEmpty() == false) {
            applicationState = ApplicationState.VISIBLE;
        }  else if (mPausedProcessesPids.isEmpty() == false) {
            applicationState = ApplicationState.BACKGROUND;
        }
        return applicationState;
    }

    private void notifyApplicationStateChanged() {
        DebugLogger.INSTANCE.info(
            TAG,
            "NotifyApplicationStateChanged. Total observers = %d",
            mObservers.size()
        );
        for (ApplicationStateObserver observer : mObservers) {
            observer.onApplicationStateChanged(mCurrentState);
        }
    }
}
