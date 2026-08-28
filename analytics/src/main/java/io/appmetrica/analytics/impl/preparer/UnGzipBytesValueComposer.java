package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreutils.internal.io.Base64Utils;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class UnGzipBytesValueComposer implements ValueComposer {

    private static final String TAG = "[UnGzipBytesValueComposer]";

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        byte[] value = new byte[0];
        try {
            value = Base64Utils.decompressBase64GzipAsBytes(event.getValue());
        } catch (Throwable e) {
            DebugLogger.INSTANCE.error(TAG, e);
        }
        return value == null ? new byte[0] : value;
    }
}
