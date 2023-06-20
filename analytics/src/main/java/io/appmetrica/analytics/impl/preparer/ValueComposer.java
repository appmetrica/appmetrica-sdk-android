package io.appmetrica.analytics.impl.preparer;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.request.ReportRequestConfig;

public interface ValueComposer {

    @NonNull
    byte[] getValue(@NonNull EventFromDbModel event, @NonNull ReportRequestConfig config);
}
