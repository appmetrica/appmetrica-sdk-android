package io.appmetrica.analytics.impl.preparer;

import android.util.Base64;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class BytesValueComposer implements ValueComposer {

    private static final String TAG = "[BytesValueComposer]";

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        byte[] value = new byte[0];
        if (event.getValue() != null) {
            try {
                value = Base64.decode(event.getValue(), Base64.DEFAULT);
            } catch (Throwable e) {
                DebugLogger.INSTANCE.error(
                    TAG,
                    e,
                    "Something went wrong while decoding base 64 event value."
                );
            }
        }
        return value;
    }
}
