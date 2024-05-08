package io.appmetrica.analytics.coreutils.internal.services;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrier;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.ActivationBarrierCallback;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;

public class WaitForActivationDelayBarrier implements ActivationBarrier {

    public static class ActivationBarrierHelper {

        private boolean activated;
        @NonNull
        private final ActivationBarrierCallback activationCallback;
        @NonNull
        private final WaitForActivationDelayBarrier activationBarrier;

        public ActivationBarrierHelper(@NonNull final Runnable runnable,
                                       @NonNull WaitForActivationDelayBarrier activationBarrier) {
            activated = false;
            activationCallback = new ActivationBarrierCallback() {
                @Override
                public void onWaitFinished() {
                    YLogger.info(TAG, "Wait finished. Execute callback");
                    activated = true;
                    runnable.run();
                }
            };
            this.activationBarrier = activationBarrier;
        }

        public void subscribeIfNeeded(final long delay, @NonNull ICommonExecutor executor) {
            if (!activated) {
                YLogger.info(TAG, "Has not been activated yet. Subscribe with delay %d.", delay);
                activationBarrier.subscribe(
                        delay,
                        executor,
                    activationCallback
                );
            } else {
                YLogger.info(TAG, "Already has been activated. Execute now.");
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        activationCallback.onWaitFinished();
                    }
                });
            }
        }

    }

    private static final String TAG = "[ActivationBarrier]";

    private long mStartTime;

    @NonNull
    private final SystemTimeProvider mTimeProvider;

    public WaitForActivationDelayBarrier() {
        this(new SystemTimeProvider());
    }

    @VisibleForTesting
    WaitForActivationDelayBarrier(@NonNull SystemTimeProvider timeProvider) {
        mTimeProvider = timeProvider;
    }

    public void activate() {
        mStartTime = mTimeProvider.currentTimeMillis();
    }

    @Override
    public void subscribe(final long delta,
                          @NonNull ICommonExecutor executor,
                          @NonNull final ActivationBarrierCallback callback) {
        final long timeToWait = Math.max(delta - (mTimeProvider.currentTimeMillis() - mStartTime), 0);
        executor.executeDelayed(new Runnable() {
            @Override
            public void run() {
                YLogger.debug(TAG, "Activation finished");
                callback.onWaitFinished();
            }
        }, timeToWait);
    }

}
