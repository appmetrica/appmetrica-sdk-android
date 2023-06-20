package io.appmetrica.analytics.impl.utils.encryption;

import io.appmetrica.analytics.impl.CounterReport;

public class DummyEventEncrypter implements EventEncrypter {

    public EncryptedCounterReport encrypt(CounterReport input) {
        return new EncryptedCounterReport(input, getEncryptionMode());
    }

    public byte[] decrypt(byte[] input) {
        return input;
    }

    public EventEncryptionMode getEncryptionMode() {
        return EventEncryptionMode.NONE;
    }
}
