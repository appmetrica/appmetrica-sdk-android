package io.appmetrica.analytics.impl.preparer;

import android.util.Base64;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypter;
import io.appmetrica.analytics.impl.utils.encryption.EventEncrypterProvider;
import io.appmetrica.analytics.logger.internal.YLogger;

public class BytesValueComposer implements ValueComposer {

    private static final String TAG = "[BytesValueComposer]";

    @NonNull
    private final EventEncrypterProvider mEventEncrypterProvider;

    public BytesValueComposer() {
        this(new EventEncrypterProvider());
    }

    @VisibleForTesting
    BytesValueComposer(@NonNull EventEncrypterProvider eventEncrypterProvider) {
        mEventEncrypterProvider = eventEncrypterProvider;
    }

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        byte[] value = new byte[0];
        if (event.getValue() != null) {
            try {
                value = Base64.decode(event.getValue(), Base64.DEFAULT);
            } catch (Throwable e) {
                YLogger.error(TAG, e, "Something went wrong while decoding base 64 event value.");
            }
        }
        EventEncrypter eventEncrypter = mEventEncrypterProvider.getEventEncrypter(event.getEventEncryptionMode());
        return eventEncrypter.decrypt(value);
    }
}
