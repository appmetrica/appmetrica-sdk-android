package io.appmetrica.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Custom crash transformer. Used while handling crashes caught by AppMetrica.
 * Crashes caught by AppMetrica will be passed to {@link ICrashTransformer#process(Throwable)}
 * and result will be reported. If null is returned, crash is not reported.
 */
public interface ICrashTransformer {

    /**
     * This method is called before crash sending. It allows user to transform {@link Throwable} that is processed.
     *
     * @param crash Caught {@link Throwable}
     * @return Transformed {@link Throwable}
     */
    @Nullable
    Throwable process(@NonNull Throwable crash);
}
