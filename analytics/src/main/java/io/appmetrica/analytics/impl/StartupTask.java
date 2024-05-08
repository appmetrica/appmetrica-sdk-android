package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.network.Constants;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupError;
import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.impl.startup.parsing.StartupResult;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.networktasks.internal.ConfigProvider;
import io.appmetrica.analytics.networktasks.internal.FullUrlFormer;
import io.appmetrica.analytics.networktasks.internal.NetworkResponseHandler;
import io.appmetrica.analytics.networktasks.internal.RequestDataHolder;
import io.appmetrica.analytics.networktasks.internal.ResponseDataHolder;
import io.appmetrica.analytics.networktasks.internal.RetryPolicyConfig;
import io.appmetrica.analytics.networktasks.internal.UnderlyingNetworkTask;
import javax.net.ssl.SSLSocketFactory;

public class StartupTask implements UnderlyingNetworkTask {

    private static final String TAG = "[StartupTask]";

    @NonNull
    private final StartupUnit mStartupUnit;
    @Nullable
    private StartupResult mParseResult;
    private StartupError mCause;

    @NonNull
    private final RequestDataHolder requestDataHolder;
    @NonNull
    private final ConfigProvider<StartupRequestConfig>  requestConfigProvider;
    @NonNull
    private final ResponseDataHolder responseDataHolder;
    @NonNull
    private final FullUrlFormer<StartupRequestConfig> fullUrlFormer;
    @NonNull
    private final NetworkResponseHandler<StartupResult> responseHandler;

    public StartupTask(@NonNull StartupUnit startupUnit,
                       @NonNull FullUrlFormer<StartupRequestConfig> fullUrlFormer,
                       @NonNull RequestDataHolder requestDataHolder,
                       @NonNull ResponseDataHolder responseDataHolder,
                       @NonNull ConfigProvider<StartupRequestConfig> configProvider) {
        this(
                startupUnit,
                new StartupNetworkResponseHandler(),
                fullUrlFormer,
                requestDataHolder,
                responseDataHolder,
                configProvider
        );
    }

    @VisibleForTesting
    StartupTask(@NonNull StartupUnit startupUnit,
                @NonNull StartupNetworkResponseHandler responseHandler,
                @NonNull FullUrlFormer<StartupRequestConfig> fullUrlFormer,
                @NonNull RequestDataHolder requestDataHolder,
                @NonNull ResponseDataHolder responseDataHolder,
                @NonNull ConfigProvider<StartupRequestConfig> configProvider) {
        mStartupUnit = startupUnit;
        this.responseHandler = responseHandler;
        this.requestDataHolder = requestDataHolder;
        this.responseDataHolder = responseDataHolder;
        this.requestConfigProvider = configProvider;
        this.fullUrlFormer = fullUrlFormer;
        fullUrlFormer.setHosts(configProvider.getConfig().getStartupHosts());
        YLogger.debug(TAG, "create new task with config %s", configProvider.getConfig());
    }

    @Override
    public boolean onCreateTask() {
        YLogger.info(TAG, "onCreateTask: %s", description());
        requestDataHolder.setHeader(Constants.Headers.ACCEPT_ENCODING, Constants.Config.ENCODING_ENCRYPTED);
        return mStartupUnit.isStartupRequired();
    }

    @Override
    public boolean onRequestComplete() {
        mParseResult = responseHandler.handle(responseDataHolder);
        boolean successful = mParseResult != null;
        YLogger.info(TAG, "onRequestComplete with success = %b", successful);
        return successful;
    }

    @Override
    public void onPostRequestComplete(boolean success) {
        YLogger.info(TAG, "onPostRequestComplete with success = %b", success);
        if (!success) {
            mCause = StartupError.PARSE;
        }
    }

    @Override
    public void onRequestError(@Nullable Throwable error) {
        YLogger.info(TAG, "onRequestError: %s", error);
        mCause = StartupError.NETWORK;
    }

    @Override
    public void onShouldNotExecute() {
        YLogger.info(TAG, "onShouldNotExecute");
        mCause = StartupError.NETWORK;
    }

    @Override
    public void onSuccessfulTaskFinished() {
        YLogger.info(TAG, "Successful startup task is removed.");
        if (mParseResult != null && responseDataHolder.getResponseHeaders() != null) {
            mStartupUnit.onRequestComplete(
                    mParseResult,
                    requestConfigProvider.getConfig(),
                    responseDataHolder.getResponseHeaders()
            );
        } else {
            YLogger.info(TAG, "Startup task is successful, but no parse result");
        }
    }

    @Override
    public void onUnsuccessfulTaskFinished() {
        YLogger.info(TAG, "Failed startup task is removed.");
        if (mCause == null) {
            mCause = StartupError.UNKNOWN;
        }
        mStartupUnit.onRequestError(mCause);
    }

    @NonNull
    @Override
    public String description() {
       return "Startup task for component: " + mStartupUnit.getComponentId().toString();
    }

    @Override
    @Nullable
    public RetryPolicyConfig getRetryPolicyConfig() {
        return requestConfigProvider.getConfig().getRetryPolicyConfig();
    }

    @NonNull
    @Override
    public RequestDataHolder getRequestDataHolder() {
        return requestDataHolder;
    }

    @NonNull
    @Override
    public ResponseDataHolder getResponseDataHolder() {
        return responseDataHolder;
    }

    @NonNull
    @Override
    public FullUrlFormer<?> getFullUrlFormer() {
        return fullUrlFormer;
    }

    @Nullable
    @Override
    public SSLSocketFactory getSslSocketFactory() {
        return GlobalServiceLocator.getInstance().getSslSocketFactoryProvider().getSslSocketFactory();
    }

    // region overridden methods with default implementation

    @Override
    public void onTaskAdded() {
        // do nothing
    }

    @Override
    public void onPerformRequest() {
        // do nothing
    }

    @Override
    public void onTaskFinished() {
        // do nothing
    }

    @Override
    public void onTaskRemoved() {
        // do nothing
    }

    // endregion
}
