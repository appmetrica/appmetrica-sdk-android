package io.appmetrica.analytics.coreutils.internal.services;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;

public class ActivationBarrier {

    public interface IActivationBarrierCallback {

        void onWaitFinished();
    }

    public static class ActivationBarrierHelper {

        private boolean mActivated;
        @NonNull private final IActivationBarrierCallback mActivationCallback;
        @NonNull private final ActivationBarrier mActivationBarrier;

        public ActivationBarrierHelper(@NonNull final Runnable runnable) {
            this(runnable, UtilityServiceLocator.getInstance().getActivationBarrier());
        }

        @VisibleForTesting
        ActivationBarrierHelper(@NonNull final Runnable runnable, @NonNull ActivationBarrier activationBarrier) {
            mActivated = false;
            mActivationCallback = new IActivationBarrierCallback() {
                @Override
                public void onWaitFinished() {
                    YLogger.info(TAG, "Wait finished. Execute callback");
                    mActivated = true;
                    runnable.run();
                }
            };
            mActivationBarrier = activationBarrier;
        }

        public void subscribeIfNeeded(final long delay, @NonNull ICommonExecutor executor) {
            if (mActivated == false) {
                YLogger.info(TAG, "Has not been activated yet. Subscribe with delay %d.", delay);
                mActivationBarrier.subscribe(
                        delay,
                        executor,
                        mActivationCallback
                );
            } else {
                YLogger.info(TAG, "Already has been activated. Execute now.");
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mActivationCallback.onWaitFinished();
                    }
                });
            }
        }

    }

    private static final String TAG = "[ActivationBarrier]";

    private long mStartTime;

    @NonNull
    private final SystemTimeProvider mTimeProvider;

    public ActivationBarrier() {
        this(new SystemTimeProvider());
    }

    @VisibleForTesting
    ActivationBarrier(@NonNull SystemTimeProvider timeProvider) {
        mTimeProvider = timeProvider;
    }

    public void activate() {
        mStartTime = mTimeProvider.currentTimeMillis();
    }

    public void subscribe(final long delta,
                          @NonNull ICommonExecutor executor,
                          @NonNull final IActivationBarrierCallback callback) {
        final long timeToWait = Math.max(delta - (mTimeProvider.currentTimeMillis() - mStartTime), 0);
        executor.executeDelayed(new Runnable() {
            @Override
            public void run() {
                YLogger.d("%sActivation finished", TAG);
                callback.onWaitFinished();
            }
        }, timeToWait);
    }

}
