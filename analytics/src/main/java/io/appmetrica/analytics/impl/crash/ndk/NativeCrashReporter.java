package io.appmetrica.analytics.impl.crash.ndk;

import androidx.annotation.NonNull;

public interface NativeCrashReporter<T> {

    void reportCurrentSessionNativeCrash(@NonNull T nativeCrash);

    void reportPrevSessionNativeCrash(@NonNull T nativeCrash);

}
