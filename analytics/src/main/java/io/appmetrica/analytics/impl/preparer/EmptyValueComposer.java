package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;

public class EmptyValueComposer implements ValueComposer {

    @NonNull
    @Override
    public byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config) {
        return new byte[0];
    }
}
