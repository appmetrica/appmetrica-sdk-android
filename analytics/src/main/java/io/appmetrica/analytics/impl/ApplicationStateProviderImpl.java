package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateObserver;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateProvider;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
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
        YLogger.d("%sInit finished. Inital application state = %s", TAG, mCurrentState.name());
    }

    @Override
    public void onDestroy() {
        if (mCurrentState == ApplicationState.VISIBLE) {
            mCurrentState = ApplicationState.BACKGROUND;
        }
        YLogger.d("%sDestroy. Actual application state = %s", TAG, mCurrentState.name());
    }

    @Override
    @NonNull
    public ApplicationState registerStickyObserver(@Nullable ApplicationStateObserver observer) {
        if (observer != null) {
            mObservers.add(observer);
            YLogger.d("%sRegister observer(%s). Actual observers count = %d", TAG, observer, mObservers.size());
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
        YLogger.d(
                "%sResumeUserSessionForPid = %d. Visible processes count = %d; paused processes count = %d",
                TAG,
                pid,
                mVisibleProcessesPids.size(),
                mPausedProcessesPids.size()
        );
        updateApplicationState();
    }

    public void pauseUserSessionForPid(int pid) {
        mPausedProcessesPids.add(pid);
        mVisibleProcessesPids.remove(pid);
        YLogger.d(
                "%sPauseUserSessionForPid = %d. Visible processes count = %d; paused processes count = %d",
                TAG,
                pid,
                mVisibleProcessesPids.size(),
                mPausedProcessesPids.size()
        );
        updateApplicationState();
    }

    public void notifyProcessDisconnected(int pid) {
        mVisibleProcessesPids.remove(pid);
        YLogger.d(
                "%sNotifyProcessDisconnected for pid = %d. Visible processes count = %d; paused processes count = %d ",
                TAG,
                pid,
                mVisibleProcessesPids.size(),
                mPausedProcessesPids.size()
        );
        updateApplicationState();
    }

    private void updateApplicationState() {
        ApplicationState incomingState = calculateApplicationState();
        if (mCurrentState != incomingState) {
            YLogger.d(
                    "%sChange application state: %s -> %s; Actual details: visible processes pids = %d; " +
                            "paused processes pids = %d",
                    TAG,
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
        YLogger.d("%sNotifyApplicationStateChanged. Total observers = %d", TAG, mObservers.size());
        for (ApplicationStateObserver observer : mObservers) {
            observer.onApplicationStateChanged(mCurrentState);
        }
    }
}
