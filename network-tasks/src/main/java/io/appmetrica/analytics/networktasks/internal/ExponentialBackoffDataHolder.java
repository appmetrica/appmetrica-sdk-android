package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class ExponentialBackoffDataHolder {

    private static final String TAG_PATTERN = "[ExponentialBackoffDataHolder-%s]";

    @NonNull
    private final TimePassedChecker timePassedChecker;
    @NonNull
    private final TimeProvider timeProvider;
    @NonNull
    private final HostRetryInfoProvider retryInfoProvider;
    private long lastAttemptTimestampSeconds;
    private int nextAttemptNumber;
    @NonNull
    private final String tag;

    public ExponentialBackoffDataHolder(@NonNull HostRetryInfoProvider retryInfoProvider, @NonNull String subtag) {
        this(retryInfoProvider, new SystemTimeProvider(), new TimePassedChecker(), subtag);
    }

    @VisibleForTesting
    ExponentialBackoffDataHolder(@NonNull HostRetryInfoProvider retryInfoProvider,
                                 @NonNull TimeProvider timeProvider,
                                 @NonNull TimePassedChecker timePassedChecker,
                                 @NonNull String subtag) {
        this.retryInfoProvider = retryInfoProvider;
        this.timeProvider = timeProvider;
        this.timePassedChecker = timePassedChecker;
        lastAttemptTimestampSeconds = retryInfoProvider.getLastAttemptTimeSeconds();
        nextAttemptNumber = retryInfoProvider.getNextSendAttemptNumber();
        tag = String.format(TAG_PATTERN, subtag);

        DebugLogger.INSTANCE.info(
            tag,
            "Created object with lastAttemptTimestampSeconds = %d, nextAttemptNumber = %d",
            lastAttemptTimestampSeconds,
            nextAttemptNumber
        );
    }

    public void reset() {
        DebugLogger.INSTANCE.info(tag, "reset");
        nextAttemptNumber = 1;
        lastAttemptTimestampSeconds = 0;
        retryInfoProvider.saveNextSendAttemptNumber(nextAttemptNumber);
        retryInfoProvider.saveLastAttemptTimeSeconds(lastAttemptTimestampSeconds);
    }

    public void updateLastAttemptInfo() {
        lastAttemptTimestampSeconds = timeProvider.currentTimeSeconds();
        nextAttemptNumber++;
        DebugLogger.INSTANCE.info(
            tag,
            "Updated info: lastAttemptTimestampSeconds = %d, nextAttemptNumber = %d",
            lastAttemptTimestampSeconds,
            nextAttemptNumber
        );
        retryInfoProvider.saveLastAttemptTimeSeconds(lastAttemptTimestampSeconds);
        retryInfoProvider.saveNextSendAttemptNumber(nextAttemptNumber);
    }

    public boolean wasLastAttemptLongAgoEnough(@Nullable RetryPolicyConfig retryPolicyConfig) {
        if (retryPolicyConfig == null || lastAttemptTimestampSeconds == 0L) {
            DebugLogger.INSTANCE.info(
                tag,
                "Last attempt was long ago enough: retryPolicyConfig = %s, lastAttemptTimestampSeconds = %d",
                retryPolicyConfig,
                lastAttemptTimestampSeconds
            );
            return true;
        } else {
            boolean result = timePassedChecker.didTimePassSeconds(
                    lastAttemptTimestampSeconds,
                    getNextSeconds(retryPolicyConfig),
                    tag
            );
            DebugLogger.INSTANCE.info(tag, "wasLastAttemptLongAgoEnough? %b", result);
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
