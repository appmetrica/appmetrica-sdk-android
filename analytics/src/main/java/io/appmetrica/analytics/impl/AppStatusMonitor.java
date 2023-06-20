package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import java.util.HashSet;
import java.util.Set;

public class AppStatusMonitor {

    public interface Observer {

        void onResume();

        void onPause();

    }

    @NonNull
    private final ICommonExecutor mExecutor;

    private final long mDefaultSessionTimeout;

    private final Set<ObserverWrapper> mObservers = new HashSet<ObserverWrapper>();

    private boolean mPaused = true;

    public AppStatusMonitor(long defaultSessionTimeout) {
        this(
                defaultSessionTimeout,
                ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor()
        );
    }

    AppStatusMonitor(long defaultSessionTimeout, @NonNull ICommonExecutor executor) {
        mExecutor = executor;
        mDefaultSessionTimeout = defaultSessionTimeout;
    }

    public synchronized void resume() {
        mPaused = false;
        for (ObserverWrapper observer : mObservers) {
            observer.notifyOnResume();
        }
    }

    public synchronized void pause() {
        mPaused = true;
        for (ObserverWrapper observer : mObservers) {
            observer.notifyOnPause();
        }
    }

    public synchronized void registerObserver(@NonNull Observer observer) {
        registerObserver(observer, false);
    }

    public synchronized void registerObserver(@NonNull Observer observer, boolean sticky) {
        registerObserver(observer, mDefaultSessionTimeout, sticky);
    }

    public synchronized void registerObserver(@NonNull Observer observer, long timeout) {
        registerObserver(observer, timeout, false);
    }

    public synchronized void registerObserver(@NonNull Observer observer, long timeout, boolean sticky) {
        ObserverWrapper observerWrapper = new ObserverWrapper(observer, mExecutor, timeout);
        mObservers.add(observerWrapper);
        // ObserverWrapper is paused by default and do not need to call notifyOnPause
        if (sticky && mPaused == false) {
            observerWrapper.notifyOnResume();
        }
    }

    public synchronized void unregisterObserver(@NonNull Observer observer) {
        ObserverWrapper observerToRemove = null;
        for (ObserverWrapper observerWrapper : mObservers) {
            if (observerWrapper.observer.equals(observer)) {
                observerToRemove = observerWrapper;
                break;
            }
        }
        mObservers.remove(observerToRemove);
    }

    private class ObserverWrapper {
        @NonNull
        final ICommonExecutor mExecutor;
        @NonNull
        final Observer observer;
        private final long mTimeout;

        private boolean mPaused = true;

        private final Runnable mPauseRunnable = new Runnable() {
            @Override
            public void run() {
                observer.onPause();
            }
        };

        ObserverWrapper(@NonNull Observer observer, @NonNull ICommonExecutor executor, long timeout) {
            this.observer = observer;
            mExecutor = executor;
            mTimeout = timeout;
        }

        void notifyOnResume() {
            if (mPaused) {
                mPaused = false;
                mExecutor.remove(mPauseRunnable);
                observer.onResume();
            }
        }

        void notifyOnPause() {
            if (mPaused == false) {
                mPaused = true;
                mExecutor.executeDelayed(mPauseRunnable, mTimeout);
            }
        }
    }

    @NonNull
    @VisibleForTesting
    ICommonExecutor getExecutor() {
        return mExecutor;
    }

    @VisibleForTesting
    long getDefaultSessionTimeout() {
        return mDefaultSessionTimeout;
    }
}
