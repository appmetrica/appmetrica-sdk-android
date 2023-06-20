package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypter;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypterProvider;

public class EncryptedStringValueComposer implements ValueComposer {

    @NonNull
    private final EventEncrypterProvider mEventEncrypterProvider;

    public EncryptedStringValueComposer() {
        this(new EventEncrypterProvider());
    }

    @VisibleForTesting
    EncryptedStringValueComposer(@NonNull EventEncrypterProvider eventEncrypterProvider) {
        mEventEncrypterProvider = eventEncrypterProvider;
    }

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        final byte[] result;
        if (event.getValue() != null) {
            result = StringUtils.getUTF8Bytes(event.getValue());
        } else {
            result = new byte[0];
        }
        EventEncrypter eventEncrypter = mEventEncrypterProvider.getEventEncrypter(event.getEventEncryptionMode());
        return eventEncrypter.decrypt(result);
    }
}
