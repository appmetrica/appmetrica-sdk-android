package io.appmetrica.analytics.network.internal;

import androidx.annotation.NonNull;

public interface Call {

    @NonNull
    Response execute();
}
