package io.appmetrica.analytics.impl.utils.encryption;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.ServiceEvent;

public class EncryptedCounterReport {

    @NonNull
    public final ServiceEvent mServiceEvent;
    @NonNull
    public final EventEncryptionMode mEventEncryptionMode;

    public EncryptedCounterReport(@NonNull ServiceEvent serviceEvent,
                                  @NonNull EventEncryptionMode eventEncryptionMode) {
        mServiceEvent = serviceEvent;
        mEventEncryptionMode = eventEncryptionMode;
    }
}
