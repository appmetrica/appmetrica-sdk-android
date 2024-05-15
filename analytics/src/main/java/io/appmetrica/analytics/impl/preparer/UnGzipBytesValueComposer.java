package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypter;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypterProvider;
import io.appmetrica.analytics.logger.internal.DebugLogger;

public class UnGzipBytesValueComposer implements ValueComposer {

    private static final String TAG = "[UnGzipBytesValueComposer]";

    @NonNull
    private final EventEncrypterProvider eventEncrypterProvider;

    public UnGzipBytesValueComposer() {
        this (new EventEncrypterProvider());
    }

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        byte[] value = new byte[0];
        try {
            value = Base64Utils.decompressBase64GzipAsBytes(event.getValue());
        } catch (Throwable e) {
            DebugLogger.error(TAG, e);
        }
        EventEncrypter eventEncrypter = eventEncrypterProvider.getEventEncrypter(event.getEventEncryptionMode());
        value = eventEncrypter.decrypt(value);
        return value == null ? new byte[0] : value;
    }

    @VisibleForTesting
    UnGzipBytesValueComposer(@NonNull EventEncrypterProvider eventEncrypterProvider) {
        this.eventEncrypterProvider = eventEncrypterProvider;
    }

    @VisibleForTesting
    @NonNull
    public EventEncrypterProvider getEventEncrypterProvider() {
        return eventEncrypterProvider;
    }
}
