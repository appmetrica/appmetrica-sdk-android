package io.appmetrica.analytics.coreapi.internal.executors;

import androidx.annotation.NonNull;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface ICommonExecutor extends IInterruptionSafeThread, Executor {

    void execute(@NonNull Runnable runnable);

    <T> Future<T> submit(Callable<T> task);

    void executeDelayed(@NonNull Runnable runnable, long delay);

    void executeDelayed(@NonNull Runnable runnable, long delay, @NonNull TimeUnit timeUnit);

    void remove(@NonNull Runnable runnable);

    void removeAll();
}
