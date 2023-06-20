package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Custom crash transformer. Used while handling crashes caught by AppMetrica.
 * Crashes caught by AppMetrica will be passed to {@link ICrashTransformer#process(Throwable)}
 * and result will be reported. If null is returned, crash is not reported.
 */
public interface ICrashTransformer {

    @Nullable
    Throwable process(@NonNull Throwable crash);
}
