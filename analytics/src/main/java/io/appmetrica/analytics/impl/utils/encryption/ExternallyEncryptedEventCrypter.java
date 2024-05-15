package io.appmetrica.analytics.impl.utils.encryption;

import android.util.Base64;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public class ExternallyEncryptedEventCrypter implements EventEncrypter {

    private static final String TAG = "[ExternallyEncryptedEventCrypter]";

    public EncryptedCounterReport encrypt(CounterReport input) {
        throw new UnsupportedOperationException();
    }

    public byte[] decrypt(byte[] input) {
        try {
            return Base64.decode(input, Base64.DEFAULT);
        } catch (Throwable ex) {
            DebugLogger.error(TAG, ex, "Could not decode base 64 value.");
        }
        return new byte[0];
    }

    public EventEncryptionMode getEncryptionMode() {
        return EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER;
    }
}
