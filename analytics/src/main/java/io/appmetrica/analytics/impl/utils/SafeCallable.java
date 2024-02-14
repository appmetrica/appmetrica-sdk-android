package io.appmetrica.analytics.impl.utils;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.internal.YLogger;
import java.util.concurrent.Callable;

public abstract class SafeCallable<T> implements Callable<T> {

    @Override
    @Nullable
    public T call() {
        try {
            return callSafely();
        } catch (Throwable e) {
            YLogger.e(e, e.getMessage());
        }
        return null;
    }

    public abstract T callSafely() throws Exception;
}
