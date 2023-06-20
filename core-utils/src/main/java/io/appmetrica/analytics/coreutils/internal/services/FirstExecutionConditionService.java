package io.appmetrica.analytics.coreutils.internal.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FirstExecutionConditionService {

    private static final String TAG = "[FirstExecutionConditionChecker]";

    public static class FirstExecutionDelayChecker {

        public boolean delaySinceFirstStartupWasPassed(long initialConfigTime,
                                                       long lastUpateConfigTime,
                                                       long delay) {

            return (lastUpateConfigTime - initialConfigTime) >= delay;
        }

    }

    public static class FirstExecutionConditionChecker {

        private boolean firstExecutionAlreadyAllowed;
        private long lastUpdateConfigTime;
        private long initialUpdateConfigTime;
        private long mDelay;
        public final String tag;

        @NonNull
        private final FirstExecutionDelayChecker mFirstExecutionDelayChecker;

        FirstExecutionConditionChecker(@Nullable UtilityServiceConfiguration configuration,
                                       @NonNull String tag) {
            this(configuration, new FirstExecutionDelayChecker(), tag);
        }

        public FirstExecutionConditionChecker(@Nullable UtilityServiceConfiguration configuration,
                                              @NonNull  FirstExecutionDelayChecker firstExecutionDelayChecker,
                                              @NonNull String tag) {
            mFirstExecutionDelayChecker = firstExecutionDelayChecker;
            firstExecutionAlreadyAllowed = false;
            initialUpdateConfigTime = configuration == null ? 0 : configuration.getInitialConfigTime();
            lastUpdateConfigTime = configuration == null ? 0 : configuration.getLastUpdateConfigTime();
            mDelay = Long.MAX_VALUE;
            this.tag = tag;
            YLogger.info(TAG, "%s init with configuration: %s", tag, configuration);
        }

        boolean shouldExecute() {
            YLogger.info(TAG, "%s shouldExecute: mHasFirstExecutionOccurred: %b, mFirstStartupServerTime: %d, " +
                    "mLastStartupServerTime: %d, mDelay: %d", tag, firstExecutionAlreadyAllowed,
                    initialUpdateConfigTime, lastUpdateConfigTime, mDelay);
            if (firstExecutionAlreadyAllowed) {
                return true;
            }
            return mFirstExecutionDelayChecker.delaySinceFirstStartupWasPassed(
                    initialUpdateConfigTime,
                    lastUpdateConfigTime,
                    mDelay
            );
        }

        void setDelay(final long delay, @NonNull TimeUnit timeUnit) {
            mDelay = timeUnit.toMillis(delay);
            YLogger.info(TAG, "%s update delay with %d", tag, mDelay);
        }

        void setFirstExecutionAlreadyAllowed() {
            firstExecutionAlreadyAllowed = true;
        }

        void updateConfig(@NonNull UtilityServiceConfiguration configuration) {
            initialUpdateConfigTime = configuration.getInitialConfigTime();
            lastUpdateConfigTime = configuration.getLastUpdateConfigTime();
            YLogger.info(TAG, "%s Update times from startup. mLastStartupServerTime: %d, mFirstStartupServerTime: %d",
                    tag, lastUpdateConfigTime, initialUpdateConfigTime);
        }
    }

    public static class FirstExecutionHandler {

        @NonNull
        private FirstExecutionConditionChecker mFirstExecutionConditionChecker;
        @NonNull
        private final ActivationBarrier.ActivationBarrierHelper mActivationBarrierHelper;
        @NonNull
        private final ICommonExecutor mExecutor;

        private FirstExecutionHandler(@NonNull ICommonExecutor executor,
                                      @NonNull ActivationBarrier.ActivationBarrierHelper activationBarrierHelper,
                                      @NonNull FirstExecutionConditionChecker firstExecutionConditionChecker) {
            mActivationBarrierHelper = activationBarrierHelper;
            mFirstExecutionConditionChecker = firstExecutionConditionChecker;
            mExecutor = executor;
        }

        public void updateConfig(@NonNull UtilityServiceConfiguration configuration) {
            mFirstExecutionConditionChecker.updateConfig(configuration);
        }

        public void setDelaySeconds(final long delay) {
            mFirstExecutionConditionChecker.setDelay(delay, TimeUnit.SECONDS);
        }

        public boolean tryExecute(final int launchDelaySeconds) {
            if (mFirstExecutionConditionChecker.shouldExecute()) {
                YLogger.info(TAG, "%s try execute with delay = %d. First execution conditions are met",
                        mFirstExecutionConditionChecker.tag, 0);
                mActivationBarrierHelper.subscribeIfNeeded(TimeUnit.SECONDS.toMillis(launchDelaySeconds), mExecutor);
                mFirstExecutionConditionChecker.setFirstExecutionAlreadyAllowed();
                return true;
            } else {
                YLogger.info(TAG, "%s try execute with delay = %d. First execution conditions were not met",
                        mFirstExecutionConditionChecker.tag, 0);
                return false;
            }
        }

        public boolean canExecute() {
            boolean result = mFirstExecutionConditionChecker.shouldExecute();
            if (result) {
                mFirstExecutionConditionChecker.setFirstExecutionAlreadyAllowed();
            }
            return result;
        }
    }

    @NonNull
    private final List<FirstExecutionHandler> mFirstExecutionHandlers;
    @Nullable
    private UtilityServiceConfiguration configuration;

    public FirstExecutionConditionService() {
        mFirstExecutionHandlers = new ArrayList<FirstExecutionHandler>();
    }

    public synchronized FirstExecutionHandler createFirstExecutionHandler(@NonNull Runnable runnable,
                                                                          @NonNull ICommonExecutor executor,
                                                                          @NonNull String tag) {
        return createFirstExecutionHandler(
                executor,
                new ActivationBarrier.ActivationBarrierHelper(runnable),
                new FirstExecutionConditionChecker(configuration, tag)
        );
    }

    @VisibleForTesting
    synchronized FirstExecutionHandler createFirstExecutionHandler(
            @NonNull ICommonExecutor executor,
            @NonNull ActivationBarrier.ActivationBarrierHelper actHelper,
            @NonNull FirstExecutionConditionChecker checker
    ) {
        FirstExecutionHandler firstExecutionHandler = new FirstExecutionHandler(executor, actHelper, checker);
        mFirstExecutionHandlers.add(firstExecutionHandler);
        YLogger.info(
            TAG,
            "Create new first execution handler. Initial configuration = %s. Total handlers count = %d",
            configuration,
            mFirstExecutionHandlers.size()
        );
        return firstExecutionHandler;
    }

    public void updateConfig(@NonNull UtilityServiceConfiguration configuration) {
        final List<FirstExecutionHandler> firstExecutionHandlersCopy;
        synchronized (this) {
            YLogger.info(
                TAG,
                "update configuration: %s for %d handlers",
                configuration,
                mFirstExecutionHandlers.size()
            );
            this.configuration = configuration;
            firstExecutionHandlersCopy = new ArrayList<FirstExecutionHandler>(mFirstExecutionHandlers);
        }
        for (FirstExecutionHandler firstExecutionHandler : firstExecutionHandlersCopy) {
            firstExecutionHandler.updateConfig(configuration);
        }
    }
}
