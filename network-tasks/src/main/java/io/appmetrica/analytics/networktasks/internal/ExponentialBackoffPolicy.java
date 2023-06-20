package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.Nullable;

public interface ExponentialBackoffPolicy {

    void onHostAttemptFinished(boolean success);

    void onAllHostsAttemptsFinished(boolean success);

    boolean canBeExecuted(@Nullable RetryPolicyConfig retryPolicyConfig);
}
