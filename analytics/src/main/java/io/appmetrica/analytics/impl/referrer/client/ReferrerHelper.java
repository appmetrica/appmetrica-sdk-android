package io.appmetrica.analytics.impl.referrer.client;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.impl.DeferredDeeplinkStateManager;
import io.appmetrica.analytics.impl.ReferrerParser;
import io.appmetrica.analytics.impl.ReportsHandler;
import io.appmetrica.analytics.impl.db.preferences.PreferencesClientDbStorage;
import io.appmetrica.analytics.impl.referrer.common.ReferrerChosenListener;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.impl.referrer.common.ReferrerResultReceiver;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public class ReferrerHelper implements ReferrerChosenListener {

    private static final String TAG = "[Referrer]";

    private final boolean mDeferredDeeplinkWasChecked;

    @NonNull
    private final ReportsHandler mReportsHandler;
    @NonNull
    private final PreferencesClientDbStorage mClientPreferences;
    @NonNull
    private final DeferredDeeplinkStateManager mDeferredDeeplinkStateManager;
    @NonNull
    private final ReferrerParser mReferrerParser;
    @NonNull
    private final Handler callbackHandler;

    public ReferrerHelper(final ReportsHandler reportsHandler,
                          PreferencesClientDbStorage clientPreferences,
                          @NonNull Handler handler) {
        this(
                reportsHandler,
                clientPreferences,
                handler,
                clientPreferences.isDeferredDeeplinkWasChecked()
        );
    }

    private ReferrerHelper (@NonNull ReportsHandler reportsHandler,
                            @NonNull PreferencesClientDbStorage preferencesClientDbStorage,
                            @NonNull Handler handler,
                            boolean wasDeferredDeeplinkChecked) {
        this(
                reportsHandler,
                preferencesClientDbStorage,
                handler,
                wasDeferredDeeplinkChecked,
                new DeferredDeeplinkStateManager(wasDeferredDeeplinkChecked),
                new ReferrerParser()
        );
    }

    @VisibleForTesting
    ReferrerHelper(@NonNull final ReportsHandler reportsHandler,
                   PreferencesClientDbStorage clientPreferences,
                   @NonNull Handler handler,
                   boolean deferredDeeplinkWasChecked,
                   @NonNull DeferredDeeplinkStateManager deferredDeeplinkStateManager,
                   @NonNull ReferrerParser referrerParser) {
        mReportsHandler = reportsHandler;
        mClientPreferences = clientPreferences;
        mDeferredDeeplinkWasChecked = deferredDeeplinkWasChecked;
        mDeferredDeeplinkStateManager = deferredDeeplinkStateManager;
        mReferrerParser = referrerParser;
        this.callbackHandler = handler;
    }

    public void maybeRequestReferrer() {
        if (!mDeferredDeeplinkWasChecked) {
            mReportsHandler.reportRequestReferrerEvent(new ReferrerResultReceiver(callbackHandler, this));
        }
    }

    @Override
    public void onReferrerChosen(@Nullable ReferrerInfo referrerInfo) {
        DebugLogger.info(TAG, "onReferrerChosen: %s", referrerInfo);
        saveReferrerAndParse(referrerInfo == null ? null : referrerInfo.installReferrer);
    }

    private void saveReferrerAndParse(@Nullable String referrer) {
        boolean shouldHandle = !mDeferredDeeplinkWasChecked;
        DebugLogger.info(TAG, "Should handle referrer %s? %b, because mDeferredDeeplinkWasChecked = %b",
                referrer, shouldHandle, mDeferredDeeplinkWasChecked);
        if (shouldHandle) {
            synchronized (this) {
                mDeferredDeeplinkStateManager.onDeeplinkLoaded(mReferrerParser.parseDeferredDeeplinkState(referrer));
            }
        }
    }

    public synchronized void requestDeferredDeeplinkParameters(@NonNull DeferredDeeplinkParametersListener listener) {
        DebugLogger.info(TAG, "requestDeferredDeeplinkParameters for listener: %s", listener);
        try {
            mDeferredDeeplinkStateManager.requestDeferredDeeplinkParameters(listener);
        } finally {
            mClientPreferences.markDeferredDeeplinkChecked();
        }
    }

    public synchronized void requestDeferredDeeplink(@NonNull DeferredDeeplinkListener listener) {
        DebugLogger.info(TAG, "requestDeferredDeeplink for listener: %s", listener);
        try {
            mDeferredDeeplinkStateManager.requestDeferredDeeplink(listener);
        } finally {
            mClientPreferences.markDeferredDeeplinkChecked();
        }
    }
}
