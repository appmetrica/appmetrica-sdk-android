package io.appmetrica.analytics.impl.utils.encryption;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;

public class EncryptedCounterReport {

    @NonNull
    public final CounterReport mCounterReport;
    @NonNull
    public final EventEncryptionMode mEventEncryptionMode;

    public EncryptedCounterReport(@NonNull CounterReport counterReport,
                                  @NonNull EventEncryptionMode eventEncryptionMode) {
        mCounterReport = counterReport;
        mEventEncryptionMode = eventEncryptionMode;
    }
}
