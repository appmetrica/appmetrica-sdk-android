package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import javax.net.ssl.SSLSocketFactory;

public interface UnderlyingNetworkTask {

    void onTaskAdded();

    boolean onCreateTask();

    void onPerformRequest();

    boolean onRequestComplete();

    void onPostRequestComplete(boolean success);

    void onRequestError(@Nullable Throwable error);

    void onShouldNotExecute();

    void onTaskFinished();

    void onSuccessfulTaskFinished();

    void onUnsuccessfulTaskFinished();

    void onTaskRemoved();

    @NonNull
    String description();

    @Nullable
    SSLSocketFactory getSslSocketFactory();

    @Nullable
    RetryPolicyConfig getRetryPolicyConfig();

    @NonNull
    RequestDataHolder getRequestDataHolder();

    @NonNull
    ResponseDataHolder getResponseDataHolder();

    @NonNull
    FullUrlFormer<?> getFullUrlFormer();
}
