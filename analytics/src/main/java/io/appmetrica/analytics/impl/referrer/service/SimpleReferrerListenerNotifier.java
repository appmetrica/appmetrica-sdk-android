package io.appmetrica.analytics.impl.referrer.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;

public class SimpleReferrerListenerNotifier extends ReferrerListenerNotifier {

    public SimpleReferrerListenerNotifier(@NonNull ReferrerHolder.Listener listener) {
        super(
                new IReferrerNotificationFilter() {
                    @Override
                    public boolean shouldNotify(@Nullable ReferrerInfo referrerInfo) {
                        return true;
                    }
                },
                listener,
                new IReferrerHandledNotifier() {
                    @Override
                    public void onReferrerHandled() {
                        // do nothing
                    }
                }
        );
    }
}
