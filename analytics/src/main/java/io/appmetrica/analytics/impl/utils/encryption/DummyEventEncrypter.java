package io.appmetrica.analytics.impl.utils.encryption;

import io.appmetrica.analytics.impl.ServiceEvent;

public class DummyEventEncrypter implements EventEncrypter {

    public EncryptedCounterReport encrypt(ServiceEvent serviceEvent) {
        return new EncryptedCounterReport(serviceEvent, getEncryptionMode());
    }

    public byte[] decrypt(byte[] input) {
        return input;
    }

    public EventEncryptionMode getEncryptionMode() {
        return EventEncryptionMode.NONE;
    }
}
