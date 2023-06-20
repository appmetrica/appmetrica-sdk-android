package io.appmetrica.analytics.impl.utils.encryption;

import io.appmetrica.analytics.impl.CounterReport;

public interface EventEncrypter {

    public EncryptedCounterReport encrypt(CounterReport counterReport);

    public byte[] decrypt(byte[] input);

    public EventEncryptionMode getEncryptionMode();

}
