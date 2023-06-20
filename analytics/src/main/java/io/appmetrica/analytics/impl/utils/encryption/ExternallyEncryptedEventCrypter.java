package io.appmetrica.analytics.impl.utils.encryption;

import android.util.Base64;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.impl.CounterReport;

public class ExternallyEncryptedEventCrypter implements EventEncrypter {

    public EncryptedCounterReport encrypt(CounterReport input) {
        throw new UnsupportedOperationException();
    }

    public byte[] decrypt(byte[] input) {
        try {
            return Base64.decode(input, Base64.DEFAULT);
        } catch (Throwable ex) {
            YLogger.e(ex, "Could not decode base 64 value.");
        }
        return new byte[0];
    }

    public EventEncryptionMode getEncryptionMode() {
        return EventEncryptionMode.EXTERNALLY_ENCRYPTED_EVENT_CRYPTER;
    }
}
