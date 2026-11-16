package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.io.SslConfiguration;
import io.appmetrica.analytics.networktasks.internal.attempt.AttemptFailureReason;

public interface UnderlyingNetworkTask {

    void onTaskAdded();

    boolean onCreateTask();

    void onPerformRequest();

    boolean onRequestComplete();

    void onPostRequestComplete(boolean success);

    @NonNull
    default AttemptFailureReason getFailureReason() {
        return AttemptFailureReason.UNCLASSIFIED;
    }

    void onRequestError(@Nullable Throwable error);

    void onShouldNotExecute();

    void onTaskFinished();

    void onSuccessfulTaskFinished();

    void onUnsuccessfulTaskFinished();

    void onTaskRemoved();

    @NonNull
    String description();

    @NonNull
    SslConfiguration getSslConfiguration();

    @Nullable
    RetryPolicyConfig getRetryPolicyConfig();

    @NonNull
    RequestDataHolder getRequestDataHolder();

    @NonNull
    ResponseDataHolder getResponseDataHolder();

    @NonNull
    FullUrlFormer<?> getFullUrlFormer();
}
