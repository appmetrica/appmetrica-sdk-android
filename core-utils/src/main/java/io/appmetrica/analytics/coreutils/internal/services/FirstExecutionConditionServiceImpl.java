package io.appmetrica.analytics.coreutils.internal.services;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.FirstExecutionConditionService;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.FirstExecutionDelayedTask;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FirstExecutionConditionServiceImpl implements FirstExecutionConditionService {

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
        private long delay;
        public final String tag;

        @NonNull
        private final FirstExecutionDelayChecker mFirstExecutionDelayChecker;

        FirstExecutionConditionChecker(@Nullable UtilityServiceConfiguration configuration,
                                       @NonNull String tag) {
            this(configuration, new FirstExecutionDelayChecker(), tag);
        }

        public FirstExecutionConditionChecker(@Nullable UtilityServiceConfiguration configuration,
                                              @NonNull FirstExecutionDelayChecker firstExecutionDelayChecker,
                                              @NonNull String tag) {
            mFirstExecutionDelayChecker = firstExecutionDelayChecker;
            firstExecutionAlreadyAllowed = false;
            initialUpdateConfigTime = configuration == null ? 0 : configuration.getInitialConfigTime();
            lastUpdateConfigTime = configuration == null ? 0 : configuration.getLastUpdateConfigTime();
            delay = Long.MAX_VALUE;
            this.tag = tag;
            YLogger.info(TAG, "%s init with configuration: %s", tag, configuration);
        }

        boolean shouldExecute() {
            YLogger.info(TAG, "%s shouldExecute: mHasFirstExecutionOccurred: %b, mFirstStartupServerTime: %d, " +
                    "mLastStartupServerTime: %d, mDelay: %d", tag, firstExecutionAlreadyAllowed,
                initialUpdateConfigTime, lastUpdateConfigTime, delay);
            if (firstExecutionAlreadyAllowed) {
                return true;
            }
            return mFirstExecutionDelayChecker.delaySinceFirstStartupWasPassed(
                initialUpdateConfigTime,
                lastUpdateConfigTime,
                delay
            );
        }

        void setDelaySeconds(final long delaySeconds) {
            delay = TimeUnit.SECONDS.toMillis(delaySeconds);
            YLogger.info(TAG, "%s update delay with %d", tag, delay);
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

    public static class FirstExecutionHandler implements FirstExecutionDelayedTask {

        @NonNull
        private final FirstExecutionConditionChecker firstExecutionConditionChecker;
        @NonNull
        private final WaitForActivationDelayBarrier.ActivationBarrierHelper activationBarrierHelper;
        @NonNull
        private final ICommonExecutor executor;

        private FirstExecutionHandler(
            @NonNull ICommonExecutor executor,
            @NonNull WaitForActivationDelayBarrier.ActivationBarrierHelper activationBarrierHelper,
            @NonNull FirstExecutionConditionChecker firstExecutionConditionChecker
        ) {
            this.activationBarrierHelper = activationBarrierHelper;
            this.firstExecutionConditionChecker = firstExecutionConditionChecker;
            this.executor = executor;
        }

        public void updateConfig(@NonNull UtilityServiceConfiguration configuration) {
            firstExecutionConditionChecker.updateConfig(configuration);
        }

        @Override
        public void setInitialDelaySeconds(long delaySeconds) {
            firstExecutionConditionChecker.setDelaySeconds(delaySeconds);
        }

        @Override
        public boolean tryExecute(final long launchDelaySeconds) {
            if (firstExecutionConditionChecker.shouldExecute()) {
                YLogger.info(TAG, "%s try execute with delay = %d. First execution conditions are met",
                    firstExecutionConditionChecker.tag, 0);
                activationBarrierHelper.subscribeIfNeeded(TimeUnit.SECONDS.toMillis(launchDelaySeconds), executor);
                firstExecutionConditionChecker.setFirstExecutionAlreadyAllowed();
                return true;
            } else {
                YLogger.info(TAG, "%s try execute with delay = %d. First execution conditions were not met",
                    firstExecutionConditionChecker.tag, 0);
                return false;
            }
        }

        public boolean canExecute() {
            boolean result = firstExecutionConditionChecker.shouldExecute();
            if (result) {
                firstExecutionConditionChecker.setFirstExecutionAlreadyAllowed();
            }
            return result;
        }
    }

    @NonNull
    private final List<FirstExecutionHandler> mFirstExecutionHandlers = new ArrayList<>();
    @Nullable
    private UtilityServiceConfiguration configuration;

    @NonNull
    UtilityServiceProvider utilityServiceProvider;

    public FirstExecutionConditionServiceImpl(@NonNull UtilityServiceProvider utilityServiceProvider) {
        this.utilityServiceProvider = utilityServiceProvider;
    }

    @Override
    @NonNull
    public synchronized FirstExecutionDelayedTask createDelayedTask(@NonNull String tag,
                                                                    @NonNull ICommonExecutor executor,
                                                                    @NonNull Runnable runnable) {
        return createFirstExecutionHandler(
            executor,
            new WaitForActivationDelayBarrier.ActivationBarrierHelper(
                runnable,
                utilityServiceProvider.getActivationBarrier()
            ),
            new FirstExecutionConditionChecker(configuration, tag)
        );
    }

    @VisibleForTesting
    @NonNull
    synchronized FirstExecutionHandler createFirstExecutionHandler(
        @NonNull ICommonExecutor executor,
        @NonNull WaitForActivationDelayBarrier.ActivationBarrierHelper actHelper,
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
