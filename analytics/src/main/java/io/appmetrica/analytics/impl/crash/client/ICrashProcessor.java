package io.appmetrica.analytics.impl.crash.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ICrashProcessor {

    void processCrash(@Nullable Throwable originalException, @NonNull AllThreads allThreads);
}
