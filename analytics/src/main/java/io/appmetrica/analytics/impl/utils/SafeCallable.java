package io.appmetrica.analytics.impl.utils;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.concurrent.Callable;

public abstract class SafeCallable<T> implements Callable<T> {

    private static final String TAG = "[SafeCallable]";

    @Override
    @Nullable
    public T call() {
        try {
            return callSafely();
        } catch (Throwable e) {
            YLogger.error(TAG, e, e.getMessage());
        }
        return null;
    }

    public abstract T callSafely() throws Exception;
}
