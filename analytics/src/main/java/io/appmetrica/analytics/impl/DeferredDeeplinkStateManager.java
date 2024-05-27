package io.appmetrica.analytics.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.coreutils.internal.WrapUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.Map;

public class DeferredDeeplinkStateManager {

    private static final String TAG = "[DeferredDeeplinkStateManager]";

    enum Error {
        NOT_A_FIRST_LAUNCH, PARSE_ERROR, NO_REFERRER
    }

    private boolean mDeferredDeeplinkWasChecked;
    @Nullable
    private DeferredDeeplinkListener mDeferredDeeplinkListener;
    @Nullable
    private DeferredDeeplinkParametersListener mDeferredDeeplinkParametersListener;
    @Nullable
    private DeferredDeeplinkState mDeferredDeeplinkState;

    public DeferredDeeplinkStateManager(boolean deferredDeeplinkWasChecked) {
        mDeferredDeeplinkWasChecked = deferredDeeplinkWasChecked;
    }

    public void onDeeplinkLoaded(@Nullable DeferredDeeplinkState deferredDeeplinkState) {
        DebugLogger.INSTANCE.info(TAG, "onDeeplinkLoaded: %s", deferredDeeplinkState);
        mDeferredDeeplinkState = deferredDeeplinkState;
        notifyListenerIfNeeded();
    }

    private void notifyListenersOnError(@NonNull Error error) {
        String referrer = mDeferredDeeplinkState == null ? null : mDeferredDeeplinkState.mUnparsedReferrer;
        DebugLogger.INSTANCE.info(TAG, "onError: %s, referrer: %s.", error, referrer);
        notifyDeeplinkListenerOnError(error, referrer);
        notifyParametersListenerOnError(error, referrer);
    }

    public void requestDeferredDeeplinkParameters(@NonNull DeferredDeeplinkParametersListener listener) {
        mDeferredDeeplinkParametersListener = listener;
        requestSomeDeferredDeeplinkInfo();
    }

    public void requestDeferredDeeplink(@NonNull DeferredDeeplinkListener deferredDeeplinkListener) {
        mDeferredDeeplinkListener = deferredDeeplinkListener;
        requestSomeDeferredDeeplinkInfo();
    }

    private void requestSomeDeferredDeeplinkInfo() {
        DebugLogger.INSTANCE.info(
            TAG,
            "requestSomeDeferredDeeplinkInfo. mDeferredDeeplinkWasChecked: %b, state: %s",
            mDeferredDeeplinkWasChecked,
            mDeferredDeeplinkState
        );
        if (mDeferredDeeplinkWasChecked) {
            notifyListenersOnError(DeferredDeeplinkStateManager.Error.NOT_A_FIRST_LAUNCH);
        } else {
            notifyListenerIfNeeded();
        }
    }

    private void notifyListenerIfNeeded() {
        DebugLogger.INSTANCE.info(TAG, "notifyListenerIfNeeded: %s", mDeferredDeeplinkState);
        if (mDeferredDeeplinkState != null) {
            if (mDeferredDeeplinkState.mDeeplink != null) {
                notifyDeeplinkListener(mDeferredDeeplinkState.mDeeplink);
                if (Utils.isNullOrEmpty(mDeferredDeeplinkState.mParameters) == false) {
                    notifyParametersListener(mDeferredDeeplinkState.mParameters);
                } else {
                    notifyParametersListenerOnError(Error.PARSE_ERROR, mDeferredDeeplinkState.mUnparsedReferrer);
                }
            } else if (mDeferredDeeplinkState.mUnparsedReferrer != null) {
                notifyListenersOnError(Error.PARSE_ERROR);
            } else {
                notifyListenersOnError(Error.NO_REFERRER);
            }
        }
    }

    private void notifyDeeplinkListener(@NonNull String deeplink) {
        DebugLogger.INSTANCE.info(
            TAG,
            "notifyDeeplinkListener. Deeplink: %s, listener: %s",
            deeplink,
            mDeferredDeeplinkListener
        );
        if (mDeferredDeeplinkListener != null) {
            mDeferredDeeplinkListener.onDeeplinkLoaded(deeplink);
            mDeferredDeeplinkListener = null;
        }
    }

    private void notifyParametersListener(@NonNull Map<String, String> parameters) {
        DebugLogger.INSTANCE.info(
            TAG,
            "notifyParametersListener. Parameters: %s, listener: %s",
            parameters,
            mDeferredDeeplinkParametersListener
        );
        if (mDeferredDeeplinkParametersListener != null) {
            mDeferredDeeplinkParametersListener.onParametersLoaded(parameters);
            mDeferredDeeplinkParametersListener = null;
        }
    }

    private void notifyDeeplinkListenerOnError(@NonNull Error error, @Nullable String unparsedReferrer) {
        DebugLogger.INSTANCE.info(
            TAG,
            "notifyDeeplinkListenerOnError. Error: %s, unparsed referrer: %s, listener: %s",
            error,
            unparsedReferrer,
            mDeferredDeeplinkListener
        );
        if (mDeferredDeeplinkListener != null) {
            mDeferredDeeplinkListener.onError(
                    convertErrorForDeeplinkListener(error),
                    WrapUtils.getOrDefault(unparsedReferrer, StringUtils.EMPTY)
            );
            mDeferredDeeplinkListener = null;
        }
    }

    private void notifyParametersListenerOnError(@NonNull Error error, @Nullable String unparsedReferrer) {
        DebugLogger.INSTANCE.info(
            TAG,
            "notifyDeeplinkLParameterListenerOnError. Error: %s, unparsed referrer: %s, listener: %s",
            error,
            unparsedReferrer,
            mDeferredDeeplinkParametersListener
        );
        if (mDeferredDeeplinkParametersListener != null) {
            mDeferredDeeplinkParametersListener.onError(
                    convertErrorForDeeplinkParametersListener(error),
                    WrapUtils.getOrDefault(unparsedReferrer, StringUtils.EMPTY)
            );
            mDeferredDeeplinkParametersListener = null;
        }
    }

    @NonNull
    private DeferredDeeplinkListener.Error convertErrorForDeeplinkListener(@NonNull Error error) {
        switch (error) {
            case NOT_A_FIRST_LAUNCH:
                return DeferredDeeplinkListener.Error.NOT_A_FIRST_LAUNCH;
            case PARSE_ERROR:
                return DeferredDeeplinkListener.Error.PARSE_ERROR;
            case NO_REFERRER:
                return DeferredDeeplinkListener.Error.NO_REFERRER;
            default:
                return DeferredDeeplinkListener.Error.UNKNOWN;
        }
    }

    @NonNull
    private DeferredDeeplinkParametersListener.Error convertErrorForDeeplinkParametersListener(@NonNull Error error) {
        switch (error) {
            case NOT_A_FIRST_LAUNCH:
                return DeferredDeeplinkParametersListener.Error.NOT_A_FIRST_LAUNCH;
            case PARSE_ERROR:
                return DeferredDeeplinkParametersListener.Error.PARSE_ERROR;
            case NO_REFERRER:
                return DeferredDeeplinkParametersListener.Error.NO_REFERRER;
            default:
                return DeferredDeeplinkParametersListener.Error.UNKNOWN;
        }
    }
}
