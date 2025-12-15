package io.appmetrica.analytics.networkokhttp.internal;

import androidx.annotation.NonNull;
import okhttp3.Interceptor;

// see https://nda.ya.ru/t/Aq0cfIc77NXYZt
public interface InterceptorSupplier {

    @NonNull
    Interceptor get();
}
