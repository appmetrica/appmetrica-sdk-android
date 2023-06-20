package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;

public class ExponentialBackoffDataHolder {

    private static final String TAG = "[ExponentialBackoffDataHolder]";

    @NonNull
    private final TimePassedChecker timePassedChecker;
    @NonNull
    private final TimeProvider timeProvider;
    @NonNull
    private final HostRetryInfoProvider retryInfoProvider;
    private long lastAttemptTimestampSeconds;
    private int nextAttemptNumber;

    public ExponentialBackoffDataHolder(@NonNull HostRetryInfoProvider retryInfoProvider) {
        this(retryInfoProvider, new SystemTimeProvider(), new TimePassedChecker());
    }

    @VisibleForTesting
    ExponentialBackoffDataHolder(@NonNull HostRetryInfoProvider retryInfoProvider,
                                 @NonNull TimeProvider timeProvider,
                                 @NonNull TimePassedChecker timePassedChecker) {
        this.retryInfoProvider = retryInfoProvider;
        this.timeProvider = timeProvider;
        this.timePassedChecker = timePassedChecker;
        lastAttemptTimestampSeconds = retryInfoProvider.getLastAttemptTimeSeconds();
        nextAttemptNumber = retryInfoProvider.getNextSendAttemptNumber();
        YLogger.info(TAG, "Created object with lastAttemptTimestampSeconds = %d, nextAttemptNumber = %d",
                lastAttemptTimestampSeconds, nextAttemptNumber);
    }

    public void reset() {
        YLogger.info(TAG, "reset");
        nextAttemptNumber = 1;
        lastAttemptTimestampSeconds = 0;
        retryInfoProvider.saveNextSendAttemptNumber(nextAttemptNumber);
        retryInfoProvider.saveLastAttemptTimeSeconds(lastAttemptTimestampSeconds);
    }

    public void updateLastAttemptInfo() {
        lastAttemptTimestampSeconds = timeProvider.currentTimeSeconds();
        nextAttemptNumber++;
        YLogger.info(TAG, "Updated info: lastAttemptTimestampSeconds = %d, nextAttemptNumber = %d",
                lastAttemptTimestampSeconds, nextAttemptNumber);
        retryInfoProvider.saveLastAttemptTimeSeconds(lastAttemptTimestampSeconds);
        retryInfoProvider.saveNextSendAttemptNumber(nextAttemptNumber);
    }

    public boolean wasLastAttemptLongAgoEnough(@Nullable RetryPolicyConfig retryPolicyConfig) {
        if (retryPolicyConfig == null || lastAttemptTimestampSeconds == 0L) {
            YLogger.info(TAG,
                    "Last attempt was long ago enough: retryPolicyConfig = %s, " +
                            "lastAttemptTimestampSeconds = %d",
                    retryPolicyConfig,
                    lastAttemptTimestampSeconds
            );
            return true;
        } else {
            boolean result = timePassedChecker.didTimePassSeconds(
                    lastAttemptTimestampSeconds,
                    getNextSeconds(retryPolicyConfig),
                    "last send attempt"
            );
            YLogger.info(TAG, "wasLastAttemptLongAgoEnough? %b", result);
            return result;
        }
    }

    private int getNextSeconds(@NonNull RetryPolicyConfig retryPolicyConfig) {
        int nextTimeoutSeconds = retryPolicyConfig.exponentialMultiplier * ((1 << nextAttemptNumber - 1) - 1);
        if (nextTimeoutSeconds <= retryPolicyConfig.maxIntervalSeconds) {
            return nextTimeoutSeconds;
        } else {
            return retryPolicyConfig.maxIntervalSeconds;
        }
    }
}
