package io.appmetrica.analytics.impl.crash.jvm.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ICrashProcessor {

    void processCrash(@Nullable Throwable originalException, @NonNull AllThreads allThreads);
}
