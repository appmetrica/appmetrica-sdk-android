package io.appmetrica.analytics.impl.referrer.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.referrer.common.ReferrerChosenListener;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.HashSet;
import java.util.Set;

public class ReferrerManager implements ReferrerHolder.Listener {

    private static final String TAG = "[ReferrerManager]";

    @NonNull
    private final Set<ReferrerChosenListener> listeners = new HashSet<ReferrerChosenListener>();
    private boolean referrerChosen;
    @Nullable
    private ReferrerInfo chosenReferrer;

    public ReferrerManager() {
        this(GlobalServiceLocator.getInstance().getReferrerHolder());
    }

    @VisibleForTesting
    ReferrerManager(@NonNull ReferrerHolder referrerHolder) {
        referrerHolder.subscribe(new SimpleReferrerListenerNotifier(this));
        referrerHolder.retrieveReferrerIfNeeded();
    }


    // region chosen referrer listener

    @Override
    public synchronized void handleReferrer(@Nullable ReferrerInfo referrer) {
        DebugLogger.info(TAG, "handle referrer %s", referrer);
        chosenReferrer = referrer;
        referrerChosen = true;
        notifyListenersIfNeeded();
    }

    // end region

    public synchronized void addOneShotListener(@NonNull ReferrerChosenListener listener) {
        DebugLogger.info(TAG, "add listener: %s, current listeners count: %d", listener, listeners.size());
        listeners.add(listener);
        notifyListenerIfNeeded(listener);
    }

    private void notifyListenersIfNeeded() {
        DebugLogger.info(TAG, "notifyListenersIfNeeded. Listeners count: %d", listeners.size());
        if (referrerChosen) {
            for (ReferrerChosenListener listener : listeners) {
                listener.onReferrerChosen(chosenReferrer);
            }
            listeners.clear();
        }
    }

    private void notifyListenerIfNeeded(@NonNull ReferrerChosenListener listener) {
        DebugLogger.info(TAG, "notify listener %s, referrerChosen: %b", listener, referrerChosen);
        if (referrerChosen) {
            listener.onReferrerChosen(chosenReferrer);
            listeners.remove(listener);
        }
    }
}
