package io.appmetrica.analytics.impl.utils.encryption;

import io.appmetrica.analytics.impl.ServiceEvent;

public interface EventEncrypter {

    public EncryptedCounterReport encrypt(ServiceEvent serviceEvent);

    public byte[] decrypt(byte[] input);

    public EventEncryptionMode getEncryptionMode();

}
