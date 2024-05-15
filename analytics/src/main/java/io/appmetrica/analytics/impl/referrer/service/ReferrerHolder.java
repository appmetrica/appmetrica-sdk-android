package io.appmetrica.analytics.impl.referrer.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.db.VitalCommonDataProvider;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.HashSet;
import java.util.Set;

public class ReferrerHolder {

    public interface Listener {

        void handleReferrer(@Nullable ReferrerInfo referrer);

    }

    private static final String TAG = "[ReferrerHolderFacade]";

    private final Set<ReferrerListenerNotifier> mListeners = new HashSet<ReferrerListenerNotifier>();

    @Nullable
    private ReferrerInfo mReferrerFromServices;
    private boolean isReferrerChecked;

    @NonNull
    private final VitalCommonDataProvider vitalCommonDataProvider;
    @NonNull
    private final Context context;

    @WorkerThread
    public ReferrerHolder(@NonNull Context context) {
        this(context, GlobalServiceLocator.getInstance().getVitalDataProviderStorage().getCommonDataProvider());
    }

    @VisibleForTesting
    ReferrerHolder(@NonNull Context context, @NonNull VitalCommonDataProvider vitalCommonDataProvider) {
        this.context = context;
        this.vitalCommonDataProvider = vitalCommonDataProvider;
        mReferrerFromServices = vitalCommonDataProvider.getReferrer();
        isReferrerChecked = vitalCommonDataProvider.getReferrerChecked();
        DebugLogger.info(TAG, "Init ReferrerHolder with referrer = %s", mReferrerFromServices);
    }

    public void retrieveReferrerIfNeeded() {
        DebugLogger.info(TAG, "retrieveReferrerIfNeeded. isReferrerChecked: %b", isReferrerChecked);
        if (!isReferrerChecked) {
            new ReferrerAggregator(context, this).retrieveReferrer();
        }
    }

    public synchronized void storeReferrer(@Nullable ReferrerInfo referrer) {
        DebugLogger.info(TAG, "store referrer %s", referrer);
        mReferrerFromServices = referrer;
        isReferrerChecked = true;
        vitalCommonDataProvider.setReferrer(referrer);
        vitalCommonDataProvider.setReferrerChecked(true);
        notifyListeners(mReferrerFromServices);
    }

    @Nullable
    public ReferrerInfo getReferrerInfo() {
        return mReferrerFromServices;
    }

    public synchronized void subscribe(@NonNull ReferrerListenerNotifier listener) {
        mListeners.add(listener);
        DebugLogger.info(TAG, "Subscribe listener. Actual listeners: %s, referrer checked: %b",
                mListeners, isReferrerChecked);
        if (isReferrerChecked) {
            notifyListener(mReferrerFromServices, listener);
        }
    }

    private synchronized void notifyListeners(@Nullable ReferrerInfo referrerInfo) {
        DebugLogger.info(TAG, "notifyListeners. Listeners size: %d, referrer: %s",
                mListeners.size(), referrerInfo);
        for (ReferrerListenerNotifier listener : mListeners) {
            notifyListener(referrerInfo, listener);
        }
    }

    private void notifyListener(@Nullable ReferrerInfo referrerInfo,
                                @NonNull ReferrerListenerNotifier listener) {
        DebugLogger.info(TAG, "notifyListener. Listener: %s, referrer: %s", listener, referrerInfo);
        listener.notifyIfNeeded(referrerInfo);
    }
}
