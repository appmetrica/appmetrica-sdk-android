package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface NetworkResponseHandler<T> {

    @Nullable
    T handle(@NonNull ResponseDataHolder responseDataHolder);
}
