package io.appmetrica.analytics.networktasks.internal;

import androidx.annotation.NonNull;

public interface ConfigProvider<T> {

    @NonNull
    T getConfig();
}
