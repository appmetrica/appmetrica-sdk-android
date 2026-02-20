package io.appmetrica.analytics.impl.startup;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.StartupParamsItem;
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers;
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils;
import io.appmetrica.analytics.impl.ClientIdentifiersHolder;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.DataResultReceiver;
import io.appmetrica.analytics.impl.FeaturesResult;
import io.appmetrica.analytics.impl.IServerTimeOffsetProvider;
import io.appmetrica.analytics.impl.ReportsHandler;
import io.appmetrica.analytics.impl.Utils;
import io.appmetrica.analytics.impl.utils.JsonHelper;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;
import io.appmetrica.analytics.impl.utils.StartupUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class StartupHelper implements StartupIdentifiersProvider, IServerTimeOffsetProvider {

    private static final String TAG = "[StartupHelper]";

    static final Map<StartupError, StartupParamsCallback.Reason> STARTUP_ERROR_TO_REASON_MAP =
            Collections.unmodifiableMap(new HashMap<StartupError, StartupParamsCallback.Reason>() {
                {
                    put(StartupError.UNKNOWN, StartupParamsCallback.Reason.UNKNOWN);
                    put(StartupError.NETWORK, StartupParamsCallback.Reason.NETWORK);
                    put(StartupError.PARSE, StartupParamsCallback.Reason.INVALID_RESPONSE);
                }
            });

    private final List<String> mAllIdentifiers = Arrays.asList(
            Constants.StartupParamsCallbackKeys.UUID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID,
            Constants.StartupParamsCallbackKeys.DEVICE_ID_HASH,
            Constants.StartupParamsCallbackKeys.GET_AD_URL,
            Constants.StartupParamsCallbackKeys.REPORT_AD_URL,
            Constants.StartupParamsCallbackKeys.CLIDS
    );

    private final ReportsHandler mReportsHandler;

    private final StartupParams mStartupParams;

    @NonNull
    private final Handler mHandler;
    @Nullable
    private PublicLogger mPublicLogger;

    private final DataResultReceiver.Receiver defaultReceiver;

    private final Object mStartupParamsLock = new Object();
    private final Map<StartupParamsCallback, List<String>> mStartupParamsCallbacks = new WeakHashMap<>();

    private Map<String, String> mClientClids;
    @VisibleForTesting
    boolean initialStartupSent = false;

    public StartupHelper(
        @NonNull ReportsHandler reportsHandler,
        @NonNull StartupParams startupParams,
        @NonNull Handler handler
    ) {
        mReportsHandler = reportsHandler;
        mStartupParams = startupParams;
        mHandler = handler;
        defaultReceiver = new DataResultReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, @NonNull Bundle resultData) {
                updateAllParamsByReceiver(resultData);
            }
        };
    }

    @Override
    public String getUuid() {
        return mStartupParams.getUuid();
    }

    @Override
    public String getDeviceId() {
        return mStartupParams.getDeviceId();
    }

    @Override
    public long getServerTimeOffsetSeconds() {
        return mStartupParams.getServerTimeOffsetSeconds();
    }

    public void requestStartupParams(
        @NonNull final StartupParamsCallback callback,
        @NonNull final List<String> identifiers,
        @Nullable Map<String, String> freshClientClids
    ) {
        synchronized (mStartupParamsLock) {
            mStartupParams.setClientClids(freshClientClids);
            registerIdentifiersCallback(callback, identifiers);
            if (mStartupParams.shouldSendStartup(identifiers)) {
                DataResultReceiver.Receiver receiver = new DataResultReceiver.Receiver() {
                    @Override
                    public void onReceiveResult(int resultCode, Bundle resultData) {
                        DebugLogger.INSTANCE.info(
                            TAG,
                            "Received result %s with code %d for callback: %s",
                            resultData,
                            resultCode,
                            callback
                        );
                        processResultFromResultReceiver(resultData, callback);
                    }
                };
                sendStartupEvent(
                    identifiers,
                    receiver,
                    freshClientClids,
                    // The parameters can also be requested before activation.
                    // In this case, we must apply the current configuration based on the default parameter values.
                    // Otherwise, you will not be able to send the startup request.
                    true
                );
            } else {
                notifyCallbackWithLocalDataIfNotYet(callback);
            }
        }
    }

    public void processResultFromResultReceiver(@NonNull Bundle resultData) {
        processResultFromResultReceiver(resultData, null);
    }

    public void processResultFromResultReceiver(@NonNull Bundle resultData,
                                                @Nullable StartupParamsCallback callback) {
        synchronized (mStartupParamsLock) {
            updateAllParamsByReceiver(resultData);
            notifyCallbacksIfValid();
            if (callback != null) {
                notifyCallbackIfNotYet(callback, resultData);
            }
        }
    }

    public void setPublicLogger(@NonNull PublicLogger publicLogger) {
        mPublicLogger= publicLogger;
    }

    private void sendStartupEvent(@Nullable Map<String, String> clids, boolean forceRefreshConfiguration) {
        sendStartupEvent(mAllIdentifiers, clids, forceRefreshConfiguration);
    }

    private void sendStartupEvent(@NonNull List<String> identifiers,
                                  @Nullable Map<String, String> freshClientClids,
                                  boolean forceRefreshConfiguration) {
        sendStartupEvent(identifiers, defaultReceiver, freshClientClids, forceRefreshConfiguration);
    }

    private void sendStartupEvent(@NonNull List<String> identifiers,
                                  @NonNull DataResultReceiver.Receiver receiver,
                                  @Nullable Map<String, String> freshClientClids,
                                  boolean forceRefreshConfiguration) {
        DataResultReceiver resultReceiver = new DataResultReceiver(mHandler, receiver);
        mReportsHandler.reportStartupEvent(identifiers, resultReceiver, freshClientClids, forceRefreshConfiguration);
    }

    private void updateAllParamsByReceiver(@NonNull Bundle resultData) {
        DebugLogger.INSTANCE.info(TAG, "UpdateAllParamsByReceiver: %s", resultData);
        ClientIdentifiersHolder clientIdentifiersHolder = new ClientIdentifiersHolder(resultData);
        mStartupParams.updateAllParamsByReceiver(clientIdentifiersHolder);
        notifyModulesWithConfig(
            clientIdentifiersHolder.getModulesConfig(),
            new SdkIdentifiers(
                clientIdentifiersHolder.getUuid().id,
                clientIdentifiersHolder.getDeviceId().id,
                clientIdentifiersHolder.getDeviceIdHash().id
            )
        );
        notifyCallbacksIfValid();
    }

    public void sendStartupIfNeeded() {
        synchronized (mStartupParamsLock) {
            if (!initialStartupSent || mStartupParams.shouldSendStartup()) {
                initialStartupSent = true;
                DebugLogger.INSTANCE.info(TAG, "Send startup event");
                // Optional configuration update. If there was no initialization on the part of the application,
                // the configuration based on default values must not be applied.
                sendStartupEvent(mClientClids, false);
            }
        }
    }

    public void setCustomHosts(final List<String> customHosts) {
        synchronized (mStartupParamsLock) {
            List<String> oldCustomHosts = mStartupParams.getCustomHosts();

            if (Utils.isNullOrEmpty(customHosts)) {
                if (Utils.isNullOrEmpty(oldCustomHosts) == false) {
                    mStartupParams.setCustomHosts(null);
                    mReportsHandler.setCustomHosts(null);
                }
            } else if (Utils.areEqual(customHosts, oldCustomHosts) == false) {
                mStartupParams.setCustomHosts(customHosts);
                mReportsHandler.setCustomHosts(customHosts);
            } else {
                mReportsHandler.setCustomHosts(oldCustomHosts);
            }
        }
    }

    public void setClids(final Map<String, String> clids) {
        if (Utils.isNullOrEmpty(clids) == false) {
            synchronized (mStartupParamsLock) {
                Map<String, String> validClids = StartupUtils.validateClids(clids);
                mClientClids = validClids;
                mReportsHandler.setClids(validClids);
                mStartupParams.setClientClids(validClids);
            }
        }
    }

    public void setDistributionReferrer(String distributionReferrer) {
        synchronized (mStartupParamsLock) {
            mReportsHandler.setDistributionReferrer(distributionReferrer);
        }
    }

    public void setInstallReferrerSource(@Nullable String source) {
        synchronized (mStartupParamsLock) {
            mReportsHandler.setInstallReferrerSource(source);
        }
    }

    public Map<String, String> getClids() {
        Map<String, String> result;
        String startupClids = mStartupParams.getClids();
        if (!TextUtils.isEmpty(startupClids)) {
            result = JsonHelper.clidsFromString(startupClids);
        } else {
            result = mClientClids;
        }
        DebugLogger.INSTANCE.info(
            TAG,
            "Get clids return %s (startupClids = %s; mClientClids = %s)",
            result,
            startupClids,
            mClientClids
        );
        return result;
    }

    private void notifyCallbackWithLocalDataIfNotYet(@NonNull StartupParamsCallback callback) {
        notifyCallbackIfNotYet(callback, new Bundle());
    }

    private void notifyCallbackIfNotYet(@NonNull StartupParamsCallback callback,
                                        @NonNull Bundle bundle) {
        DebugLogger.INSTANCE.info(
            TAG,
            "notifyCallbackIfNotYet. Callback: %s, bundle: %s",
            callback,
            bundle
        );
        if (mStartupParamsCallbacks.containsKey(callback)) {
            List<String> identifiers = mStartupParamsCallbacks.get(callback);
            if (mStartupParams.containsIdentifiers(identifiers)) {
                notifyCallbackOnReceive(callback, identifiers);
            } else {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "notify callback with error. Callback: %s, bundle: %s",
                    callback,
                    bundle
                );
                StartupError error = StartupError.fromBundle(bundle);
                StartupParamsCallback.Reason reason = null;
                if (error == null) {
                    if (mStartupParams.areResponseClidsConsistent() == false) {
                        if (mPublicLogger != null) {
                            mPublicLogger.warning(
                                "Clids error. Passed clids: %s, and clids from server are empty.",
                                mClientClids
                            );
                        }
                        reason = new StartupParamsCallback.Reason("INCONSISTENT_CLIDS");
                    } else {
                        error = StartupError.UNKNOWN;
                    }
                }
                if (reason == null) {
                    reason = CollectionUtils.getOrDefault(
                        STARTUP_ERROR_TO_REASON_MAP,
                        error,
                        StartupParamsCallback.Reason.UNKNOWN
                    );
                }
                notifyCallbackOnError(callback, identifiers, reason);
            }
            unregisterIdentifiersCallback(callback);
        }
    }

    private void notifyCallbackOnReceive(@NonNull StartupParamsCallback callback,
                                         @NonNull List<String> identifiers) {
        DebugLogger.INSTANCE.info(TAG, "notifyCallbackOnReceive with identifiers: %s", identifiers);
        callback.onReceive(createMapWithRequestedIdentifiersOnly(identifiers));
    }

    private void notifyCallbackOnError(@NonNull StartupParamsCallback callback,
                                       @NonNull List<String> identifiers,
                                       @NonNull StartupParamsCallback.Reason reason) {
        DebugLogger.INSTANCE.info(
            TAG,
            "notifyCallbackOnError, reason :%s, identifiers: %s",
            reason,
            identifiers
        );
        callback.onRequestError(reason, createMapWithRequestedIdentifiersOnly(identifiers));
    }

    private void notifyCallbacksIfValid() {
        DebugLogger.INSTANCE.info(TAG, "NotifyCallbacksIfValid");

        final Map<StartupParamsCallback, List<String>> callbacksToNotify = new WeakHashMap<>();

        DebugLogger.INSTANCE.info(
            TAG,
            "Try to notify %d identifiers callbacks",
            mStartupParamsCallbacks.size()
        );
        for (final Map.Entry<StartupParamsCallback, List<String>> entry : mStartupParamsCallbacks.entrySet()) {
            List<String> requestedIdentifiers = entry.getValue();
            DebugLogger.INSTANCE.info(TAG, "callback requested identifiers: %s", requestedIdentifiers);
            if (mStartupParams.containsIdentifiers(requestedIdentifiers)) {
                callbacksToNotify.put(entry.getKey(), requestedIdentifiers);
                DebugLogger.INSTANCE.info(TAG, "add callback to notify");
            }
        }
        for (final Map.Entry<StartupParamsCallback, List<String>> entry : callbacksToNotify.entrySet()) {
            StartupParamsCallback callbackWrapper = entry.getKey();
            if (callbackWrapper != null) {
                notifyCallbackWithLocalDataIfNotYet(callbackWrapper);
            }
        }
        DebugLogger.INSTANCE.info(
            TAG,
            "Remove listeners for startup params, their identifiers are valid. Number of notices: %d",
            callbacksToNotify.size()
        );

        callbacksToNotify.clear();
    }

    @Nullable
    private StartupParamsCallback.Result createMapWithRequestedIdentifiersOnly(
            @Nullable List<String> identifiers
    ) {
        if (identifiers == null) {
            return null;
        }
        Map<String, StartupParamsItem> result = new HashMap<String, StartupParamsItem>();
        mStartupParams.putToMap(identifiers, result);
        return new StartupParamsCallback.Result(result);
    }

    private void registerIdentifiersCallback(final StartupParamsCallback callback, List<String> identifiers) {
        if (mStartupParamsCallbacks.isEmpty()) {
            mReportsHandler.onStartupRequestStarted();
            DebugLogger.INSTANCE.info(TAG, "Notify startup request started.");
        }
        mStartupParamsCallbacks.put(callback, identifiers);

        DebugLogger.INSTANCE.info(
            TAG,
            "Register callback. Total callbacks count = %d. Callback details: %s with identifiers mask %s",
            mStartupParamsCallbacks.size(),
            callback,
            identifiers
        );
    }

    private void unregisterIdentifiersCallback(final StartupParamsCallback callback) {
        mStartupParamsCallbacks.remove(callback);

        DebugLogger.INSTANCE.info(
            TAG,
            "Unregister callback. Total callbacks count = %d. Callback details: %s.",
            mStartupParamsCallbacks.size(),
            callback
        );

        if (mStartupParamsCallbacks.isEmpty()) {
            mReportsHandler.onStartupRequestFinished();
            DebugLogger.INSTANCE.info(TAG, "Notify all startup requests finished.");
        }
    }

    private void notifyModulesWithConfig(@Nullable Bundle bundle, @NonNull SdkIdentifiers identifiers) {
        ClientServiceLocator.getInstance().getModulesController().notifyModulesWithConfig(bundle, identifiers);
    }

    @VisibleForTesting
    @NonNull
    Map<StartupParamsCallback, List<String>> getStartupAllParamsCallbacks() {
        return mStartupParamsCallbacks;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @NonNull
    public DataResultReceiver.Receiver getDefaultReceiver() {
        return defaultReceiver;
    }

    @NonNull
    public AdvIdentifiersResult getCachedAdvIdentifiers() {
        return mStartupParams.getCachedAdvIdentifiers();
    }

    @NonNull
    public FeaturesResult getFeatures() {
        return mStartupParams.getFeatures();
    }
}
